# Module Architecture

Use this file for module boundaries, package layout and cross-module changes.

---

## Goal

Consistent layered architecture so developers can switch between modules without relearning structure.
Each technical domain is a separate Gradle subproject with explicit dependencies.

---

## Module overview

| Module | Artifact | Purpose |
|---|---|---|
| `bdd-cucumber-gherkin-lib-core` | published | Shared scenario state, base class for all glue, matchers, hooks, utilities |
| `bdd-cucumber-gherkin-lib-rest` | published | REST Gherkin step definitions (Given/When/Then), HTTP client, URL utilities |
| `bdd-cucumber-gherkin-lib-db` | published | Database Gherkin step definitions, Liquibase integration, CSV comparison |
| `bdd-cucumber-gherkin-lib-bom` | published | Bill of Materials for consumer dependency management |
| `bdd-cucumber-gherkin-lib` | published | Convenience aggregator — pulls `core`, `rest`, and `db` as transitive `api` dependencies; consumers get everything with one dependency. Also hosts the integration test suite (demo app, Cucumber runner, Konsist architecture tests) in `src/test/`. |

---

## Dependency graph

```
rest ──► core
db   ──► core
bdd-cucumber-gherkin-lib ──► rest, db, core
```

Rules:
- `core` has no runtime dependency on `rest` or `db`. Never add one.
- `rest` and `db` are independent of each other. Neither imports from the other.
- `bdd-cucumber-gherkin-lib` is the only module that wires all three together (as `api` dependencies).

---

## Package layout

### `bdd-cucumber-gherkin-lib-core`

```
com.ragin.bdd.cucumber
  constants/    — library-wide and configuration-related constant values
  config/       — Spring @ConfigurationProperties binding (prefix "cucumbertest")
  core/         — shared scenario state singleton and base class for all glue
  datetimeformat/ — date/time format support used in step definitions
  hooks/        — Cucumber lifecycle hooks for scenario reset and logging
  matcher/      — custom JSON matcher interface and built-in implementations
  utils/        — shared utilities for JSON, HTTP headers, date handling, file loading
```

### `bdd-cucumber-gherkin-lib-rest`

```
com.ragin.bdd.cucumber.rest
  constants/    — REST-specific constants (URL patterns, header names, placeholders)
  extensions/   — Kotlin extension functions adapting Cucumber types for REST use
  glue/         — REST step definitions (Given/When/Then) and abstract HTTP execution base
  httpclient/   — HTTP client factory and configuration
  utils/        — URL construction, service/web-server resolution, path placeholder replacement,
                  request logging
```

#### Multi-port resolution (`rest`)

`ServiceUrlResolver` enumerates every web server of the application under test by scanning the
Spring `Environment` for the `local.<namespace>.port` properties that Spring Boot registers for
each of them. The namespace is the service name (`server`, `management`, …). Discovered services
are merged field by field with `cucumbertest.services.<name>`, configuration winning.

`BaseRESTExecutionGlue.targetUrlFor` resolves the request URL in this order:

1. path already absolute (`http://`, `https://`) — used as-is
2. `ScenarioStateContext.serviceName` — fails fast when the name is unknown
3. the service with the longest matching `pathPrefixes` entry
4. `cucumbertest.server.*`
5. nothing — the path stays relative and the `TestRestTemplate` resolves it

Steps 4 and 5 are the pre-existing behaviour and must stay reachable. In particular the `server`
service gets **no** default path prefix, because a prefix there would divert every call away from
step 5. Resolution happens outside the `runCatching` of the request so that a configuration error
surfaces instead of becoming a missing response.

### `bdd-cucumber-gherkin-lib-db`

```
com.ragin.bdd.cucumber.database
  executor/     — interface and implementation for executing SQL and Liquibase scripts
  glue/         — database step definitions (Given/Then)
  hooks/        — Cucumber lifecycle hook for database reset before each scenario
configuration/com.ragin.bdd.cucumber.database/
              — Spring bean configuration for the database executor
```

### `bdd-cucumber-gherkin-lib` (aggregator + integration test module)

This module has no production sources in `src/main/`. Its published artifact is a convenience
aggregator: it declares `core`, `rest`, and `db` as `api` dependencies, so a consumer who adds
this single artifact gets all three modules transitively. This is the recommended entry point for
projects that use both REST and database sentences.

