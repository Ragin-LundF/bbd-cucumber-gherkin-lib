---
name: bdd-cucumber-gherkin
description: Write, extend and fix Cucumber/Gherkin tests for Spring Boot REST APIs and databases with the bdd-cucumber-gherkin-lib step library. Use when creating or changing .feature files, asserting JSON responses with dynamic values (UUIDs, dates, generated ids), configuring authentication/polling/form-data/database steps, adding custom JSON-Unit matchers, or setting up the DAST security scan that routes the suite through a scanner proxy. Triggers: "write a cucumber test", "add a feature file", "gherkin step", "json-unit matcher", "bdd-cucumber-gherkin-lib", "security scan", "DAST", "bdd-cucumber-gherkin-lib-security".
---

# Skill: BDD Cucumber Gherkin Tests

Use this skill when writing or changing Gherkin `.feature` files (or their glue/configuration) for a
project that uses `io.github.ragin-lundf:bdd-cucumber-gherkin-lib`.

The library ships ready-made step definitions for REST calls, authentication, request/response
manipulation, polling, form-data uploads, database setup and JSON assertions, plus a scenario-wide
state container and a set of [JSON-Unit](https://github.com/lukas-krecan/JsonUnit) matchers for
dynamic values. An optional module (`bdd-cucumber-gherkin-lib-security`) turns the same suite into a
DAST security scan. This skill is self-contained: everything needed to write a working feature file
is below.

Optional extras, if more information is needed:

- [BDD_CUCUMBER_AGENT_INSTRUCTIONS.md](https://raw.githubusercontent.com/Ragin-LundF/bbd-cucumber-gherkin-lib/refs/heads/main/BDD_CUCUMBER_AGENT_INSTRUCTIONS.md) — the long-form reference with the full sentence catalogue.
- [bdd-cucumber-gherkin-lib-security/README.md](https://raw.githubusercontent.com/Ragin-LundF/bbd-cucumber-gherkin-lib/refs/heads/main/bdd-cucumber-gherkin-lib-security/README.md) — the security scan module in full (architecture, replay mode, replacing the scanner).

## Non-negotiable rules

1. **Never write custom step definitions before checking the catalogue below.** The library covers
   almost every REST/DB case. New glue is a last resort, and its package must be added to the
   runner's `GLUE_PROPERTY_NAME`.
2. **Never assert a dynamic value literally.** UUIDs, timestamps and generated ids are asserted with
   a JSON-Unit matcher or against the scenario context.
3. **Prefer targeted tolerance.** `${json-unit.ignore}` on one field beats
   `@bdd_lib_json_ignore_extra_fields` on a whole feature.
4. **Prefer independent scenarios.** Cross-scenario context is supported but is an anti-pattern —
   seed data with Liquibase/SQL where possible.
5. **Never weaken an assertion to make a test pass.** Fix the expectation or the code.
6. **Never weaken the security gate.** Do not lower the risk in the scan sentence and do not ignore
   a scanner rule id without a comment saying why the finding was assessed and accepted.

## Feature file organisation

* **One feature per file.** A `Feature:` describes one capability; the file is named after it
  (`user_registration.feature`). Do not collect unrelated behaviour in a single file, and do not
  split one capability across several files unless there is an ordering reason (see the stateful
  pattern below).
* **One directory per domain.** As soon as features belong to different domains, bounded contexts or
  API areas, give each its own directory under `features/` instead of a flat pile of files. The
  directory name is the domain (`user/`, `account/`, `payment/`), and everything belonging to it —
  request bodies, expected responses, SQL/CSV/Liquibase scripts — lives next to it.
* **Keep test resources local to the feature** (`requests/`, `responses/`, `scripts/`) and address
  them with a `Background:` base path so scenarios stay short.
* **Give every feature its own tag** (`@user_registration`) so it can be selected or excluded in the
  runner.
* **Order-dependent features:** if a feature really must run after another, prefix the file names
  (`01_create_context_state.feature`, `02_validate_context_state.feature`) and keep them in the same
  directory. Use this sparingly — it is an anti-pattern.

```
src/test/resources/features/
├── user/
│   ├── user_registration.feature          @user_registration
│   ├── user_search.feature                @user_search
│   ├── requests/register_user.json
│   ├── responses/registered_user.json
│   └── scripts/reset_users.xml
├── account/
│   ├── account_creation.feature           @account_creation
│   └── responses/account.json
└── payment/
    └── payment_execution.feature          @payment_execution
```

## Workflow

1. Decide where the feature belongs — existing domain directory, or a new one.
2. State the behaviour as one `Scenario:` per case; group related ones with `Rule:`.
3. Put shared setup into `Background:` — base paths are reset before every scenario:
   `Given that all file paths are relative to "features/<domain>/"`,
   `Given that all URLs are relative to "<basePath>"`.
4. Pick the request step from the cheat sheet. Use the `authorized` variant when a bearer token is
   needed.
5. Build the body inline (docstring), from a file, or by manipulating a stored body.
6. Assert status code and body. Replace every dynamic value with a matcher.
7. Move bodies longer than ~15 lines into `requests/` / `responses/` JSON files.
8. If a sentence really is missing: add the glue in the project's own package, register that package
   in the runner, prove it against a test endpoint, and add a feature file that exercises it.
9. Run the tests (`./gradlew test`) and report real results.

## Setup (only if the project does not run Cucumber yet)

Dependency (`bdd-cucumber-gherkin-lib` = REST + DB; `-rest` / `-db` for single modules; `-bom` for
version alignment; `-security` for the DAST scan, which is not part of the bundle and is added
separately). `cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine`, `json-unit` and
`json-path` come in transitively.

```groovy
testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib:${bddCucumberVersion}"
```

Runner — the library glue packages must be registered via `BddLibConfigConstants`
(`GLUE_PROPERTY_VALUES_REST`, `GLUE_PROPERTY_VALUES_DATABASE`, `GLUE_PROPERTY_VALUES_REST_DATABASE`,
plus `GLUE_PROPERTY_VALUES_SECURITY` appended for the scan runner):

```kotlin
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST_DATABASE +
            BddLibConfigConstants.Base.COMMA +
            "com.example.myservice.cucumber.hooks"   // own hooks/matchers package
)
@ExcludeTags("ignore")
class CucumberRunner
```

Spring context class — one per test module:

```kotlin
@CucumberContextConfiguration
@ContextConfiguration(
    classes = [
        Application::class,
        DatabaseExecutorService::class,   // only with the DB module
        BddJsonUtils::class,
        MyCustomMatcher::class,           // custom matchers / date formats
    ],
    loader = SpringBootContextLoader::class
)
@ConfigurationPropertiesScan("com.ragin.bdd.cucumber.config")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"]
)
@AutoConfigureTestRestTemplate
class CreateContextHooks {
    @Before
    fun springDummyForConfiguration() = Unit
}
```

Configuration (`application.yml` of the test sources, prefix `cucumberTest`, relaxed binding):

```yaml
cucumberTest:
  authorization:
    bearer-token:
      default: "<jwt>"        # token used by every "authorized" step
      # the "without scopes" step is read via @Value and needs the exact camelCase key:
      # cucumberTest.authorization.bearerToken.noscope
  scenario-context:           # pre-filled context entries
    CTX_PRE_DEFINED_USER: Pre Defined
  server:                     # omit to call the locally started test server (RANDOM_PORT)
    protocol: https
    host: api.example.com
    port: "443"
  services:                   # optional; every web server Spring starts is found without this
    management:
      port: "${local.management.port}"
      path-prefixes: ["/actuator"]
  proxy: { host: localhost, port: -1 }
  ssl: { disableCheck: false }
  logging:                    # what is reported about a call
    request-body: true
    response-body: true
    headers: false            # credentials are obfuscated when switched on
    sql: false
    pretty-json: true
    max-body-length: 8192
  databaseless: false         # true = DB steps become no-ops
```

### Output while a test runs

The console gets one line per call with no setup (`→ GET /api/v1/users`, `← 200 OK (23 ms)`);
silence it with `logging.level.com.ragin.bdd.cucumber: WARN`. Append
`BddLibConfigConstants.Plugin.PLUGIN_PROPERTY_VALUES_DEFAULT` to the runner's plugin list for the
scenario and step tree, and set Gradle's `testLogging { showStandardStreams = true }`.

The report gets the same lines plus collapsed, titled blocks for request/response bodies, headers,
SQL, query results and expected-vs-actual. Credentials are obfuscated everywhere, including inside
a body an API echoed back.

## Cheat sheet

### State model

One `ScenarioStateContext` carries API path, body, headers, bearer token, context map and the last
response.

**Reset before each scenario:** response, body, headers, JSON tolerance options, file/URL base
paths, selected service, bearer token (back to the configured default), polling config, file
context, execution timer.
**Survives scenarios and feature files:** `scenarioContextMap`, user/token map, `uriPath`, proxy.
→ Set the API path inside the scenario; set base paths in `Background:`.

### Placeholders and resolution

| Syntax | Meaning |
|---|---|
| `${key}` | context value, substituted inside the URL/API path |
| `{name}` | replaced via the `URI Elements` / `URI Values` data table |
| bare `key` | most step arguments resolve against the context first, else the literal is used |
| `<placeholder>` | plain `Scenario Outline` substitution |
| `absolutePath:` | file path prefix that bypasses the file base path (classpath root) |

Files are read from the classpath (`src/test/resources`); the base path is a plain prefix, so it must
end with `/`. URLs starting with `http://`/`https://` are called as-is; otherwise the target is
resolved in this order: the service selected by `Given that the service "<name>" is used`, then the
service whose longest `path-prefixes` entry matches the path, then `cucumberTest.server.*`, then the
path stays relative and the `TestRestTemplate` resolves it against the application under test. The
URL is `protocol://host[:port]` + service base path + URL base path + path.

Every web server the Spring context starts is discovered under the name Spring uses for it, so
`/actuator/health` reaches the management port with no configuration. Feature files never contain a
protocol, host or port.

Requests default to `Content-Type`/`Accept: application/json`; the `authorized` variants add
`Authorization: Bearer <token>` unless an `Authorization` header was set manually. Bodies are only
sent for non-`GET` methods. 4xx and 5xx responses are captured normally, so error codes can be
asserted directly.

### Given

```gherkin
Given that all file paths are relative to "features/user/"
Given that all URLs are relative to "/api"
Given that the service "management" is used                     # only for a second port
Given that the API path is "/api/v1/{resourceId}"
Given that the following users and tokens are existing        # data table: user | token
Given that the user is "john_doe"
Given that the Bearer token is "<tokenOrContextKey>"
Given that a bearer token without scopes is used
Given that the body of the request is                          # docstring
Given that the file "requests/request.json" is used as the body
Given that the file "test.txt" is stored as "FORM_FILE"
Given that the context contains the key "K" with the value "V"
Given that the context contains the following 'key' and 'value' pairs   # data table
Given that the response JSON can contain extra fields
Given that the response JSON can contain arrays with extra elements
Given that the response JSON can contain arrays in different order
Given that the stored data in the scenario context map has been reset
Given that the request body in the scenario context map has been reset
Given that a requests polls every 1 seconds
Given that a requests polls for 5 times
Given that a request polls every 1 seconds for 5 times
Given that a date 3 days|months|years in the past|future is stored as "KEY"
Given that a proxy with host "h" and port "p" is configured
```

### When (methods: GET, POST, PUT, PATCH, DELETE — each with an `authorized` variant)

```gherkin
When executing a|an authorized GET call to "/api/v1/x"
When executing a|an authorized POST call to "/api/v1/x" with previously given body
When executing a|an authorized POST call to "/api/v1/x" with the body from file "requests/r.json"
When executing a|an authorized GET call with previously given URI
When executing a|an authorized POST call with previously given URI and body
When executing a POST call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'
When executing an authorized POST call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'
When executing a|an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
  | URI Elements | URI Values |
  | resourceId   | abc-def    |
When executing a|an authorized form-data POST call to "/api/v1/files" with the fields   # name | value
When executing a url-encoded POST call to "/api/v1/x" with the fields                   # Key | Value header
When executing a|an authorized GET poll request until the response code is 200
When executing a|an authorized GET poll request until the response code is 200 and the body is equal to [file "expected.json"]
When I set the header "X-H" to "V" [prefixed by "PRE_"]
When I set the value of the previously given body property "ids[1]" to "V"
```

The `URI Elements` / `URI Values` header row is literal; values are context-resolved first.
Form-data: first column = field name, last = value; a value that is a key of the file context is
uploaded as a file part. Polling requires a configured number of polls, otherwise the step fails.
Body-property magic values: `null` removes the field, `bdd_lib_uuid` inserts a random UUID,
`20 bdd_lib_numbers` inserts 20 digits; anything else is context-resolved. JSON paths work with and
without the leading `$.`.

### Then

```gherkin
Then I ensure that the status code of the response is 200
Then I ensure that the body of the response is equal to                       # docstring
Then I ensure that the body of the response is equal to the file "responses/r.json"
Then I ensure that the response code is 201 and the body is equal to [the file "..."]
Then I ensure that the body of the response contains a field "$.a.b" with the value "V"
Then I ensure that the body of the response contains the following fields and values   # data table
Then I ensure, that the header "X-TEST-HEADER" is equal to "present"
Then I store the string of the field "id" in the context "createdId" for later usage
Then I ensure that the execution time is less than 600 ms      # measured from scenario start
Then I wait for 1000 ms
```

Database module:

```gherkin
Given that the database was initialized with the liquibase file "scripts/reset.xml"
Given that the SQL statements from the SQL file "scripts/insert.sql" was executed
Then I ensure that the result of the query of the file "scripts/select.sql" is equal to the CSV file "scripts/expected.csv"
```

The CSV comparison is database-agnostic (column names upper-cased, booleans normalised to `1`/`0`,
null/empty dropped). A classpath resource `database/reset_database.xml` is executed automatically
before every scenario if it exists.

Gherkin keywords are interchangeable — a sentence registered as `@Then` may be written after `When`,
`And` or `*`. Use what reads best.

### Dynamic JSON in `Then` (the core of this skill)

Comparison defaults: strict field and array match, numeric tolerance `0.0`, `null` treated as
absent.

JSON-Unit built-ins: `${json-unit.ignore}`, `${json-unit.ignore-element}`, `${json-unit.any-string}`,
`${json-unit.any-number}`, `${json-unit.any-boolean}`, `${json-unit.regex}<pattern>`,
`${json-unit.matches:<name>}<parameter>`.

Library matchers:

| Matcher | Use |
|---|---|
| `${json-unit.matches:isValidUUID}` | generated ids |
| `${json-unit.matches:isValidDate}` | ISO date/date-time, `yyyy-MM-dd HH:mm:ss`, epoch millis, custom patterns |
| `${json-unit.matches:isValidIBAN}` | IBAN format |
| `${json-unit.matches:isDateOfContext}KEY` | date part equals a stored relative date |
| `${json-unit.matches:isEqualToScenarioContext}KEY` | equals a stored value |
| `${json-unit.matches:isNotEqualToScenarioContext}KEY` | differs from a stored value |
| `${json-unit.matches:isNotEqualTo}value` | differs from a literal |
| `${json-unit.matches:string-contains}valueOrKey` | case-insensitive substring |
| `${json-unit.matches:string-contains-not}valueOrKey` | substring absent |

Everything after the closing `}` is the matcher parameter — no quotes, no spaces.

```gherkin
Given that a date 3 days in the past is stored as "3_DAYS_IN_PAST"
When executing an authorized POST call with previously given URI and body
Then I ensure that the response code is 201 and the body is equal to
"""
{
  "id": "${json-unit.matches:isValidUUID}",
  "createdAt": "${json-unit.matches:isValidDate}",
  "validFrom": "${json-unit.matches:isDateOfContext}3_DAYS_IN_PAST",
  "name": "${json-unit.matches:isEqualToScenarioContext}expectedName",
  "volatile": "${json-unit.ignore}"
}
"""
```

Field-level keywords (for `contains a field ... with the value ...` and its data-table variant):
`@bdd_lib_not <value>` (must differ), `@bdd_lib_not_exist` (field must be absent), and
non-parameterized matchers. Values are compared as strings: a list reads `["First","Second"]`, an
object reads `{firstname=John, lastname=Doe}`; inline arguments need escaped quotes, data-table
cells do not. **Parameterized matchers do not work in field-level validation** — the parameter is
not passed to the matcher; use a full-body compare instead.

Tolerance can also be switched on per scenario or feature with tags:
`@bdd_lib_json_ignore_extra_fields`, `@bdd_lib_json_ignore_new_array_elements`,
`@bdd_lib_json_ignore_array_order`. Skip a feature or scenario with `@ignore`.

### Custom matchers and date formats

Implement `BddCucumberJsonMatcher` + Hamcrest `BaseMatcher` (add `ParametrizedMatcher` to receive the
text after the `}`), give it a unique `matcherName()` and a public no-arg constructor, then register
the class in the `@ContextConfiguration` of the `@CucumberContextConfiguration` class:

```kotlin
class SimpleCustomUUIDMatcher : BaseMatcher<String>(), BddCucumberJsonMatcher {
    override fun matcherName() = "isUUID"
    override fun matcherClass(): Class<out BaseMatcher<*>?> = this.javaClass
    override fun matches(actual: Any) = actual is String && actual.matches(UUID_REGEX)
    override fun describeTo(description: Description) = Unit
}
```

The parameter may be JSON, which allows several arguments in one matcher:
`"${json-unit.matches:containsOneOf}{\"firstArgument\": \"a\", \"secondArgument\": \"b\"}"`.

Extra date patterns for `isValidDate` / `isDateOfContext`: register a `BddCucumberDateTimeFormat`
bean returning the additional `DateTimeFormatter`s.

## Security scan (DAST, optional module)

`bdd-cucumber-gherkin-lib-security` needs Docker and is **not** part of the
`bdd-cucumber-gherkin-lib` bundle. A `@Before` hook starts a scanner container, exposes the host
ports of the application under test to it and routes every request of the run through the scanner's
proxy; a final scenario attacks the recorded traffic, writes a report and fails the build on
findings. The recorded traffic *is* the attack surface — an endpoint no scenario calls is only
scanned when its OpenAPI definition is imported. Nothing a project writes names the scanner
(currently OWASP ZAP): tags, properties and sentences are all `security*`. The scanner, the
orchestration and the configuration types live in `bdd-cucumber-gherkin-lib-security-core`, which
comes in transitively and needs neither Cucumber nor Spring — a suite without Cucumber depends on
that module directly.

Integration is one dependency plus configuration — no glue code, and the Spring context class stays
untouched (the beans come from Spring auto-configuration):

```groovy
testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib-security:${bddCucumberVersion}"
```

```kotlin
// runner glue: append the security value to the one already in use
value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST +
    BddLibConfigConstants.Base.COMMA +
    BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY
```

The scan profile (e.g. `application-cucumberSecurity.yaml`) — the application runs in the test JVM
on the host, the scanner in a container, so **every URL and target has to use the Testcontainers
host alias**:

```yaml
cucumbertest:
  server: { protocol: http, host: host.testcontainers.internal, port: "${server.port}" }
  security:
    enabled: true                     # false (the default) = every hook and step is a no-op
    target:
      host: host.testcontainers.internal
      exposed-ports: ["${server.port}", "${management.server.port}"]
    alerts:
      ignored-rule-ids: ["40042"]     # assessed and accepted, with a comment saying why
```

A dedicated Gradle task and runner keep the scan out of the regular run:

```groovy
tasks.register('cucumberSecurity', Test) {
    include '**/*CucumberSecurity*'
    systemProperty 'spring.profiles.active', 'cucumberSecurity'
    // REQUIRED — ZAP separates proxy and API by the Host header, which both JDK clients drop
    systemProperty 'sun.net.http.allowRestrictedHeaders', 'true'
    systemProperty 'jdk.httpclient.allowRestrictedHeaders', 'host'
    systemProperty 'cucumbertest.security.report.output-dir', rootProject.projectDir.absolutePath
}
```

```kotlin
@Suite @IncludeEngines("cucumber") @SelectPackages("cucumber")
@ConfigurationParameter(key = Constants.EXECUTION_ORDER_PROPERTY_NAME, value = "lexical")
@ExcludeTags("ignore") @IncludeTags("securityScan")
class CucumberSecurityRunner
```

Because the order is `lexical` and the scan must see the traffic of every other feature, the scan
feature lives in a directory that sorts last (`cucumber/zzz_securityscan/`). `@securityScan` marks
every feature that contributes traffic; `@securityExecuteScan` marks the scan scenario itself (in
replay mode every scenario without it is skipped).

```gherkin
@securityScan
Feature: Security scan

  @securityExecuteScan
  Scenario: scan the application and fail on relevant findings
    Then I run the security scan for max. 30 minutes and fail on findings of risk "MEDIUM" or higher
```

That one sentence exports the recording, imports the configured API definitions, scans every target,
waits for the analysis, writes the report, gates on the findings and stops the scanner. Granular
alternatives, for a different order or to opt out of a part:

```gherkin
Then I import the API definition "https://host/openapi.json" into the security scanner
Then I run the security scan for max. 30 minutes
Then I ensure that no security finding has a risk of "MEDIUM" or higher
Then I store the security scan report to the file "build/reports/security/report.html"
Then I export the recorded security scan traffic to the file "build/reports/security/recording.har"
Then I make sure that the security scanner is stopped
```

Risk and confidence scale: `INFORMATIONAL` < `LOW` < `MEDIUM` < `HIGH`. The time budget and the
failing risk are sentence parameters on purpose, so a profile can never weaken the gate; the budget
covers the whole run and is shared by all target ports.

Properties (prefix `cucumbertest.security`, all optional, defaults in brackets): `enabled`
[`false`], `scanner.image` [`zaproxy/zap-stable:latest`; pin it when a build must be reproducible],
`scanner.startup-timeout` [`5m`], `scanner.plugins`, `target.host`
[`host.testcontainers.internal`], `target.port` [the bound port], `target.exposed-ports`,
`api.definition-urls`, `scan.poll-interval` [`10s`], `scan.recurse` [`true`], `scan.in-scope-only`
[`false`], `alerts.ignored-rule-ids`, `alerts.min-confidence` [`LOW`], `report.template`
[`traditional-html`], `report.title`, `report.output-dir` [`.`], `report.file-name`
[`security-report.html`], `recording.export` [`true`], `recording.export-path`
[`build/reports/security/recording.har`], `recording.replay-from`.

To iterate on the scan itself, set `recording.replay-from` to a HAR of an earlier run: the
functional scenarios are skipped and only the scan runs. The recording is exported *before* the
scan on purpose — afterwards it would also contain the scanner's own attack requests.

Automated scanners produce false positives. Check every finding manually and put the accepted ones
into `alerts.ignored-rule-ids` with a comment; never lower the risk threshold to get a green build.

## Output expectations

- One feature per file, grouped in a domain directory with its own resources and tag.
- Feature files read as behaviour, not as HTTP scripting; one behaviour per scenario.
- No literal UUIDs, dates or generated ids in expected bodies.
- Referenced JSON/SQL/CSV files exist and resolve against the configured base path.
- Polling scenarios configure the number of polls; custom matchers are registered in the context
  class.
- A security scan feature is tagged `@securityScan`, its scan scenario `@securityExecuteScan`, the
  feature file sorts last, and the risk threshold is never lowered to make a build pass.
- Test results are reported honestly, including failures.
