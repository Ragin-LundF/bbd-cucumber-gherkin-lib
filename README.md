# Cucumber REST Gherkin library

![Java CI with Gradle](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ragin-LundF_bbd-cucumber-gherkin-lib&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ragin-LundF_bbd-cucumber-gherkin-lib)

A [Cucumber](https://cucumber.io) / [Gherkin](https://cucumber.io/docs/gherkin/) step library for [Behaviour-Driven Development (BDD)](https://cucumber.io/docs/bdd/) of Spring Boot REST APIs and database interactions.

It ships pre-built step definitions so tests can be written in plain Gherkin without custom step code for the common cases.

## What it does

### REST API testing
- Execute **GET, POST, PUT, PATCH, DELETE** calls against any base URL
- Configure **authentication** (Bearer tokens, per-user credentials) in one `Given` step
- Set a **base path** once in `Background` and use short relative paths in all scenarios
- Validate **HTTP status codes**, **response headers**, and **response bodies** against inline JSON or JSON files

### JSON validation
- Exact matching and **[JSON-Unit](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/JSON-Unit)** flexible matchers (e.g. `${json-unit.matches:isValidUUID}`)
- Custom matchers for domain-specific assertions

### Context & data flow
- **[ScenarioContext](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Concepts#scenariocontext)**: pass values between steps within a scenario
- **`${key}` placeholder substitution** in URLs and request bodies for dynamic path construction
- Store and read response fields for chained calls

### Async & polling
- **[Polling](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/When-Polling)**: repeatedly call an endpoint until a condition is met
- **Timing assertions**: validate execution duration or introduce waits for async operations

### Database
- **[Database steps](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Common-Database)**: Liquibase migrations, SQL execution, CSV result comparison

### Dates
- **[Dynamic date generation](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Common-Dates)**: past/future timestamps without hardcoded values

See [Anti-Patterns](https://cucumber.io/docs/guides/anti-patterns/) to keep feature files clean.

See [Changelog](CHANGELOG.md) for release information.

The library tests itself against a dummy Spring Boot application in the test sources.
See [bdd-cucumber-gherkin-lib/src/test](bdd-cucumber-gherkin-lib/src/test) for feature file examples and custom JSON-Unit matchers.

## How to integrate

The library is available on Maven Central.

### Maven
```xml
<dependency>
	<groupId>io.github.ragin-lundf</groupId>
	<artifactId>bdd-cucumber-gherkin-lib</artifactId>
	<version>${version.bdd-cucumber-gherkin-lib}</version>
	<scope>test</scope>
</dependency>
```

### Gradle

For all modules (REST + DB):

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib:${version.bdd-cucumber-gherkin-lib}"
}
```

For REST only modules:

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib-rest:${version.bdd-cucumber-gherkin-lib}"
}
```

For DB modules:

```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib-db:${version.bdd-cucumber-gherkin-lib}"
}
```

## Documentation

Full documentation lives in the **[Wiki](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki)**.

| Topic                                           | Wiki page |
|-------------------------------------------------|---|
| First test from scratch                         | [Getting Started](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Getting-Started) |
| Maven / Gradle / BOM setup                      | [Installation](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Installation) |
| Spring context, auth, proxy, SSL, DB config     | [Configuration](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Configuration) |
| ScenarioContext, file paths, URL base, keywords | [Concepts](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Concepts) |
| All step signatures at a glance                 | [All Steps — Quick Reference](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Reference) |
| Authentication steps                            | [Given — Authentication](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Given-Authentication) |
| Request setup steps                             | [Given — Request Setup](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Given) |
| HTTP call steps                                 | [When — REST Calls](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-When) |
| Polling steps                                   | [When — Polling](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-When-Polling) |
| Status code / body / header assertions          | [Then — Validation](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Then-Validation) |
| Storing response values for later steps         | [Then — Context](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Then-Context) |
| Async wait and timing assertions                | [Then — Timing](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Then-Time) |
| Dynamic date handling                           | [Common — Dates](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Common-Dates) |
| Liquibase, SQL, CSV comparison                  | [Common — Database](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/Steps-Common-Database) |
| Built-in and custom JSON matchers               | [JSON-Unit](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/wiki/JSON-Unit) |
