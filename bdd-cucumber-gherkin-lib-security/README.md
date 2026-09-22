# bdd-cucumber-gherkin-lib-security

DAST (dynamic application security testing) driven from the existing Cucumber suite.

The module starts a security scanner in a container, routes the whole BDD run through its HTTP proxy, and then attacks
the application using the recorded traffic as the seed. Findings above a configurable risk fail the build.

The current implementation is [OWASP ZAP](https://www.zaproxy.org/), but **nothing a project touches names a
product** - the profile, the tag, the properties and the Gherkin sentences are all `security*`, so replacing the
scanner costs one bean and no test changes.

## How it works

1. A `@Before` hook starts the scanner container once per JVM and exposes the host ports of the application under test
   to it (Testcontainers `exposeHostPorts`).
2. The same hook points the HTTP client of the library at the scanner's proxy, so **every** request the functional
   scenarios make is recorded.
3. The normal Cucumber scenarios run and produce the traffic.
4. A final scenario runs the active scan against that traffic, writes the report and fails on findings.

The proxy history is the attack surface: a scan can only test the endpoints the scenarios actually called. Endpoints
no scenario touches can be added by importing an OpenAPI definition (see `cucumbertest.security.api.definition-urls`).

## Prerequisites

- Docker (Testcontainers pulls the scanner image)

## How to integrate

Integration is **one test dependency plus configuration** - no Java/Kotlin code is required in the consuming project.

### 1. Gradle

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib-security:${version.bdd-cucumber-gherkin-lib}"
}
```

The beans register themselves through Spring auto-configuration
(`configuration.com.ragin.bdd.cucumber.security.SecurityScanBeanConfig`). The
`@CucumberContextConfiguration` class stays untouched.

Because every step and hook is a no-op while `cucumbertest.security.enabled` is `false` (the default), the dependency
can stay on the test classpath of the regular Cucumber run.

### 2. Cucumber glue

Add the glue of this module to the runner:

```kotlin
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST +
        BddLibConfigConstants.Base.COMMA +
        BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY
)
```

### 3. Spring profile

Create a profile for the scan (e.g. `application-cucumberSecurity.yaml`). The application under test runs in the test
JVM on the host while the scanner runs in a container, so **every URL the tests build has to use the Testcontainers
host alias** - otherwise the scanner cannot reach the application:

```yaml
cucumbertest:
    server:
        protocol: http
        host: host.testcontainers.internal
        port: ${server.port}

    security:
        enabled: true
        target:
            host: host.testcontainers.internal
            port: ${server.port}
            # every port that has to be reachable from inside the container
            exposed-ports:
                - ${server.port}
                - ${management.server.port}
        alerts:
            ignored-rule-ids:
                - "40042"   # Spring Actuator Information Leak
