# bdd-cucumber-gherkin-lib-security-core

The basic implementations of the DAST (dynamic application security testing) scan, without Cucumber.

This module contains the scanner abstraction, the scan orchestration, the finding gate, the configuration objects, the
Testcontainers based [OWASP ZAP](https://www.zaproxy.org/) implementation and the entry points a plain jUnit suite
needs. It has **no dependency on Cucumber, on Spring, on Spring Boot or on any other module of this library**, so a
project that does not run Cucumber - or runs on a different Spring Boot generation - can use it and create its own
objects and configuration.

Projects that use the Gherkin sentences take [bdd-cucumber-gherkin-lib-security](../bdd-cucumber-gherkin-lib-security/README.md)
instead; this module comes in transitively and there is nothing extra to add.

## Prerequisites

- Docker (Testcontainers pulls the scanner image)
- JDK 25 - the published class files target the toolchain of this repository

## Gradle

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib-security-core:${version.bdd-cucumber-gherkin-lib}"
}
```

Published dependencies: Jackson 3 `jackson-databind`, Testcontainers and kotlin-logging. `junit-jupiter-api` is
`compileOnly` and is not published, so [SecurityScanExtension](#junit-5-extension) runs against whatever jUnit version
the project already has.

> The scan JVM **must** be started with `jdk.httpclient.allowRestrictedHeaders=host` and
> `sun.net.http.allowRestrictedHeaders=true`. ZAP serves the proxy and its own REST API on one port and tells them
> apart by the `Host` header; without these properties the JDK strips the header and every API call fails in a way
> that is hard to read.

## How a scan runs

1. Start the scanner and expose the host ports of the application under test to it.
2. Route the HTTP client of the tests through the proxy the scanner hands back, so every request is recorded.
3. Run the tests that produce the traffic.
4. Attack that traffic, write the report and fail on findings at or above a given risk.

The proxy history is the attack surface: the scan can only test the endpoints the tests actually called. Endpoints no
test touches are added by importing an API definition (`cucumbertest.security.api.definition-urls`, or
`SecurityScan.importApiDefinition`).

## Scan session

`SecurityScanSession` is the whole lifecycle in one object:

```kotlin
val properties = SecurityScanProperties(
    enabled = true,
    // hand the report directory over - nothing in this module reads system properties
    report = ReportProperties(outputDir = "build/reports/security")
)