All handwritten code lives in `src/test/` and is used to test the library itself.

Because this module has **no `src/main`**, Kover creates no variant for it and ignores the binary
reports of its test tasks. `config/gradle/kover_sonar.gradle` therefore feeds them into the root
report through `additionalBinaryReports`. Without that the whole Gherkin suite counts for nothing
and every glue class reads as 0%. Do not remove it, and keep it in mind when adding a test task
here.

```
src/test/kotlin/com/ragin/bdd
  (root)          — Spring Boot test application entry point
  architecture/   — Konsist architecture tests enforcing module conventions
  cucumber/       — unit tests for core utilities
  cucumbertests/  — Cucumber runner, demo REST controllers, demo hooks, test fixtures
```

---

## ScenarioStateContext

`ScenarioStateContext` is a Kotlin `object` (JVM singleton) in `core`. It is the **single source of truth
for all mutable state** during a Cucumber scenario.

### What it holds

| Field | Cleared on scenario reset? | Notes |
|---|---|---|
| `latestResponse` | yes | Last HTTP response |
| `fileBasePath` | yes | Prefix for relative classpath file lookups |
| `urlBasePath` | yes | Prefix prepended to relative URL paths |
| `serviceName` | yes | Logical name of the web server the next requests go to; `null` derives it from the path |
| `scenario` | **no** | The running Cucumber scenario, so every glue class can report. Set by the logging hook at order 1, which is *after* `reset()` at order 3 — clearing it there would empty every report |
| `editableBody` | yes | Request body text |
| `bearerToken` | yes (reset to `defaultBearerToken`) | Current authorization token |
| `headerValues` | yes | Extra request headers |
| `jsonPathOptions` | yes | JSON comparison tolerances |
| `polling` | yes | Poll interval and count |
| `executionTime` | yes (set to `now`) | Scenario start timestamp |
| `scenarioContextMap` | **no** | Key/value store shared across scenarios |
| `userTokenMap` | **no** | User → token map, set once per feature |
| `dynamicProxyHost/Port` | **no** | Proxy config, set explicitly per scenario |

### Rules for ScenarioStateContext

- Add a field only when it represents state that multiple glue classes across multiple modules need.
  State owned by a single glue class stays in that class.
- Every field added to `ScenarioStateContext` must be explicitly reset in `reset()` unless it is
  intentionally cross-scenario (like `scenarioContextMap`). Document the decision in the PR.
- Never inject `ScenarioStateContext` as a Spring bean. Access it directly as a Kotlin object.
- `uriPath` is also reset implicitly (it is re-set by every Given/When that sets a path).

---

## BaseCucumberCore

`BaseCucumberCore` is the required superclass for every glue class.

It provides:
- `readFileAsString(path)` / `readFileAsByteArray(path)` — classpath file loading with `absolutePath:` prefix support.
- `getFilePath(path)` — resolves relative paths against `ScenarioStateContext.fileBasePath`.
- `assertJSONisEqual(expected, actual)` — delegates to `BddJsonUtils`.
- `setDefaultBearerToken(token)` — sets the initial bearer token from config or parameter.

All glue classes must extend `BaseCucumberCore` directly or through an abstract base class.
This is enforced by `GlueKonsistTests`.

---

## Glue classes

### Naming

Glue class names must follow the pattern `<Purpose><Topic>Glue`:

- Start with the Cucumber step type they own: `Given`, `When`, or `Then`.
- Follow with a descriptive topic that identifies what domain or concern the class covers.
- End with `Glue`.

The `Given`/`When`/`Then` prefix is the primary boundary. When a topic grows too large,
split by introducing a new `<Purpose><NarrowerTopic>Glue` class rather than widening the existing one.

`GlueKonsistTests` enforces the `*Glue` suffix at build time. The prefix convention is a design rule.

### Inheritance

All glue classes must extend `BaseCucumberCore` directly or through an abstract base class.
This is enforced by `GlueKonsistTests`.

REST glue classes that execute HTTP requests must extend the shared abstract REST base class
in the `rest` module rather than holding `TestRestTemplate` directly. HTTP execution logic
belongs in that abstract base, not in concrete glue classes.

### Responsibilities per prefix

- **Given** glue — sets up state before a request (path, body, headers, tokens, context values).
  No HTTP calls.
