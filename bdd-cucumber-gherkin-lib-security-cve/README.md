# bdd-cucumber-gherkin-lib-security-cve

Gherkin sentences that scan the dependencies of the application under test for known vulnerabilities (CVEs) and
fail the build from a severity the feature file states itself.

The scan runs [Trivy](https://trivy.dev/) in a Testcontainers container against the archives on the classpath of the
test JVM - or against given archives such as the packaged application. It needs no build plugin, no SBOM and no hook:
one sentence covers it. The scanner, the gate and the configuration live in
[bdd-cucumber-gherkin-lib-security-cve-core](../bdd-cucumber-gherkin-lib-security-cve-core/README.md), which also has
the jUnit 5 extension for suites without Cucumber and the full list of properties.

It is independent of the DAST scan in `bdd-cucumber-gherkin-lib-security`; a project can use either or both.

## Prerequisites

- Docker
- Network access to the vulnerability databases, or another source for them - see
  [Where the vulnerability databases come from](#where-the-vulnerability-databases-come-from)

## Setup

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib-security-cve:${version.bdd-cucumber-gherkin-lib}"
}
```

The Spring Boot auto-configuration registers the beans. Every step is a no-op while
`cucumbertest.security.cve.enabled` is `false` (the default), so the module can stay on the test classpath of the
regular run.

**Glue** - append the CVE value to the glue the runner already declares:

```kotlin
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST +
        BddLibConfigConstants.Base.COMMA +
        BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY_CVE
)
@IncludeTags("cveScan")
class CucumberCveRunner
```

**Profile** (e.g. `application-cucumberCve.yaml`):

```yaml
cucumbertest:
  security:
    cve:
      enabled: true
      vulnerabilities:
        # the test tooling is on the classpath of the test JVM but never ships with the application
        excluded-packages:
          - "org.junit*:*"
          - "io.cucumber:*"
          - "org.testcontainers:*"
        # ignored-ids: [ "CVE-2022-42889" ]
        # ignore-unfixed: true
      report:
        # relative to the working directory of the test JVM, i.e. the module directory
        output-dir: build/reports/cve-scan
```

**Gradle task** - only when asked for by name, because it needs Docker:

```groovy
tasks.register('cucumberCve', Test) {
    useJUnitPlatform()
    include '**/*CucumberCve*'
    systemProperty 'spring.profiles.active', 'cucumberCve'
    // optional: override the report directory of the profile
    // systemProperty 'cucumbertest.security.cve.report.output-dir', rootProject.projectDir.absolutePath
    onlyIf { gradle.startParameter.taskNames.contains("cucumberCve") }
}
```

## Sentences

```gherkin
@cveScan
Feature: CVE scan

  Scenario: No dependency has a known critical vulnerability
    Then I scan the dependencies for known vulnerabilities and fail on findings of severity "CRITICAL" or higher

  Scenario: The packaged application has no known high vulnerability
    Then I scan the artifacts "build/libs" for known vulnerabilities and fail on findings of severity "HIGH" or higher
```

| Sentence                                                                                                   | Scans                                                                    |
|------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|
| `I scan the dependencies for known vulnerabilities and fail on findings of severity {string} or higher`    | every archive on the classpath of the test JVM                           |
| `I scan the artifacts {string} for known vulnerabilities and fail on findings of severity {string} or higher` | comma separated archives or directories, relative to the working directory |

Severities: `UNKNOWN`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. The threshold is part of the sentence on purpose, so no
profile can silently lower it.

## Reports

Each sentence writes a JSON and an HTML report to `cucumbertest.security.cve.report.output-dir`, named after the scan,
so both sentences in one run keep their own:

- `vulnerability-report-dependencies.html` / `.json`
- `vulnerability-report-artifacts-build-libs.html` / `.json` - the name is derived from the paths of the sentence

The HTML report shows the verdict, the counts per severity, the findings that fail the build, those below the threshold
and the ignored ones with the reason. It is one self-contained file - styles embedded, no JavaScript - so uploading or
archiving that single file is enough:

```groovy
// Jenkinsfile
post {
    always {
        publishHTML(target: [
            reportName : 'Vulnerability report',
            reportDir  : 'build/reports/cve-scan',
            reportFiles: 'vulnerability-report-dependencies.html, vulnerability-report-artifacts-build-libs.html',
            keepAll    : true,
            alwaysLinkToLastBuild: true,
            allowMissing: true
        ])
        archiveArtifacts artifacts: 'build/reports/cve-scan/*', allowEmptyArchive: true
    }
}
```

Jenkins serves published HTML with `style-src 'self'` by default, which drops embedded styles. The report stays
complete and readable without them - verdict and severities are words, the findings are plain tables. For the styled
view, allow inline styles for published reports (Script Console or `-D` on the controller):

```groovy
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP",
    "sandbox allow-same-origin; default-src 'none'; img-src 'self'; style-src 'self' 'unsafe-inline';")
```

The report needs no JavaScript, so `script-src` stays closed.

## Where the vulnerability databases come from

By default from public registries. For a build agent behind a firewall, point them at a pull-through cache
(`scanner.database.repositories`), at a daily updated storage (`scanner.database.archive`, an `http(s)` URL or a file
path), or route the downloads through a proxy (`scanner.https-proxy`). The same keys exist under
`scanner.java-database`. See [Database sources](../bdd-cucumber-gherkin-lib-security-cve-core/README.md#database-sources).

## Replacing the scanner

Define your own `VulnerabilityScanner` bean; the Trivy bean is `@ConditionalOnMissingBean`. No feature file changes.