SecurityScanSession(properties, ZapSecurityScanner.create(properties)).use { session ->
    val proxy = session.start(hostPorts = setOf(port))

    // route the HTTP client of the tests through proxy.host:proxy.port, then run the traffic

    session.scanAndVerify(maxDuration = Duration.ofMinutes(30), failFrom = SecurityRisk.MEDIUM)
}
```

`close()` stops the scanner, so `use` (Kotlin) or try-with-resources (Java) is enough to clean up even when the gate
fails the build. The granular operations - import an API definition, run the scan, store the report, export the
recording, gate separately - are on `session.scan`.

The scanner is a required argument rather than a default, so no class outside the `zap` package names a product.
Swapping the scanner means implementing `SecurityScanner` and passing your own instance; nothing else changes.

`maxDuration` and `failFrom` are parameters rather than configuration properties on purpose, so no profile can
silently weaken the gate.

## jUnit 5 extension

`SecurityScanExtension` wraps the same session around a test class:

```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiSecurityTests {
    @RegisterExtension
    val securityScan = SecurityScanExtension(
        properties = properties,
        scanner = ZapSecurityScanner.create(properties),
        maxDuration = Duration.ofMinutes(30),
        failFrom = SecurityRisk.MEDIUM,
        hostPorts = Supplier { setOf(port) }
    )
}
```

The scanner starts before the first test, the scan and the gate run after the last one, and the scanner is stopped in
either case. `securityScan.proxy` is the endpoint to point the test's HTTP client at.

`hostPorts` is resolved in `beforeAll` rather than at construction, so a port the server only assigns while the
context starts can still be supplied. Because of that, a randomly assigned port needs a **non static**
`@RegisterExtension` field on a `PER_CLASS` class - class level callbacks still fire there. With a fixed port, set
`target.port` on the properties, register the extension statically and leave `hostPorts` out.

While `properties.enabled` is `false` - the default - the extension does nothing, so it can stay on a test class that
also runs outside the security profile.

## Configuration

`SecurityScanProperties` is a plain data class with defaults for everything; the binding annotation deliberately
lives with whoever creates the bean. Build it directly:

```kotlin
val properties = SecurityScanProperties(
    enabled = true,
    target = TargetProperties(port = 8080, exposedPorts = listOf(8080, 8081)),
    alerts = AlertProperties(ignoredRuleIds = setOf("40042"))
)
```

Every constructor carries `@JvmOverloads`, so Java gets a no-argument constructor and the trailing defaults:

```java
SecurityScanProperties properties = new SecurityScanProperties();
```

To read the values from an `application.yaml` under `cucumbertest.security` in a Spring Boot project, bind them
yourself - this works on any Spring Boot generation and needs no annotation on the type:

```kotlin
@Bean
fun securityScanProperties(environment: Environment): SecurityScanProperties {
    return Binder.get(environment)
        .bind(SecurityScanProperties.PREFIX, SecurityScanProperties::class.java)
        .orElseGet { SecurityScanProperties() }
}
```

The property reference is in the
[security module README](../bdd-cucumber-gherkin-lib-security/README.md#configuration-reference); the keys are the
same, because both modules bind the same type.

## Architecture

```
  SecurityScanExtension ─────────┐   jUnit entry point (….security.junit)
  SecurityScanSession ───────────┤
  SecurityScan       ────────────┤   scanner independent
  SecurityAlertGate  ────────────┘   (com.ragin.bdd.cucumber.security)
            │
            │ SecurityScanner (interface = the seam)
            ▼
  ZapSecurityScanner ────────────┐
  ZapContainer                   │   product specific
  ZapApiClient                   │   (….security.zap)
                          ───────┘

  SecurityAlert, SecurityRisk, ProxyEndpoint      (….security.models)
  SecurityScanProperties + one type per group     (….security.config)
```

| Class                                                | Responsibility                                                                    |
|------------------------------------------------------|-------------------------------------------------------------------------------------|
| `SecurityScanExtension`                              | jUnit 5 lifecycle around a test class.                                            |
| `SecurityScanSession`                                | Start, hand out the proxy, scan, gate, stop.                                      |
| `SecurityScan`                                       | Orchestration - what to attack and in which order.                                |
| `SecurityAlertGate`                                  | The verdict - ignore list, confidence threshold, risk threshold, failure message.  |
| `SecurityScanner`                                    | The seam every implementation has to satisfy.                                     |
| `ZapSecurityScanner`, `ZapContainer`, `ZapApiClient` | The only classes that know ZAP exists.                                            |

An application usually listens on more than one port (public, intranet, applications) and scanners keep a separate
tree per `host:port`, so both the scan and the alert query run **per base URL**. Filtering on a single one would
silently drop findings on the others.

## Notes and caveats

- Automated scanners produce false positives. Every finding has to be checked manually; use
  `alerts.ignored-rule-ids` for the ones you have assessed and accepted, with a comment saying why. Ignored rules are
  dropped by the gate and left out of the report (ZAP marks them as false positive through the `alertFilters` add-on,
  which `zap-stable` bundles).
- A floating image tag means two builds of the same commit can report different findings. Pin `scanner.image` when a
  run has to be reproducible.
- The scan only covers what the proxy recorded. Growing the test suite grows the attack surface.
- Testcontainers is published at the version this repository builds against. A project whose own dependency
  management pins an older Testcontainers should check that resolution, or keep the newer one.