- **When** glue — triggers actions (HTTP requests, body/header mutations at runtime).
- **Then** glue — validates outcomes (response code, response body, database state).
  No HTTP calls, no state mutation.

### Where new step definitions go

| Step type | Module | Class pattern |
|---|---|---|
| Set up request state | `rest` | `Given*Glue` |
| Execute HTTP request | `rest` | `When*Glue` extending the abstract REST base |
| Mutate request body or headers | `rest` | `When*Glue` |
| Validate HTTP response | `rest` | `Then*Glue` |
| Initialize or query the database | `db` | `Given*Glue` / `Then*Glue` |

If a new sentence fits none of the existing glue classes, introduce a new one rather than widening an existing one.

---

## Hooks

### Naming

Hook class names must follow the pattern `<Purpose>Hooks` and end with `Hooks`.
The purpose describes what the hook does, not when it runs (e.g. logging, reset, database).
`HooksKonsistTests` enforces the `*Hooks` suffix at build time.

### Execution order

Hooks use Cucumber's `@Before(order = N)` and `@After(order = N)`. Lower order runs first for
`@Before`, lower order runs last for `@After`.

Reserved order slots:

| Order | Purpose |
|---|---|
| 1 | Logging — scenario entry and exit logging |
| 2 | Database reset — run before state reset so the DB is clean when state initializes |
| 3 | State reset — reset `ScenarioStateContext` to a clean baseline |
| 10+ | Tag-guarded setup — optional configuration applied only when a scenario carries a specific tag |

Rules:
- Do not add a hook at order 3 or lower that depends on database state — database reset runs at order 2.
- Tag-guarded hooks (order 10+) must guard with a Cucumber tag expression and must not affect
  untagged scenarios.
- Database reset hooks silently skip if the reset resource is absent from the classpath.
  Absence is not an error; consumer projects decide whether to provide one.

### `@After` hooks

Logging hooks run at order 1 on `@After` and must see the final scenario state, including the last
HTTP response. Do not add unconditional teardown at order 1 or lower — it would execute before
logging and hide failure details.

---

## Reporting and logging

`ScenarioReporter` (`core/utils`) is the **only** sanctioned way to produce user-visible output.
Never call `scenario.log`/`scenario.attach` or a logger directly from a glue class for something a
reader is meant to see.

It offers two kinds of output, because the Cucumber HTML report renders them differently:

| Call | Media type | Rendered as | Use for |
|---|---|---|---|
| `summary(line)` | `text/x.cucumber.log+plain` | always-expanded block, no title | one short line: method, URL, status, duration, row count |
| `attachJson` / `attachText` / `attachBody` | `application/json`, `text/plain` | **collapsed** block titled with the name | payloads: bodies, headers, SQL, query results |
| `attachLink` | `text/uri-list` | clickable link | a URL worth opening |

Rules:
- `summary` also goes to the logger, so it reaches the console with no plugin configured.
  Attachments never do — a body on the console buries the steps around it.
- Everything attached passes through `ValueObfuscator.obfuscateSecretsIn`, so a credential that an
  API echoes back cannot reach a shared report. Header values named in
  `BddReportConstants.OBFUSCATED_HEADERS` are obfuscated by name as well.
- A reporting failure must never fail a scenario: the reporter no-ops when no scenario is running
  and swallows its own errors at debug level.

### Log levels

| Level | What belongs there |
|---|---|
| `error` | a failed scenario, and nothing else |
| `warn` | something the project should fix, for example a discarded statement in databaseless mode |
| `info` | the request and response summary lines, and nothing per-step beyond that |
| `debug` | framework bookkeeping: state reset, scenario entry and exit, per-field matcher traces |

All loggers sit under `com.ragin.bdd.cucumber`, so a project silences the library with a single
`logging.level.com.ragin.bdd.cucumber` entry.

### Console structure

Cucumber cannot register a plugin automatically, so `BddPrettyFormatter` (`core/plugin`) is opt-in
through `BddLibConfigConstants.Plugin.PLUGIN_PROPERTY_VALUES_DEFAULT`, **appended** to the plugins a
project already configures. It wraps Cucumber's pretty writer with attachments switched off.
Never make the library overwrite a project's own `cucumber.plugin` value.

---

## Matchers

Custom JSON field matchers implement `BddCucumberJsonMatcher` (interface in `core`) and extend
`BaseMatcher` from Hamcrest.

