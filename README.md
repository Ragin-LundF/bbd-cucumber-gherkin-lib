# Cucumber REST Gherkin library

![Java CI with Gradle](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ragin-LundF_bbd-cucumber-gherkin-lib&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ragin-LundF_bbd-cucumber-gherkin-lib)

This library supports some basic sentences to handle REST API calls and basic database operations.

It is based on [Cucumber](https://cucumber.io) and helps to support [Behaviour-Driven Development (BDD)](https://cucumber.io/docs/bdd/).

Cucumber executes `Steps` in form of [Gherkin](https://cucumber.io/docs/gherkin/) language.

Read also about [Anti-Patterns](https://cucumber.io/docs/guides/anti-patterns/) of Cucumber to avoid problems and to have a clear style.

See [Changelog](CHANGELOG.md) for release information.

The library tests with itself and with a dummy application in the test sources to have a lot of examples for the usage.
There you can also find custom-matcher for JSON-Assert.
See [src/test](src/test) folder for examples.

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
```groovy
dependencies {
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib:${version.bdd-cucumber-gherkin-lib}"
}
```

# Documentation

The documentation of the configuration, integration and available sentences can be found in the wiki.

See [Wiki](wiki)
