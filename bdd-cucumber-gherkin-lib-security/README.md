# bdd-cucumber-gherkin-lib-security

DAST (dynamic application security testing) driven from the existing Cucumber suite.

The module starts a security scanner in a container, routes the whole BDD run through its HTTP proxy, and then attacks
the application using the recorded traffic as the seed. Findings above a configurable risk fail the build.

The current implementation is [OWASP ZAP](https://www.zaproxy.org/), but **nothing a project touches names a
product** - the profile, the tag, the properties and the Gherkin sentences are all `security*`, so replacing the
scanner costs one bean and no test changes.

This module contributes the Cucumber surface: the Gherkin sentences, the lifecycle hooks and the Spring Boot
auto-configuration. The scanner abstraction, the scan orchestration, the finding gate, the configuration objects and
the ZAP implementation live in [bdd-cucumber-gherkin-lib-security-core](../bdd-cucumber-gherkin-lib-security-core/README.md),
which carries no Cucumber and no Spring dependency and comes in transitively - nothing to add for a Cucumber project.
A project that does not use Cucumber depends on that module directly and drives the scan from a plain jUnit suite.

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
            # finer than ignored-rule-ids: same fields as the ZAP Automation Framework 'alertFilter' job
            alert-filter:
                - rule-id: "10038"              # CSP header not set - the actuator serves no HTML
                  rule-name: Content Security Policy (CSP) Header Not Set
                  new-risk: False Positive
                  url: ".*/actuator/.*"
                  url-regex: true
                  methods: [GET]
        report:
            # relative to the working directory of the test JVM, i.e. the module directory
            output-dir: build/reports/security
            # optional: also list what the alert filters and ignored rules marked as false positive
            include-suppressed-alerts: true
            # optional, this is the default: the library's own report plus an XML for collecting the results
            templates:
                - template: bdd-modern-plus
                  file-name: security-report.html
                - template: traditional-xml
                  file-name: security-report.xml
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

    // optional: override the report directory of the profile, e.g. to collect it at the root project
    // systemProperty 'cucumbertest.security.report.output-dir', rootProject.projectDir.absolutePath

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
| `scanner.browser-enabled` | `false`                                | Let the scanner launch a headless browser for rules that need one (ZAP 40026, DOM based XSS). Off, because a REST API has no DOM.        |
| `scanner.database-recovery-log` | `false`                          | Let ZAP write the recovery log of its session database (`database.recoverylog`). Off, because the container is thrown away after the run; `true` restores ZAP's default. |
| `target.host`             | `host.testcontainers.internal`         | How the application is reachable **from inside** the container.                                                                          |
| `target.port`             | *(the bound port)*                     | Primary port. When unset, the port the application actually bound is used.                                                               |
| `target.exposed-ports`    | *(empty)*                              | All host ports that must be reachable from the container (public, intranet, applications).                                               |
| `api.definition-urls`     | *(empty)*                              | OpenAPI definitions to import before the scan, to also attack endpoints no scenario touches.                                             |
| `scan.poll-interval`      | `10s`                                  | How often scan progress is polled.                                                                                                       |
| `scan.recurse`            | `true`                                 | Attack the whole tree below a target, not just the exact URL.                                                                            |
| `scan.in-scope-only`      | `false`                                | Also attack URLs the scanner does not consider part of a configured context.                                                             |
| `alerts.ignored-rule-ids` | *(empty)*                              | Scanner rule ids to ignore, e.g. ZAP `40042` = Spring Actuator Information Leak. Dropped by the gate and left out of the report unless `report.include-suppressed-alerts` is set. |
| `alerts.alert-filter`     | *(empty)*                              | ZAP global alert filters that change the risk of matching alerts, see [Alert filters](#alert-filters).    |
| `alerts.min-confidence`   | `LOW`                                  | Findings below this confidence are dropped.                                                                                              |
| `report.title`            | `Security scan`                        | Report title.                                                                                                                            |
| `report.output-dir`       | `.`                                    | Directory the reports are written to, absolute or relative to the working directory. Set it in the profile; a system property of the same name overrides it. Without Spring, pass it to `ReportProperties`. |
| `report.templates`        | `bdd-modern-plus` → `security-report.html`, `traditional-xml` → `security-report.xml` | Every report to write, each a `template` and a `file-name`, see [Reports](#reports). |
| `report.include-suppressed-alerts` | `false`                         | Also list the alerts marked as false positive by `alerts.alert-filter` or `alerts.ignored-rule-ids`, in every report. The gate ignores them either way. |
| `recording.export`        | `true`                                 | Export the recorded traffic (HAR) after the run.                                                                                         |
| `recording.export-path`   | `build/reports/security/recording.har` | Where the recording is written.                                                                                                          |
| `recording.replay-from`   | *(unset)*                              | Host path of a previously exported recording - see [Replay mode](#replay-mode).                                                          |

### Reports

`report.templates` lists every report written after the scan; each entry is a `template` and a `file-name` below
`report.output-dir`. Any [ZAP report template](https://www.zaproxy.org/docs/desktop/addons/report-generation/templates/)
works, plus the library's own one:

- `bdd-modern-plus` - one self-contained HTML file built from ZAP's `traditional-json-plus`: counts per risk, an index
  of all alerts, and per alert the description, solution, references, CWE/WASC and every instance with its evidence,
  request and response (folded away, without JavaScript; long messages are cut at 20,000 characters).
- `traditional-xml` - for collecting the results of several projects into a management summary.
- `traditional-html-plus`, `traditional-json-plus`, `sarif-json`, ... - ZAP's own reports.

```yaml
cucumbertest:
    security:
        report:
            output-dir: build/reports/security
            templates:
                - template: bdd-modern-plus
                  file-name: security-report.html
                - template: traditional-xml
                  file-name: security-report.xml
                - template: traditional-html-plus
                  file-name: security-report-zap.html
```

A template that fails does not cost the others: every report is attempted, then the scan fails with the first error.

Suppressed alerts - marked as false positive by an `alerts.alert-filter` with `new-risk: False Positive` or by
`alerts.ignored-rule-ids` - are left out of every report by default. `report.include-suppressed-alerts: true` lists them,
so a review can see what was suppressed. An alert filter with another `new-risk` suppresses nothing: those alerts are
always reported at their new risk. ZAP's own templates then show them with the confidence `False Positive`;
`bdd-modern-plus` puts them into a section *Suppressed alerts* after the findings and leaves them out of
the counts per risk. The gate is not affected: it drops false positives either way.

For Jenkins, publish the HTML and archive the rest:

```groovy
// Jenkinsfile
post {
    always {
        publishHTML(target: [
            reportName : 'Security scan',
            reportDir  : 'build/reports/security',
            reportFiles: 'security-report.html',
            keepAll    : true,
            alwaysLinkToLastBuild: true,
            allowMissing: true
        ])
        archiveArtifacts artifacts: 'build/reports/security/*', allowEmptyArchive: true
    }
}
```

Jenkins serves published HTML with `style-src 'self'` by default, which drops embedded styles. The `bdd-modern-plus`
report stays complete and readable without them - risks are words, the findings are plain tables and lists. For the
styled view, allow inline styles for published reports (Script Console or `-D` on the controller):

```groovy
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP",
    "sandbox allow-same-origin; default-src 'none'; img-src 'self'; style-src 'self' 'unsafe-inline';")
```

The report needs no JavaScript, so `script-src` stays closed.

### Alert filters

`alerts.alert-filter` is a list of ZAP alert filters. The fields are the ones of an `alertFilters` entry of the
[ZAP Automation Framework `alertFilter` job](https://www.zaproxy.org/docs/desktop/addons/alert-filters/automation/),
written in kebab-case, so a filter from a ZAP plan can be copied over:

| Field             | Description                                                                                  |
|-------------------|----------------------------------------------------------------------------------------------|
| `rule-id`         | Mandatory, the scan rule id or the alert reference.                                          |
| `rule-name`       | Optional, the name of the rule. Only used in the log.                                        |
| `new-risk`        | `False Positive` *(default)*, `Info`, `Low`, `Medium` or `High`.                              |
| `url`             | Optional string to match against the alert url.                                              |
| `url-regex`       | `true` if `url` is a regex.                                                                  |
| `parameter`       | Optional string to match against the alert parameter.                                        |
| `parameter-regex` | `true` if `parameter` is a regex.                                                            |
| `attack`          | Optional string to match against the alert attack.                                           |
| `attack-regex`    | `true` if `attack` is a regex.                                                               |
| `evidence`        | Optional string to match against the alert evidence.                                         |
| `evidence-regex`  | `true` if `evidence` is a regex.                                                             |
| `methods`         | Optional list of HTTP methods.                                                               |

`context` is not supported: the scan creates no ZAP context, so every filter is global. The filters are registered
right after ZAP started, before any traffic. The gate reads the alerts back from ZAP and therefore sees the new risk;
a `False Positive` alert is dropped as long as `alerts.min-confidence` is above `INFORMATIONAL`, and appears in the
reports only with `report.include-suppressed-alerts: true`. A filter that ZAP
rejects fails the start of the scan, because a silently missing filter would change the verdict.

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
| `I store the security scan reports to the directory {string}`                         | Write every report of `report.templates` into the directory. |
| `I store the security scan report to the file {string}`                                | Write the first report of `report.templates`.                |
| `I store the security scan report with template {string} to the file {string}`         | Write one report with any template.                          |
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
                                 │   this module
  SecurityScanHooks  ────────────┤   (….security.hooks / .glue)
  ThenSecurityScanGlue ──────────┘
            │
            ▼
  SecurityScanSession ───────────┐
  SecurityScan       ────────────┤   scanner independent
  SecurityAlertGate  ────────────┘   (com.ragin.bdd.cucumber.security, security-core)
            │
            │ SecurityScanner (interface = the seam)
            ▼
  ZapSecurityScanner ────────────┐
  ZapContainer                   │   product specific
  ZapApiClient                   │   (….security.zap, security-core)
                          ───────┘

  SecurityAlert, SecurityRisk, ProxyEndpoint      (….security.models, security-core)
  SecurityScanProperties + one type per group     (….security.config, security-core)
  SecurityScanExtension                           (….security.junit, security-core)
```

| Class                                                | Responsibility                                                                               |
|------------------------------------------------------|----------------------------------------------------------------------------------------------|
| `SecurityScanHooks`                                  | Starts the scanner, exposes the host ports, wires the proxy, skips scenarios in replay mode. |
| `ThenSecurityScanGlue`                               | The Gherkin sentences. Free of any product name.                                             |
| `SecurityScanSession`                                | The lifecycle - start, hand out the proxy, scan, gate, stop. Shared with the jUnit entry point. |
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

The ZAP bean is declared `@ConditionalOnMissingBean(SecurityScanner::class)`, so yours wins and ZAP is never
started. No feature file, tag, property, sentence, Gradle task or CI change is needed.

## Notes and caveats

- Automated scanners produce false positives. Every finding has to be checked manually; use
  `cucumbertest.security.alerts.ignored-rule-ids` or a narrower `alerts.alert-filter` for the ones you have assessed
  and accepted, with a comment saying why.
- A floating image tag means two builds of the same commit can report different findings.
- The scan only covers what the proxy recorded. Growing the functional suite grows the attack surface.