### Naming

All matcher files must end with `Matcher`. Enforced by `MatcherKonsistTests`.

### Registration

Matchers are registered in `BddJsonUtils` via the `BddCucumberJsonMatcher` extension point.
New matchers belong in `core` unless they are specific to the REST or database domain.

---

## BddProperties configuration

Spring `@ConfigurationProperties` prefix: `cucumbertest` (all lowercase, no separator).

| Property | Type | Default | Purpose |
|---|---|---|---|
| `cucumbertest.authorization.bearerToken.default` | String? | none | Default bearer token |
| `cucumbertest.authorization.bearerToken.noscope` | String? | none | No-scope bearer token |
| `cucumbertest.server.protocol` | String | `http` | Target server protocol |
| `cucumbertest.server.host` | String? | none | Target server host |
| `cucumbertest.server.port` | String? | none | Target server port |
| `cucumbertest.services.<name>.protocol` | String? | `cucumbertest.server.protocol` | Protocol of that service |
| `cucumbertest.services.<name>.host` | String? | `cucumbertest.server.host`, else `localhost` | Host of that service |
| `cucumbertest.services.<name>.port` | String? | the discovered `local.<name>.port` | Port of that service |
| `cucumbertest.services.<name>.basePath` | String? | servlet context path for `server`, else empty | Prepended to every URL of that service |
| `cucumbertest.services.<name>.pathPrefixes` | List<String>? | `/actuator` for `management`, else empty | Paths routed to that service; empty list disables routing |
| `cucumbertest.logging.requestBody` | Boolean | true | Attach the request body to the report |
| `cucumbertest.logging.responseBody` | Boolean | true | Attach the response body to the report |
| `cucumbertest.logging.headers` | Boolean | false | Attach request and response headers, with credentials obfuscated |
| `cucumbertest.logging.sql` | Boolean | false | Attach the executed SQL |
| `cucumbertest.logging.prettyJson` | Boolean | true | Indent JSON before attaching it |
| `cucumbertest.logging.maxBodyLength` | Int | 8192 | Truncate an attached payload beyond this length |
| `cucumbertest.proxy.host` | String | `http` | Proxy host |
| `cucumbertest.proxy.port` | Int? | none | Proxy port |
| `cucumbertest.ssl.disableCheck` | Boolean | false | Disable SSL cert check |
| `cucumbertest.scenarioContext` | Map<String,String> | empty | Pre-loaded context values |
| `cucumbertest.liquibase.closeConnection` | Boolean | false | Close Liquibase connection after use |

---

## Architecture tests (Konsist)

The integration test module contains Konsist tests that enforce module conventions at build time.
Do not suppress or disable them.

| Test class | Enforces |
|---|---|
| `GlueKonsistTests` | All classes in `glue/` end with `Glue` and extend `BaseCucumberCore` |
| `HooksKonsistTests` | All classes in `hooks/` end with `Hooks` |
| `MatcherKonsistTests` | All classes in `matcher/` end with `Matcher` and implement `BddCucumberJsonMatcher` |
| `TestKonsistTests` | All test classes end with `Tests`, use `kotlin.test.Test`, have all non-test members private |

---

## Decision rules: what belongs where

| What | Module | Where |
|---|---|---|
| Step that sets up HTTP request state (path, body, headers, tokens) | `rest` | a `Given*Glue` class |
| Step that executes an HTTP request | `rest` | a `When*Glue` class extending the abstract REST base |
| Step that mutates the request body or headers at runtime | `rest` | a `When*Glue` class |
| Step that validates an HTTP response | `rest` | a `Then*Glue` class |
| Step that initializes or queries the database | `db` | a `Given*Glue` or `Then*Glue` class |
| Utility shared across REST and database steps | `core` | `utils/` |
| Custom JSON assertion matcher | `core` | `matcher/` |
| State shared across multiple modules within a scenario | `core` | field on `ScenarioStateContext`, reset in `reset()` |
| Lifecycle hook for scenario setup or teardown | `core` | `hooks/` |
| Lifecycle hook specific to database concerns | `db` | `hooks/` |
| Spring bean configuration for database infrastructure | `db` | `configuration/` |
| Demo controller or fixture for testing a sentence | `bdd-cucumber-gherkin-lib` | `src/test/` |