```

### 4. Gradle task and runner

A dedicated task and suite keep the scan out of the regular Cucumber run:

```groovy
tasks.register('cucumberSecurity', Test) {
    group 'verification'
    dependsOn assemble
    exclude '**/*Tests*'
    include '**/*CucumberSecurity*'
    systemProperty 'spring.profiles.active', 'cucumberSecurity'

    // REQUIRED. ZAP serves its API only for requests whose Host header is 'zap' (see below),
    // and both JDK HTTP clients treat Host as restricted and would silently drop it.
    systemProperty 'sun.net.http.allowRestrictedHeaders', 'true'
    systemProperty 'jdk.httpclient.allowRestrictedHeaders', 'host'

    // the test JVM runs in the module directory - write the report next to the other top level artifacts
    systemProperty 'cucumbertest.security.report.output-dir', rootProject.projectDir.absolutePath

    onlyIf("Execute only if cucumberSecurity task is called directly") {
        gradle.startParameter.taskNames.contains("cucumberSecurity")
    }
}
```

> **The two `allowRestrictedHeaders` properties are not optional.** ZAP serves the proxy and its own REST API on the
> same port and tells them apart by the `Host` header: only the reserved name `zap` reaches the API. Addressing it as
> `localhost:<mappedPort>` - all Testcontainers can offer - is interpreted as "please proxy a request to
> localhost:<mappedPort>" instead. Without these properties the JDK strips the header and every ZAP API call fails in
> a way that is hard to read.

```kotlin
@Suite
@IncludeEngines("cucumber")
@SelectPackages("cucumber")
@ConfigurationParameter(key = Constants.EXECUTION_ORDER_PROPERTY_NAME, value = "lexical")
@ExcludeTags("ignore")
@IncludeTags("securityScan")
class CucumberSecurityRunner
```

Pin the execution order to `lexical` and put the scan scenario in a directory that sorts last (e.g.
`cucumber/zzz_securityscan/`). The scan must see the traffic of all other features.

## Configuration reference

All properties are optional. Prefix: `cucumbertest.security`.

| Property                  | Default                                | Description                                                                                                                              |
|---------------------------|----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`                 | `false`                                | Master switch. While `false`, every step and hook is a no-op.                                                                            |
| `scanner.image`           | `zaproxy/zap-stable:latest`            | Scanner image. A floating tag keeps the rule set current but makes runs irreproducible - pin a version when a build has to be repeatable. |
| `scanner.startup-timeout` | `5m`                                   | Container start-up timeout.                                                                                                              |
| `scanner.plugins`         | *(empty)*                              | Scanner add-ons to install on start-up. Needs marketplace access from the build agent.                                                   |
| `target.host`             | `host.testcontainers.internal`         | How the application is reachable **from inside** the container.                                                                          |
| `target.port`             | *(the bound port)*                     | Primary port. When unset, the port the application actually bound is used.                                                               |
| `target.exposed-ports`    | *(empty)*                              | All host ports that must be reachable from the container (public, intranet, applications).                                               |
| `api.definition-urls`     | *(empty)*                              | OpenAPI definitions to import before the scan, to also attack endpoints no scenario touches.                                             |
| `scan.poll-interval`      | `10s`                                  | How often scan progress is polled.                                                                                                       |
| `scan.recurse`            | `true`                                 | Attack the whole tree below a target, not just the exact URL.                                                                            |
| `scan.in-scope-only`      | `false`                                | Also attack URLs the scanner does not consider part of a configured context.                                                             |
| `alerts.ignored-rule-ids` | *(empty)*                              | Scanner rule ids to ignore, e.g. ZAP `40042` = Spring Actuator Information Leak.                                                         |
| `alerts.min-confidence`   | `LOW`                                  | Findings below this confidence are dropped.                                                                                              |
| `report.template`         | `traditional-html`                     | Report template.                                                                                                                         |
| `report.title`            | `Security scan`                        | Report title.                                                                                                                            |
| `report.output-dir`       | `.`                                    | Directory the report is written to, absolute or relative to the working directory.                                                       |
| `report.file-name`        | `security-report.html`                 | Report file name.                                                                                                                        |
| `recording.export`        | `true`                                 | Export the recorded traffic (HAR) after the run.                                                                                         |
| `recording.export-path`   | `build/reports/security/recording.har` | Where the recording is written.                                                                                                          |
| `recording.replay-from`   | *(unset)*                              | Host path of a previously exported recording - see [Replay mode](#replay-mode).                                                          |

The **time budget** and the **risk that fails the build** are deliberately *not* properties. Both are part of the
Gherkin sentence, so a feature file states its own limits and no profile can silently weaken the gate.

Risk and confidence levels, in order: `INFORMATIONAL` < `LOW` < `MEDIUM` < `HIGH`.

## Gherkin sentences

The composite sentence covers the default case - import the optional API definitions, scan, write report and
recording, and fail on findings:

```gherkin
@securityScan
Feature: Security scan

    @securityExecuteScan
    Scenario: scan the application and fail on relevant findings
        Then I run the security scan for max. 30 minutes and fail on findings of risk "MEDIUM" or higher
```

Two tags matter:

- `@securityScan` - marks every feature that should contribute traffic; the runner includes this tag.
- `@securityExecuteScan` - marks the scan scenario itself. In replay mode every scenario *without* this tag is
  skipped.

The granular sentences exist for projects that need a different order or want to opt out of a single part:

| Sentence                                                             | Purpose                                                         |
|----------------------------------------------------------------------|-----------------------------------------------------------------|
| `I import the API definition {string} into the security scanner`     | Import one OpenAPI definition. Failures are logged and ignored. |
| `I run the security scan for max. {int} minutes`                     | Scan every target and wait for the analysis to catch up.        |
| `I ensure that no security finding has a risk of {string} or higher` | The gate on its own.                                            |
| `I store the security scan report to the file {string}`              | Write the report.                                               |
| `I export the recorded security scan traffic to the file {string}`   | Write the HAR recording.                                        |
| `I make sure that the security scanner is stopped`                   | Stop the container.                                             |

## Replay mode

A full run is slow: the functional suite has to produce the traffic before anything can be scanned. To iterate on the
scan itself, replay a recording from an earlier run:

```yaml
cucumbertest:
    security:
        recording:
            replay-from: build/reports/security/recording.har
```

Every scenario without `@securityExecuteScan` is then skipped (`TestAbortedException`), the recording is imported into
the scanner, and only the scan runs.

The recording is exported **before** the scan on purpose. At that point it holds exactly the traffic the functional
scenarios produced, which is what replaying it should reproduce. Exporting afterwards would fold the scanner's own
attack requests into the recording; replaying that both re-raises every finding the attacks provoked and makes the
next scan roughly ten times larger.

## Architecture

```
  Feature files ──@securityScan──┐
                                 │
  SecurityScanHooks  ────────────┤   scanner independent
  ThenSecurityScanGlue ──────────┤   (com.ragin.bdd.cucumber.security)
  SecurityScan       ────────────┤
  SecurityAlertGate  ────────────┘
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

| Class                                                | Responsibility                                                                               |
|------------------------------------------------------|----------------------------------------------------------------------------------------------|
| `SecurityScanHooks`                                  | Starts the scanner, exposes the host ports, wires the proxy, skips scenarios in replay mode. |
| `ThenSecurityScanGlue`                               | The Gherkin sentences. Free of any product name.                                             |
| `SecurityScan`                                       | Orchestration - what to attack and in which order.                                           |
| `SecurityAlertGate`                                  | The verdict - ignore list, confidence threshold, risk threshold, failure message.            |
| `SecurityScanner`                                    | The seam every implementation has to satisfy.                                                |
| `ZapSecurityScanner`, `ZapContainer`, `ZapApiClient` | The only classes that know ZAP exists.                                                       |

An application usually listens on more than one port (public, intranet, applications) and scanners keep a separate
tree per `host:port`, so both the scan and the alert query run **per base URL**. Filtering on a single one would
silently drop findings on the others.

## Replacing the scanner

Publish your own `SecurityScanner` bean:

```kotlin
@Bean
fun securityScanner(/* … */): SecurityScanner {
    return MyOtherScanner(/* … */)
}
```

Every ZAP bean is declared `@ConditionalOnMissingBean(SecurityScanner::class)`, so yours wins and the ZAP ones are
never created. No feature file, tag, property, sentence, Gradle task or CI change is needed.

## Notes and caveats

- Automated scanners produce false positives. Every finding has to be checked manually; use
  `cucumbertest.security.alerts.ignored-rule-ids` for the ones you have assessed and accepted, with a comment saying
  why.
- A floating image tag means two builds of the same commit can report different findings.
- The scan only covers what the proxy recorded. Growing the functional suite grows the attack surface.
