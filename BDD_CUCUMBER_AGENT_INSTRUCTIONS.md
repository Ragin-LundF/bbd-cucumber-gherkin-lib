# Agent Instructions — `bdd-cucumber-gherkin-lib`

**Audience:** an AI agent that has to write, extend or fix Cucumber/Gherkin tests for a Spring Boot
service using this library.
**Scope:** this file is self-contained. Read it and you know every step sentence, every placeholder,
every JSON-Unit matcher and every configuration knob the library offers. You do not need the wiki.

> Rule zero: **do not write custom step definitions** before you have checked the step catalogue in
> §4. Almost every REST/DB test case is already covered. Custom glue code is a last resort.

---

## 1. What this library is

A Cucumber/Gherkin **step library** for behaviour-driven testing of Spring Boot REST APIs and their
databases. It ships ready-made step definitions (`@Given`/`@When`/`@Then`), a scenario-wide state
container, and a set of [JSON-Unit](https://github.com/lukas-krecan/JsonUnit) matchers so that
responses with dynamic content (UUIDs, dates, generated ids) can be asserted without brittle
hard-coded values.

Coordinates: `io.github.ragin-lundf`.

| Module                        | Artifact                        | Use when                                        |
|-------------------------------|---------------------------------|-------------------------------------------------|
| Meta package (REST + DB)      | `bdd-cucumber-gherkin-lib`      | default; you want everything                    |
| REST only                     | `bdd-cucumber-gherkin-lib-rest` | service has no DB steps                         |
| DB only                       | `bdd-cucumber-gherkin-lib-db`   | only Liquibase/SQL/CSV steps                    |
| Core (state, matchers, utils) | `bdd-cucumber-gherkin-lib-core` | transitive; needed for custom matchers          |
| BOM                           | `bdd-cucumber-gherkin-lib-bom`  | version alignment                               |

`cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine`, `json-unit` and `json-path`
come in transitively (`api` scope). You still add `spring-boot-starter-test` (or the
`resttestclient` starter), and for DB tests a datasource + `spring-boot-starter-liquibase`.

---

## 2. Setup (skip if the project already runs Cucumber tests)

### 2.1 Dependency

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib:${bddCucumberVersion}"
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.ragin-lundf</groupId>
    <artifactId>bdd-cucumber-gherkin-lib</artifactId>
    <version>${bddCucumberVersion}</version>
    <scope>test</scope>
</dependency>
```

### 2.2 Runner

The library's glue packages must be registered explicitly. Use the constants from
`com.ragin.bdd.cucumber.constants.BddLibConfigConstants` — never hand-type the package names.

```kotlin
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST_DATABASE +
            BddLibConfigConstants.Base.COMMA +
            "com.example.myservice.cucumber.hooks"   // your own hooks/matchers package
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "json:build/reports/cucumber/cucumber.json, " +
            "html:build/reports/cucumber/cucumber.html, " +
            "junit:build/reports/cucumber/cucumber.xml"
)
@ExcludeTags("ignore")
class CucumberRunner
```

Available glue constants:

| Constant                          | Registers                                   |
|-----------------------------------|---------------------------------------------|
| `GLUE_PROPERTY_VALUES_REST`       | core hooks + REST glue                      |
| `GLUE_PROPERTY_VALUES_DATABASE`   | core hooks + DB hooks + DB glue             |
| `GLUE_PROPERTY_VALUES_REST_DATABASE` | core hooks + REST glue + DB hooks + DB glue |

### 2.3 Spring context class

One class per test module, annotated `@CucumberContextConfiguration`. It wires the Spring Boot app,
`BddJsonUtils`, the library's `@ConfigurationProperties`, a `TestRestTemplate`, and every custom
matcher / date format you add.

```kotlin
@CucumberContextConfiguration
@ContextConfiguration(
    classes = [
        Application::class,
        DatabaseExecutorService::class,          // only with the DB module
        BddJsonUtils::class,
        MyCustomMatcher::class,                  // custom JSON-Unit matchers
        MyCustomDateTimeFormatter::class         // custom date patterns
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

### 2.4 Configuration properties (`application.yml` of the test sources)

Bound by `BddProperties` (`@ConfigurationProperties(prefix = "cucumbertest")`, relaxed binding — so
`cucumberTest:` in YAML is fine).

```yaml
cucumberTest:
  authorization:
    bearer-token:
      default: "eyJhbGciOi..."      # token used by every "authorized" step
      noscope: "eyJhbGciOi..."      # see the warning below
  scenario-context:                 # pre-filled scenario context entries
    CTX_PRE_DEFINED_USER: Pre Defined
    CTX_PRE_DEFINED_FIRST_ID: abcdefg
  server:                           # optional: target an external server
    protocol: https
    host: api.example.com
    port: "443"                     # "none" = omit; empty/absent = omit
  proxy:
    host: localhost
    port: -1
  ssl:
    disableCheck: false
  databaseless: false               # true = DB steps become no-ops (DB module)
```

* If `cucumberTest.server.host` is **not** set, requests go to the locally started Spring Boot test
  server (`RANDOM_PORT`) — this is the normal case.
* ⚠️ The step `Given that a bearer token without scopes is used` is read via `@Value` and therefore
  needs the **exact camelCase key** `cucumberTest.authorization.bearerToken.noscope`. The kebab-case
  `bearer-token` form only feeds the *default* token. Without the exact key the step yields the
  literal string `none`.

### 2.5 Layout convention

* **One feature per file**, named after the feature (`user_registration.feature`). Do not collect
  unrelated behaviour in one file.
* **One directory per domain.** As soon as features belong to different domains, bounded contexts or
  API areas, give each its own directory under `features/` instead of a flat pile of files.
* **Keep the feature's resources next to it** (`requests/`, `responses/`, `scripts/`) and address
  them through a base path set in `Background:`.
* **Give every feature its own tag** so it can be selected or excluded in the runner.
* **Order-dependent features** get a numeric file prefix and stay in one directory
  (`01_create_context_state.feature`, `02_validate_context_state.feature`). Use sparingly — it is an
  anti-pattern.

```
src/test/resources/features/
├── user/
│   ├── user_registration.feature          @user_registration
│   ├── user_search.feature                @user_search
│   ├── requests/register_user.json        # request bodies
│   ├── responses/registered_user.json     # expected responses
│   └── scripts/reset_users.xml            # SQL / Liquibase / CSV
├── account/
│   ├── account_creation.feature           @account_creation
│   └── responses/account.json
└── payment/
    └── payment_execution.feature          @payment_execution
```

---

## 3. Mental model — you must understand this before writing steps

### 3.1 `ScenarioStateContext`

A single object carrying all state between steps:

| Field                   | Set by                                                    | Used by                              |
|-------------------------|-----------------------------------------------------------|--------------------------------------|
| `uriPath`               | `that the API path is` / `... call to "<uri>"`             | every request                        |
| `editableBody`          | body `Given`s and body manipulation steps                  | every non-GET request                |
| `headerValues`          | `I set the header ...`                                     | every request                        |
| `bearerToken`           | `that the user is` / `that the Bearer token is` / config    | "authorized" requests                |
| `userTokenMap`          | `that the following users and tokens are existing`          | `that the user is`                   |
| `scenarioContextMap`    | context `Given`s, `I store the string of the field ...`, config | placeholders, matchers, resolution |
| `scenarioContextFileMap`| `that the file ... is stored as ...`                        | form-data uploads                    |
| `latestResponse`        | the last executed request                                   | all `Then` assertions                |
| `fileBasePath`          | `that all file paths are relative to`                       | every file lookup                    |
| `urlBasePath`           | `that all URLs are relative to`                             | URL building                         |
| `polling`               | polling `Given`s                                            | poll requests                        |
| `executionTime`         | scenario start                                              | execution-time assertion             |

### 3.2 Lifecycle — what survives a scenario

A `@Before` hook resets part of the state before **every** scenario.

**Reset:** `latestResponse`, `editableBody`, `headerValues`, JSON compare options, `fileBasePath`,
`urlBasePath`, `bearerToken` (back to the configured default), polling config,
`scenarioContextFileMap`, `executionTime` (restarted).

**NOT reset (survives across scenarios and even across feature files):**
`scenarioContextMap`, `userTokenMap`, `uriPath`, proxy settings.

Consequences you must respect:

* `Given that all file paths are relative to "..."` and `that all URLs are relative to "..."` belong
  into a `Background:` — they are wiped per scenario.
* Values stored with `I store the string of the field ... for later usage` are visible in later
  scenarios **and later feature files** (this is what the `stateful_e2e` features exploit). It is an
  anti-pattern; use it only when Cucumber must act as an ordered test suite.
* Clean up deliberately with `Given that the stored data in the scenario context map has been reset`.
  That drops everything, including values preloaded from `cucumberTest.scenario-context` (those are
  re-inserted when the REST glue is created for the next scenario).
* Always set `that the API path is` inside the scenario that uses it — a stale `uriPath` from a
  previous scenario would otherwise be reused silently.

### 3.3 Two different placeholder syntaxes — do not mix them up

| Syntax          | Meaning                                                                 | Where                                       |
|-----------------|-------------------------------------------------------------------------|---------------------------------------------|
| `${key}`        | replaced by `scenarioContextMap["key"]` (string substitution)            | inside the **URL/API path**                 |
| `{name}`        | replaced via the `URI Elements` / `URI Values` data table                | inside the **URL/API path**                 |
| bare `key`      | many steps resolve a plain argument against the context first, and fall back to the literal value | header values, body property values, bearer token, expected field values, `URI Values`, form-data values |
| `<placeholder>` | plain Cucumber `Scenario Outline` substitution                           | anywhere in the scenario                    |

"Resolve against the context" always means: *if the string is a key of the scenario context, use its
value, otherwise use the string itself.*

The whole path is also looked up as a context key before substitution, so
`Given that the context contains the key "${URL}" with the value "/api/v1/a/b"` +
`Given that the API path is "${URL}"` works too.

### 3.4 File resolution

Files are read from the **classpath** (i.e. `src/test/resources`).

* Normal case: `fileBasePath + path` — so a base path must end with `/`
  (`Given that all file paths are relative to "features/polling/responses/"`).
* Prefix `absolutePath:` bypasses the base path and resolves from the classpath root:
  `"absolutePath:/features/body_manipulation/requests/request.json"`.

### 3.5 URL resolution

1. If the path starts with `http://` or `https://` it is used as-is (external calls are possible).
2. Otherwise `protocol://host[:port]` (from config, if set) + `urlBasePath` + `path`, with duplicate
   or missing `/` normalised.
3. `{name}` data-table elements are replaced, then `${key}` context placeholders.

### 3.6 Requests and responses

* Default headers: `Content-Type: application/json` and `Accept: application/json` unless you set
  them explicitly with `I set the header ...`.
* "authorized" step variants add `Authorization: Bearer <bearerToken>` — unless you already set an
  `Authorization` header manually, which then wins.
* A body is only attached for non-`GET` methods.
* 4xx responses are returned normally; 5xx are converted into a response entity as well. Assertions
  on error codes therefore work without special handling (see the `errors.feature` pattern).
* Request and response are attached to the Cucumber report automatically. Bodies with the subtypes
  `pdf`, `octet-stream`, `zip` are not dumped.

---

## 4. Step catalogue (complete)

`{httpMethod}` is a typed parameter and accepts exactly: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`.
Gherkin keywords are interchangeable — a step registered as `@Then` can be written after `When`,
`And` or `*`. Use the keyword that reads best.

### 4.1 Given — paths, auth, body, context, JSON tolerance, polling, dates, files

| Step | Effect |
|---|---|
| `Given that all file paths are relative to "<basePath>"` | prefix for every file argument (end it with `/`) |
| `Given that all URLs are relative to "<basePath>"` | prefix for every request URL |
| `Given that the API path is "<uri>"` | stores the URI for "previously given URI" steps |
| `Given that the following users and tokens are existing` + data table `\| user \| token \|` | fills the user→token map (token column is context-resolved) |
| `Given that the user is "<user>"` | selects that user's bearer token |
| `Given that the Bearer token is "<tokenOrContextKey>"` | sets the bearer token directly |
| `Given that a bearer token without scopes is used` | uses `cucumberTest.authorization.bearerToken.noscope` |
| `Given that a proxy with host "<host>" and port "<port>" is configured` | dynamic proxy (both args context-resolved) |
| `Given that the file "<path>" is used as the body` | request body from file |
| `Given that the body of the request is` + docstring | request body inline |
| `Given that the file "<path>" is stored as "<contextKey>"` | loads a binary file into the context (for form-data) |
| `Given that the context contains the key "<key>" with the value "<value>"` | static context entry |
| `Given that the context contains the following 'key' and 'value' pairs` + data table | multiple static context entries |
| `Given that the response JSON can contain arrays with extra elements` | JSON-Unit `IGNORING_EXTRA_ARRAY_ITEMS` |
| `Given that the response JSON can contain extra fields` | JSON-Unit `IGNORING_EXTRA_FIELDS` |
| `Given that the response JSON can contain arrays in different order` | JSON-Unit `IGNORING_ARRAY_ORDER` |
| `Given that the stored data in the scenario context map has been reset` | clears the context map |
| `Given that the request body in the scenario context map has been reset` | clears the editable body |
| `Given that a requests polls every <int> seconds` | polling interval |
| `Given that a requests polls for <int> times` | max polls (**mandatory** before any poll step) |
| `Given that a request polls every <int> seconds for <int> times` | both at once |
| `Given that a date <int> days\|months\|years in the past is stored as "<key>"` | ISO date into the context |
| `Given that a date <int> days\|months\|years in the future is stored as "<key>"` | ISO date into the context |

### 4.2 When — executing requests

All of these exist in an unauthenticated (`executing a ...`) and an authenticated
(`executing an authorized ...`) variant.

| Step | Uses |
|---|---|
| `When executing a\|an authorized {httpMethod} call to "<uri>"` | inline URI |
| `When executing a\|an authorized {httpMethod} call to "<uri>" with previously given body` | inline URI + stored body |
| `When executing a\|an authorized {httpMethod} call to "<uri>" with the body from file "<path>"` | inline URI + file body |
| `When executing a\|an authorized {httpMethod} call with previously given URI` | stored URI |
| `When executing a\|an authorized {httpMethod} call with previously given URI and body` | stored URI + stored body |
| `When executing a {httpMethod} call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'` + data table | `{name}` substitution |
| `When executing an authorized {httpMethod} call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'` + data table | `{name}` substitution |
| `When executing a {httpMethod} call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'` + data table | `{name}` substitution + stored body |
| `When executing an authorized {httpMethod} call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'` + data table | `{name}` substitution + stored body |
| `When executing a\|an authorized form-data POST call to "<uri>" with the fields` + data table | `multipart/form-data` |
| `When executing a url-encoded POST call to "<uri>" with the fields` + data table with `Key`/`Value` header | `application/x-www-form-urlencoded` |

Dynamic-URI data table format (header row is mandatory and literal):

```gherkin
Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
When executing an authorized POST call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'
  | URI Elements  | URI Values |
  | resourceId    | abc-def    |
  | subResourceId | ghi-jkl    |
```

`URI Values` are context-resolved first, so you can pass a context key instead of a literal.

Form-data data table: first column = field name, last column = value; a value that is a key of the
*file* context is uploaded as a file part.

```gherkin
Given that the file "test.txt" is stored as "FORM_FILE"
When executing a form-data POST call to "/api/v1/files" with the fields
  | identifier  | MY-ID      |
  | file        | FORM_FILE  |
  | filename    | myfile.txt |
```

### 4.3 When — polling (async)

Configure the polling first, then poll until status (and optionally body) match.

| Step |
|---|
| `executing a\|an authorized {httpMethod} poll request until the response code is <int>` |
| `executing a\|an authorized {httpMethod} poll request until the response code is <int> and the body is equal to` + docstring |
| `executing a\|an authorized {httpMethod} poll request until the response code is <int> and the body is equal to file "<path>"` |

```gherkin
Scenario: Poll until the job finished
  Given that a request polls every 1 seconds for 5 times
  And that the API path is "/api/v1/polling"
  When executing a GET poll request until the response code is 200 and the body is equal to
  """
  { "message": "SUCCESSFUL" }
  """
```

Without a configured number of polls the step fails with *"Please configure max number of polls!"*.
After the last attempt the assertion is evaluated once more, so the failure message is the real
mismatch.

### 4.4 When — request manipulation

| Step | Effect |
|---|---|
| `When I set the value of the previously given body property "<jsonPath>" to "<value>"` | edits the stored body |
| `When I set the header "<name>" to "<value>"` | sets/overwrites a header |
| `When I set the header "<name>" to "<value>" prefixed by "<prefix>"` | concatenation; both parts context-resolved |

The JSON path may be written with or without the leading `$.` (`ids[1]` == `$.ids[1]`).
The value is context-resolved, and these **magic values** are interpreted:

| Value | Result |
|---|---|
| `null` | the field is removed from the body |
| `bdd_lib_uuid` | a random UUID |
| `<n> bdd_lib_numbers` (e.g. `20 bdd_lib_numbers`) | a string of *n* digits (`1234567890` repeated) |
| anything else | context value if the string is a context key, otherwise the literal |

### 4.5 Then — response validation

| Step | Asserts |
|---|---|
| `Then I ensure that the status code of the response is <int>` | status code |
| `Then I ensure that the body of the response is equal to` + docstring | full JSON compare (JSON-Unit) |
| `Then I ensure that the body of the response is equal to the file "<path>"` | full JSON compare against a file |
| `Then I ensure that the response code is <int> and the body is equal to` + docstring | both at once |
| `Then I ensure that the response code is <int> and the body is equal to the file "<path>"` | both at once |
| `Then I ensure that the body of the response contains a field "<jsonPath>" with the value "<value>"` | single field |
| `Then I ensure that the body of the response contains the following fields and values` + data table | many fields |
| `Then I ensure, that the header "<name>" is equal to "<value>"` | response header (note the comma in the sentence) |

### 4.6 Then — context, timing

| Step | Effect |
|---|---|
| `Then I store the string of the field "<jsonPath>" in the context "<key>" for later usage` | reads via JSON path (filters like `$.objectList[?(@.first == 3)].second` work) and stores the value |
| `Then I ensure that the execution time is less than <long> ms` | ⚠️ measures the time **since the scenario started**, not the duration of the last request |
| `Then I wait for <long> ms` | sleeps |

### 4.7 Database steps (DB module)

| Step | Effect |
|---|---|
| `Given that the database was initialized with the liquibase file "<path>"` | runs a Liquibase changelog |
| `Given that the SQL statements from the SQL file "<path>" was executed` | runs raw SQL |
| `Then I ensure that the result of the query of the file "<path>" is equal to the CSV file "<path>"` | runs a query and compares the result to a CSV |

The comparison is database-agnostic: column names are upper-cased, booleans and the strings
`true`/`false` become `1`/`0`, `null`/empty values are dropped. Comparison itself runs through the
same JSON-Unit engine, so matchers work in the CSV as well.

If a classpath resource `database/reset_database.xml` exists, it is executed automatically before
every scenario. Set `cucumberTest.databaseless: true` to turn all DB steps into no-ops.

---

## 5. Writing `Then` statements for dynamic JSON (JSON-Unit)

This is the part that makes the library useful: **never assert a generated value literally.**

### 5.1 Default comparison configuration

Every full-body compare runs `JsonAssert.assertJsonEquals` with:

* numeric tolerance `0.0`,
* `Option.TREATING_NULL_AS_ABSENT` — a `null` in the actual response equals an absent field,
* all library matchers registered (§5.3),
* plus every custom matcher bean you added,
* plus the options activated by tags/steps (§5.5).

Otherwise the comparison is **strict**: same fields, same array order, no extra elements.

### 5.2 Built-in JSON-Unit placeholders

These come from JSON-Unit itself and can be used as the *expected* value of any field:

| Placeholder | Matches |
|---|---|
| `${json-unit.ignore}` | any value, including absent |
| `${json-unit.ignore-element}` | the whole element |
| `${json-unit.any-string}` | any string |
| `${json-unit.any-number}` | any number |
| `${json-unit.any-boolean}` | any boolean |
| `${json-unit.regex}<pattern>` | value matches the regex |
| `${json-unit.matches:<name>}<parameter>` | a named matcher, see below |

```gherkin
Then I ensure that the body of the response is equal to
"""
{
  "resourceId": "${json-unit.ignore}",
  "regexValue": "${json-unit.regex}[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
}
"""
```

### 5.3 Matchers shipped with the library

| Matcher | Syntax | Checks |
|---|---|---|
| Valid UUID | `${json-unit.matches:isValidUUID}` | parseable as `java.util.UUID` |
| Valid IBAN | `${json-unit.matches:isValidIBAN}` | IBAN format for ~55 countries |
| Valid date | `${json-unit.matches:isValidDate}` | ISO date, ISO date-time, `yyyy-MM-dd HH:mm:ss`, epoch millis (Long/BigDecimal) + your custom patterns |
| Date equals context | `${json-unit.matches:isDateOfContext}<contextKey>` | the value's **date part** equals the ISO date stored under that key |
| Equals context | `${json-unit.matches:isEqualToScenarioContext}<contextKey>` | value == context value |
| Not equals context | `${json-unit.matches:isNotEqualToScenarioContext}<contextKey>` | value != context value |
| Not equals literal | `${json-unit.matches:isNotEqualTo}<value>` | value != the given string |
| Contains | `${json-unit.matches:string-contains}<valueOrContextKey>` | case-insensitive substring |
| Does not contain | `${json-unit.matches:string-contains-not}<valueOrContextKey>` | case-insensitive substring absent |

Everything after the closing `}` is the matcher parameter — no quotes, no spaces around it.

**Dynamic ids without storing them:**

```gherkin
Then I ensure that the body of the response is equal to
"""
{
  "id": "${json-unit.matches:isValidUUID}",
  "createdAt": "${json-unit.matches:isValidDate}",
  "iban": "${json-unit.matches:isValidIBAN}"
}
"""
```

**Comparing against something you stored earlier:**

```gherkin
Given that the context contains the key "newUserNameInContext" with the value "Max Done"
...
Then I ensure that the body of the response is equal to
"""
{
  "newName": "${json-unit.matches:isEqualToScenarioContext}newUserNameInContext"
}
"""
```

**Relative dates without hard-coded values:**

```gherkin
Given that a date 3 days in the past is stored as "3_DAYS_IN_PAST"
When executing a GET call to "/api/v1/date/past/days/3"
Then I ensure that the body of the response is equal to
"""
{ "date": "${json-unit.matches:isDateOfContext}3_DAYS_IN_PAST" }
"""
```

**Substring checks (also against context values):**

```gherkin
Then I ensure that the body of the response is equal to
"""
{
  "myfixvalue": "${json-unit.matches:string-contains}mustbethere",
  "fromContext": "${json-unit.matches:string-contains}CONTEXT_PARAM"
}
"""
```

### 5.4 Field-level validation and its keywords

`I ensure that the body of the response contains a field ... with the value ...` (and the data-table
variant) compares a single JSON-path value as a **string**. The expected value is context-resolved
first, and understands:

| Expected value | Meaning |
|---|---|
| `<literal>` | exact string comparison of `value.toString()` |
| `<contextKey>` | compares against the stored context value |
| `@bdd_lib_not <value>` | must **not** equal that value |
| `@bdd_lib_not_exist` | the field must not exist / must be null |
| `${json-unit.matches:<name>}` | non-parameterized matcher (e.g. `isValidUUID`) |
| `@bdd_lib_not ${json-unit.matches:<name>}` | matcher must **not** match |

```gherkin
Then I ensure that the body of the response contains the following fields and values
  | string                               | is a string                         |
  | number                               | 12                                  |
  | uuid                                 | ${json-unit.matches:isValidUUID}    |
  | $.number                             | @bdd_lib_not 15                     |
  | list                                 | ["First","Second"]                  |
  | list[0]                              | First                               |
  | object.firstname                     | John                                |
  | objectList[1]                        | {first=3, second=4}                 |
  | $.objectList[?(@.first == 3)].first  | [3]                                 |
  | shouldNotExist                       | @bdd_lib_not_exist                  |
```

Notes:

* Values are stringified Java objects: a list becomes `["First","Second"]`, an object becomes
  `{firstname=John, lastname=Doe}`, a JSON-path filter result is a list (`[3]`).
* In an inline `"<value>"` argument, quotes must be escaped: `"[\"First\",\"Second\"]"`. In a data
  table they must not.
* ⚠️ **Parameterized matchers do not work here** — the parameter after the `}` is not passed to the
  matcher. Use them only in full-body compares.
* ⚠️ If your expected literal happens to be a context key, the context value wins. Pick distinctive
  context keys (uppercase with underscores is the convention in this repo).

### 5.5 Loosening the comparison

Per scenario/feature via a `Given`, or declaratively via a tag (tags can sit on a `Feature:` too):

| Tolerance | Step | Tag |
|---|---|---|
| Arrays may contain extra elements | `Given that the response JSON can contain arrays with extra elements` | `@bdd_lib_json_ignore_new_array_elements` |
| Response may contain extra fields | `Given that the response JSON can contain extra fields` | `@bdd_lib_json_ignore_extra_fields` |
| Array order irrelevant | `Given that the response JSON can contain arrays in different order` | `@bdd_lib_json_ignore_array_order` |

They combine, and they are reset after each scenario.

```gherkin
@bdd_lib_json_ignore_array_order
@bdd_lib_json_ignore_new_array_elements
Scenario: Only some elements matter
  When executing a GET call to "/api/v1/jsonWithUnsortedArray"
  Then I ensure that the response code is 200 and the body is equal to
  """
  { "unsorted": [ "First Element", "Second Element" ] }
  """
```

Prefer a targeted `${json-unit.ignore}` on the volatile field over globally ignoring extra fields.

### 5.6 Adding your own matcher

Implement `BddCucumberJsonMatcher` plus Hamcrest's `BaseMatcher`, and register the class in the
`@ContextConfiguration` of your Cucumber context class. It is then usable as
`${json-unit.matches:<matcherName>}`.

Simple matcher:

```kotlin
class SimpleCustomUUIDMatcher : BaseMatcher<String>(), BddCucumberJsonMatcher {
    override fun matcherName() = "isUUID"
    override fun matcherClass(): Class<out BaseMatcher<*>?> = this.javaClass
    override fun matches(actual: Any): Boolean =
        actual is String && actual.matches("[a-fA-F0-9]{8}-...".toRegex())
    override fun describeTo(description: Description) = Unit
}
```

Parameterized matcher (implement `ParametrizedMatcher`, the text after `}` arrives in
`setParameter`):

```kotlin
class ParameterizedCustomScenarioContextMatcher :
    BaseMatcher<Any>(), BddCucumberJsonMatcher, ParametrizedMatcher {
    private var jsonParameter: String? = null
    override fun matcherName() = "isInContextAvailable"
    override fun matcherClass(): Class<out BaseMatcher<*>?> = this.javaClass
    override fun setParameter(parameter: String?) { jsonParameter = parameter }
    override fun matches(actual: Any) =
        actual.toString() == ScenarioStateContext.current().scenarioContextMap[jsonParameter]
    override fun describeTo(description: Description) = Unit
}
```

The parameter may be JSON, which lets one matcher take several arguments:

```gherkin
"resourceId": "${json-unit.matches:containsOneOf}{\"firstArgument\": \"abc-def\", \"secondArgument\": \"ghi-jkl\"}"
```

Requirements: a public no-arg constructor (the matcher is instantiated reflectively) and a unique
`matcherName()`.

### 5.7 Adding date formats

`isValidDate` / `isDateOfContext` accept ISO date, ISO date-time, `yyyy-MM-dd HH:mm:ss` and epoch
millis out of the box. Add more by registering a `BddCucumberDateTimeFormat` bean:

```kotlin
class CustomDateTimeFormatter : BddCucumberDateTimeFormat {
    override fun formatters() = listOf(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
}
```

---

## 6. Recipes

### 6.1 Standard authorized call with a file-based expectation

```gherkin
@get_auth
Feature: Authorization with GET tests
  Background:
    Given that all file paths are relative to "features/auth_and_methods/responses/"

  Scenario: Authorized GET call is successful
    When executing an authorized GET call to "/api/v1/authorized"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "response_authorized.json"
```

### 6.2 Per-user authentication

```gherkin
Background:
  Given that the following users and tokens are existing
    | john_doe    | my_auth_token_for_john_doe    |
    | maxi_marble | my_auth_token_for_maxi_marble |

Scenario Outline: Authorized test with different users
  Given that the user is "<username>"
  When executing an authorized GET call to "/api/v1/user/<username>"
  Then I ensure that the status code of the response is 200
  And I ensure that the body of the response is equal to
  """
  { "username": "<username>", "token": "${json-unit.regex}(Bearer .*)" }
  """
  Examples:
    | username    |
    | john_doe    |
    | maxi_marble |
```

### 6.3 Chaining calls (create → reuse the generated id)

```gherkin
Scenario: Create and reuse
  Given that the API path is "/api/v1/resources"
  And that the body of the request is
  """
  { "name": "John Doe" }
  """
  When executing an authorized POST call with previously given URI and body
  Then I ensure that the status code of the response is 201
  And I store the string of the field "resourceId" in the context "createdResourceId" for later usage

Scenario: Read it back
  Given that the API path is "/api/v1/resources/${createdResourceId}"
  When executing an authorized GET call with previously given URI
  Then I ensure that the body of the response is equal to
  """
  { "resourceId": "${json-unit.matches:isEqualToScenarioContext}createdResourceId" }
  """
```

### 6.4 Building a request body from context values

```gherkin
Given that the file "requests/request.json" is used as the body
  * that the context contains the key "newUserNameInContext" with the value "Max Done"
Then I set the value of the previously given body property "name" to "newUserNameInContext"
  * I set the value of the previously given body property "$.ids[2]" to "thirdEntry"
  * I set the value of the previously given body property "obsoleteField" to "null"
When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
```

### 6.5 Database round trip

```gherkin
Background:
  Given that all file paths are relative to "features/database/"
  And that the database was initialized with the liquibase file "scripts/database_reset.xml"

Scenario: Insert a user and check the database
  Given that the body of the request is
  """
  { "userName": "mytestuser" }
  """
  When executing a POST call to "/api/v1/user/db"
  Then I ensure that the status code of the response is 200
  And I ensure that the result of the query of the file "scripts/selectuser.sql" is equal to the CSV file "scripts/expectedselect.csv"
```

### 6.6 Error codes via an outline

```gherkin
Scenario Outline: Application returns a <ErrorCode> error
  When executing a GET call to "/api/v1/error/<ErrorCode>"
  Then I ensure that the status code of the response is <ErrorCode>
  Examples:
    | ErrorCode |
    | 400       |
    | 500       |
    | 503       |
```

### 6.7 Skipping tests

Tag a `Feature:` or a single `Scenario:` with `@ignore` — the runner excludes it via
`@ExcludeTags("ignore")`.

---

## 7. Rules for the agent

1. **Reuse existing sentences.** Check §4 before writing glue code. If a sentence is genuinely
   missing and you must add one, put it in the project's own glue package, follow the existing
   phrasing style, and register the package in the runner's `GLUE_PROPERTY_NAME`.
2. **No hard-coded dynamic values.** UUIDs, timestamps, generated ids → matcher or context.
   Hard-coded "today" strings are a bug.
3. **Keep scenarios business-readable.** A scenario describes behaviour, not HTTP mechanics.
   Follow the [Cucumber anti-patterns](https://cucumber.io/docs/guides/anti-patterns/) guidance.
4. **One behaviour per scenario, one feature per file.** Group scenarios with `Rule:` when a feature
   covers several rules, and keep features of different domains in separate directories (§2.5).
5. **Prefer independent scenarios.** Cross-scenario context is possible but is explicitly an
   anti-pattern in this library. Seed data with Liquibase/SQL instead where you can.
6. **Put shared setup into `Background:`** — remember that base paths are reset per scenario.
7. **Prefer targeted tolerance** (`${json-unit.ignore}` on one field) over `@bdd_lib_json_ignore_*`
   on a whole feature.
8. **Choose distinctive context keys** (`UPPER_SNAKE_CASE`) to avoid accidental resolution of
   ordinary literals.
9. **Store request/response JSON in files** when it exceeds ~15 lines; keep inline docstrings for
   short bodies.
10. **Add new endpoints for new sentences.** In *this* repository, new sentences are proven against
    the dummy Spring Boot app in `bdd-cucumber-gherkin-lib/src/test` — add a controller there and a
    feature file that exercises the sentence.

## 8. Checklist before you finish

- [ ] Every new/changed sentence exists in §4 (or was added to glue **and** the runner glue path).
- [ ] `Background:` sets file/URL base paths that the scenarios rely on.
- [ ] No literal UUIDs, dates, or generated ids in expected bodies.
- [ ] Files referenced by the feature exist under `src/test/resources` and the base path resolves.
- [ ] Polling scenarios configure the number of polls.
- [ ] Custom matchers/date formats are registered in the `@ContextConfiguration` class.
- [ ] Parameterized matchers are only used in full-body compares, not in field validation.
- [ ] The feature has a tag, and skipped scenarios use `@ignore`.
- [ ] Tests run: `./gradlew test` (in this repo the feature files are executed by `CucumberRunner`).

## 9. Where to look in this repository

| What | Path |
|---|---|
| Given steps | `bdd-cucumber-gherkin-lib-rest/src/main/kotlin/com/ragin/bdd/cucumber/rest/glue/GivenRESTStateGlue.kt` |
| When steps (calls, polling, form-data) | `.../rest/glue/WhenRESTExecutionGlue.kt` |
| When steps (body/header manipulation) | `.../rest/glue/WhenRESTManipulationGlue.kt` |
| Then steps | `.../rest/glue/ThenRESTValidationGlue.kt` |
| Database steps | `bdd-cucumber-gherkin-lib-db/src/main/kotlin/com/ragin/bdd/cucumber/database/glue/DatabaseGlue.kt` |
| Scenario state | `bdd-cucumber-gherkin-lib-core/src/main/kotlin/com/ragin/bdd/cucumber/core/ScenarioStateContext.kt` |
| JSON assert configuration | `bdd-cucumber-gherkin-lib-core/src/main/kotlin/com/ragin/bdd/cucumber/utils/BddJsonUtils.kt` |
| Built-in matchers | `bdd-cucumber-gherkin-lib-core/src/main/kotlin/com/ragin/bdd/cucumber/matcher/` |
| Reset / tag hooks | `bdd-cucumber-gherkin-lib-core/src/main/kotlin/com/ragin/bdd/cucumber/hooks/ResetHooks.kt` |
| Runnable examples for every feature | `bdd-cucumber-gherkin-lib/src/test/resources/features/` |
| Custom matcher / date format examples | `bdd-cucumber-gherkin-lib/src/test/kotlin/com/ragin/bdd/cucumbertests/hooks/` |
| Release notes / new sentences | `CHANGELOG.md` |
