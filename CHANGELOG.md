# Release 2.27.0

## Summary

Release 2.27.0 updates versions across Gradle, Spring Boot, Kotlin, libraries, and Testcontainers.
Adds a JUnit 5 BOM, enforces Kotlin alignment for Detekt, tweaks Gradle task configurations (including cucumber with
JUnit Platform), updates Gradle wrapper and scripts, and adjusts a test datasource image tag.

## Changes

| Cohort / File(s)                                                                                                                        | Summary of Changes                                                                                                                                           |
|-----------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Gradle wrapper update**<br>`config/gradle/wrapper.gradle`, `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`       | Bumped Gradle from 8.14.2 to 8.14.3; wrapper scripts now invoke the wrapper jar via -jar and clear CLASSPATH.                                                |
| **Build plugin and Detekt alignment**<br>`build.gradle`                                                                                 | Spring Boot plugin 3.5.3 → 3.5.4; added resolutionStrategy to force Detekt’s supported Kotlin version for org.jetbrains.kotlin deps in detekt configuration. |
| **Gradle config syntax normalization**<br>`config/gradle/detekt.gradle`, `config/gradle/java.gradle`, `config/gradle/publishing.gradle` | Switched from setter methods to property assignments for report requirements, toolchain languageVersion, artifactId, and version. No functional change.      |
| **Test task configuration**<br>`config/gradle/tests.gradle`                                                                             | Enabled useJUnitPlatform() for cucumber; set group via property; wired testClassesDirs and classpath from testing.suites.test.                               |
| **Dependency versions and BOM**<br>`dependencies.gradle`                                                                                | Added dependencyManagement importing org.junit:junit-bom:5.13.4; bumped commons-text, commons-io, commons-lang3, kotlin-logging-jvm, liquibase-core.         |
| **Project/version properties**<br>`gradle.properties`                                                                                   | Version 2.25.0 → 2.26.0; Kotlin 2.0.21 → 2.2.10; Cucumber 7.23.0 → 7.27.0; Testcontainers 1.21.2 → 1.21.3; Jackson 2.19.1 → 2.19.2.                          |
| **Test resource adjustment**<br>`src/test/resources/application.yml`                                                                    | Updated Testcontainers PostgreSQL image tag from 16.6 to 17 for cucumberTest datasource.                                                                     |

If you have trouble with Detekt and Kotlin 2.2.10, you can add the following to your `build.gradle`:

```groovy
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        if (details.requested.group == "org.jetbrains.kotlin") {
            details.useVersion(io.gitlab.arturbosch.detekt.DetektPluginKt.getSupportedKotlinVersion())
        }
    }
}
```

# # Release 2.26.0
n/a
-> Maven Central Changes

# Release 2.25.0

- JDK 21 is now minimum requirement
- jUnit 5 support by default

## Update Dependencies

- Spring Boot 3.5.3
- Liquibase 4.32.0
- Cucumber 7.23.0

## Migration from jUnit4 to jUnit5

Example from jUnit4:

```kotlin
package com.ragin.bdd.cucumbertests

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = [
        "classpath:features"
    ],
    glue = [
        "com.ragin.bdd.cucumber.glue",
        "com.ragin.bdd.cucumber.hooks",
        "com.ragin.bdd.cucumbertests.hooks"
    ],
    plugin = [
        "json:build/reports/cucumber/cucumber.json",
        "html:build/reports/cucumber/cucumber.html",
        "junit:build/reports/cucumber/cucumber.xml"
    ],
    publish = true,
    tags = "not @ignore"
)
class CucumberRunner
```

can be changed to:

```kotlin
package com.ragin.bdd.cucumbertests

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.ExcludeTags
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.ragin.bdd.cucumber.glue, " +
            "com.ragin.bdd.cucumber.hooks, " +
            "com.ragin.bdd.cucumbertests.hooks"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "json:build/reports/cucumber/cucumber.json, " +
            "html:build/reports/cucumber/cucumber.html, " +
            "junit:build/reports/cucumber/cucumber.xml"
)
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true")
// @ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
@ExcludeTags("ignore")
class CucumberRunner
```

To include tags, the `@IncludeTags` annotation can be used.
Exclude is possible with `@ExcludeTags`.

Please note, that the `@` is not required in this case.

# Release 2.24.0

Update Dependencies

# Release 2.23.0

## New sentence to set a proxy for the communication at "runtime"

This introduces a sentence for dynamic proxy settings.

```gherkin
Given that a proxy with host "<string>" and port "<string>" is configured
```

Both the host and the port can either be the host/proxy or a variable from the `ScenarioStateContext`.

This helps, for example, to set proxies started via test containers (e.g. Burp Scanner or Mock Server) during the test.
This way it is not necessary to assign a fixed port to the proxy.
This can then be written in another sentence or after starting a proxy container in the `ScenarioContext`.

Alternatively, the proxy can also be written directly to the `ScenarioStateContext`:

```kotlin
ScenarioStateContext.current().dynamicProxyHost = "localhost"
ScenarioStateContext.current().dynamicProxyPort = "8808"
```

# Release 2.22.0

## Fixes

The SSL configuration has returned a wrong HTTP client configuration, which was not ignoring redirects from the server.

# Release 2.21.0

## Dependency Updates

All dependencies were updated.

Most noticeable:

- Spring Boot to 3.4.4

# Release 2.20.0

## Dependency Updates

All dependencies were updated.

Most noticeable:

- Spring Boot to 3.4.2
- Cucumber to 7.21.1

## Logging Optimization

Now the logging of the response will only log the body, if it is not a media-subtype of `pdf`, `octet-stream` or `zip`.

## Assertions

The internal assertions are now using `assertj-core`, which is a dependency of `spring-test` instead of using `jUnit 4`.
`jUnit 4` is sadly still a dependency, because of `cucumber-junit`.

# Release 2.18.0

## Dependency Updates

All dependencies were updated.

Most noticeable:

- Spring Boot to 3.4.1
- Kotlin to 2.0.10

## Form Data Optimization

The multi-form-data upload uses now `Resource` as basis instead of `ByteArray`.
This matches better to the needs of `RestTemplate` and the `MultipartFile` on server-side.

The sentence `Given that the file "<filename>" is stored as "<context>"` stores now the file in the `ScenarioContext`
file map, but also the name of the file in the regular map under the same key.
This is required, because the filename is required for multi-form-data uploads.

# Release 2.17.0

The configuration

```yaml
cucumberTest:
  authorization:
    ...
```

is no longer required.
It must be set, if a default token can be used. Else the token must be provided via `Background` or inside of the
`Scenario`.

# Release 2.16.0

- Spring Boot to 3.3.3
- liquibase-core to 4.29.1

# Release 2.15.0

- Fixed issues with BOM's. It was not possible to resolve the transitive dependencies.

# Release 2.14.0

## Updated Gradle structure, code analysis and coverage

- Updated Gradle structure and split each configuration into separate files
- Replaced `jacoco` with `kover`
- Updated Gradle task for `cucumber`
- Added `Detekt` for static code analysis

## Updated Libs

- Spring Boot to 3.3.2
- liquibase-core to 4.29.0
- Cucumber libs to 7.18.1

# Release 2.12.0

## Updated Libs

- Spring Boot to 3.2.5
- Cucumber Libs to 7.18.0
- liquibase-core to 4.27.0

## Features

Added a new default matcher (`${json-unit.matches:isNotEqualTo}string`) to compare, that a value is not equal to a given
string.
The string can be anything after the closing bracket:

```json
{
  "string": "${json-unit.matches:isNotEqualTo}another string"
}
```

```gherkin
  Scenario: Validate field with a string that it is not the value
When executing a GET call to "/api/v1/fieldValidation"
Then I ensure that the status code of the response is 200
And I ensure that the body of the response is equal to
"""
      {
        "string": "${json-unit.matches:isNotEqualTo}another string",
        "number": 12,
        "boolean": true,
        "list": [
          "First",
          "Second"
        ],
        "object": {
          "firstname": "John",
          "lastname": "Doe"
        },
        "uuid": "${json-unit.matches:isValidUUID}",
        "objectList": [
          {
            "first": 1,
            "second": 2
          },
          {
            "first": 3,
            "second": 4
          }
        ]
      }
    """
```

See [src/test/resources/features/body_validation/field_compare.feature](src/test/resources/features/body_validation/field_compare.feature)

# Release 2.11.0

## Updated Libs

- Spring Boot to 3.2.4
- Cucumber Libs to 7.16.1
- liquibase-core to 4.26.0

# Release 2.10.0

- Removed `Accept-Language` default header.

# Release 2.9.0

## Updated Libs

- Spring Boot to 3.2.3
- Cucumber Libs to 7.15.0
- liquibase-core to 4.26.0

# Release 2.8.0

## Updated Libs

- Spring Boot to 3.2.0
- Cucumber Libs to 7.14.1
- liquibase-core to 4.25.0

# Release 2.7.0

## Updated Libs

- Spring Boot to 3.1.4
- Cucumber Libs to 7.14.0
- json-unit to 3.2.2
- liquibase-core to 4.24.0

## Features

Added a new default matcher (`${json-unit.matches:isNotEqualToScenarioContext}MY_CONTEXT_VALUE`) to compare, that a
value is not equal to the context:

```gherkin
    And I ensure that the body of the response is equal to
""" 
        {
          "value": "${json-unit.matches:isNotEqualToScenarioContext}MY_CONTEXT_VALUE",
        }
      """
```

# Release 2.6.1

Small fix for URL encoded calls, to be able to leave a value empty if the parameters are dynamic.

# Release 2.6.0

New sentence added for `application/x-www-form-urlencoded` `POST` calls.

```gherkin
When executing a url-encoded POST call to "/api/form-encoded" with the fields
| Key    | Value         |
| first  | myFirstValue  |
| second | mySecondValue |
```

The first line of the fields MUST contain `| Key | Value |`.

# Release 2.5.1

In some cases, a `@PostConstruct` annotation was not executed in the correct order.
This has now been taken care of by moving the logic it contains to a constructor.

# Release 2.5.0

- Fix for URLs with `http(s)://`

# Release 2.4.0

## Logging of requests and responses

Now all `executing` sentences log the request and response as scenario text in the report.
This makes problems in tests more visible on CI/CD scenarios.

Example:

```gherkin
When executing a GET call to "/api/v1/unauthorized"
```

```plain
  Request:
  ========
  HTTP Method: GET
  HTTP URL   : /api/v1/unauthorized
  Response:
  ========
  Status Code: 401 UNAUTHORIZED
  Body       : {"error_description":"Full authentication is required to access this resource","error":"unauthorized"}
```

# Release 2.3.0

## New sentence

```gherkin
Given that the request body in the scenario context map has been reset
```

This sentence resets the body if a scenario uses 2 requests and the second should be empty.

# Release 2.2.0

## Feature

- Logs the http method and URL for each request to have a better overview what was called with dynamic URLs.

# Release 2.1.0

## Update Libraries

- Spring Boot updated to 3.1.2

## New matcher

- A new matcher for IBANs was added. The syntax is `${json-unit.matches:isValidIBAN}`.

# Release 2.0.1

This release requires Java 17 and Spring Boot 3.
It uses also the `jakarta.*` packages instead of the `javax.*` packages.

## Update Libraries

- Spring Boot updated to 3.1.1
- Liquibase updated to 4.23.0
- Cucumber updated to 7.13.0
- Apache HTTP Client updated to 5.2.1

# Release 1.52.0

## Update Libraries

- Cucumber updated to 7.11.2
- Spring Boot updated to 2.7.10
- Liquibase updated to 4.20.0
- Snakeyaml updated to 2.0

# Release 1.51.0

## Update Libraries

- Cucumber updated to 7.11.1
- Spring Boot updated to 2.7.8
- Liquibase updated to 4.19.0

# Release 1.50.0

## Update Libraries

- Cucumber updated to 7.9.0
- Spring Boot updated to 2.7.5
- Liquibase updated to 4.17.2

# Release 1.49.0

## Update Libraries

- Cucumber updated to 7.5.0
- Spring Boot updated to 2.7.2
- Liquibase updated to 4.14.0

## Fixes

The sentence `I store the string of the field "{string}" in the context "{string}" for later usage` is now able to store
any kind of objects and not only strings.

# Release 1.48.0

## Updated Libraries

- Cucumber updated to 7.3.4
- Liquibase updated to 4.10.0 (CVE-fix)
- JSON-Unit to 2.35.0

# Release 1.46.0

## Updated Libraries

- Spring Boot updated to 2.6.7
- Cucumber updated to 7.3.2

## Feature: Simple form-data POST request

It is now possible to create a simple form-data POST request.
A file can also be attached in the process.

To add the file there is the sentence:

```gherkin
Scenario: Post form data
Given that the file "test.txt" is stored as "FORM_FILE"
```

The file is placed as a byte array in a separate context, which is emptied again after the scenario is completed for
memory reasons.

To execute a form-data request the following sentences are available:

```gherkin
When executing a form-data POST call to "/your/api" with the fields
| text-form-data-key  | text-form-data-value      |
```

```gherkin
When executing an authorized form-data POST call to "/your/api" with the fields
| text-form-data-key  | text-form-data-value      |
```

The fields are written as key/value datamap.

Examples can be found at [src/test/resources/features/form_data/](src/test/resources/features/form_data/).

# Release 1.45.0

- Updated Spring Boot to 2.6.6 to avoid issues with Spring Shell.

# Release 1.44.0

- Disable redirects for HTTP client.

# Release 1.43.0

## Updating dependencies

- Spring Boot 2.6.4

# Release 1.42.0

An issue was fixed, which prevented to overwrite the `Accept-Language`, `Content-Type` and `Accept` headers.

# Release 1.41.0

## Updated libraries

- Spring Boot updated to `2.6.2`
- Cucumber updated to `7.2.2`

## Extended Polling

Polling now allows also to check for the HTTP code only.

### Unauthorized Example

```gherkin
Scenario: Polling unauthorized until response code is correct with long config
Given that a requests polls every 1 seconds
And that a requests polls for 5 times
And that the API path is "/api/v1/polling"
When executing a GET poll request until the response code is 200
```

### Authorized Example

```gherkin
Scenario: Polling authorized until response code is correct with short config
Given that a request polls every 1 seconds for 5 times
And that the API path is "/api/v1/pollingAuth"
When executing an authorized GET poll request until the response code is 200
```

Examples can be found at [src/test/resources/features/polling/](src/test/resources/features/polling/).

## Extended Header Manipulation

### Prefix for Header manipulation

```gherkin
Scenario: Add custom header with prefix
Given I set the header "X-My-Custom-Header" to "ABC_DEF" prefixed by "PRE_"
```

This sets the header `X-My-Custom-Header` to the value of `ABC_DEF` with the prefix `PRE_`.
The prefix and the value can be also a variable name from the context.

Please have a look to the examples at: [src/test/resources/features/header/](src/test/resources/features/header/)

# Release 1.40.0

## Updated libraries

- Spring Boot updated to `2.6.1`
- Cucumber updated to `7.1.0`

# Release 1.39.0

## Updated libraries

- Spring Boot updated to `2.5.6` because of a memory leak in tomcat
- Cucumber updated to `7.0.0`

## Enhancements

### Added check for headers

If it is required to check if a HTTP header contains a special value, this can be done with the following sentence:

```gherkin
And I ensure, that the header "<header name>" is equal to "<header values>"
```

Be aware, that headers are always an array.
If there are more header values you want to check, please add them comma separated.

Example:

```gherkin
Scenario: Check a header
When executing an authorized GET call to "/api/v1/header"
Then I ensure that the status code of the response is 200
And I ensure, that the header "X-TEST-HEADER" is equal to "present"
```

This might be used to check if XSS headers are set for example.

See [src/test/resources/features/header/header.feature](src/test/resources/features/header/header.feature)

# Release 1.37.0

## Updated libraries

- Spring Boot updated to `2.5.5`
- Cucumber-JVM updated to `6.11.0`

# Release 1.36.0

## Updated libraries

- Spring Boot updated to `2.5.2`
    - With the Spring update Liquibase will also be updated to `4.3.5`
- Cucumber-JVM updated to `6.10.4`
- Json-Unit updated to `2.27.0`

## Small enhancements

A user token can be resolved also from context map. If nothing was found there, it uses the given one.
This refers to the following sentences:

- `that the following users and tokens are existing`
- `that the user is {string}`

In general the `ScenarioStateContext` class has now a new method called `resolveEntry(key: String)`.
This method tries to read the value from the context map for the given key.
If the key was not found, it returns the key itself.
This may be helpful for own sentences based on the context map.

## Possibility to wait

There is a new sentence that allows you to wait some time before proceeding:

```gherkin
  Scenario: Wait for something
Then I wait for 1000 ms
```

Examples at:
[src/test/resources/features/performance/performance.feature](src/test/resources/features/performance/performance.feature)

# Release 1.35.0

Introduction of date util sentences and extended date validators.

This version introduces some `Given` sentences to create a date in the past or future.
In addition, there is a new matcher that allows to compare this date with a JSON date or a datetime.

Read more at:
[docs/date_operations.md](docs/date_operations.md).

Examples at:
[src/test/resources/features/date_operations/date_operations.feature](src/test/resources/features/date_operations/date_operations.feature)

# Release 1.34.1

An issue with the new configuration was fixed.

The introduction of the `BddProperties` class made some problems with not configured properties.

Please ensure also, that you've put the library on the configuration scan path:

```java
@ConfigurationPropertiesScan({
        "com.ragin.bdd", "configuration.com.ragin.bdd"
})
```

# Release 1.34.0

## Updates

This release updates the Spring dependency to `2.4.5`.

It is recommended to update the project also to this version.

## New groupId and Maven Central deployment

Because of the deprecation of jcenter, the artifacts are now published on Maven Central.

To be able to do that, the `groupId` has to be changed to:

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
    testImplementation "io.github.ragin-lundf:bdd-cucumber-gherkin-lib:${version.bdd - cucumber - gherkin - lib}"
}
```

# Release 1.32.0

## Support for predefined ScenarioContext values

In order to predefine some global `ScenarioContext` values outside the gherkin definition,
it is now possible to add them to the `application.yaml`/`application.properties` file.

The key `cucumberTests.scenario-context` allows to define a set of key/value pairs for the `ScenarioContext` map:

application.yaml

```yaml
cucumberTests:
  scenario-context:
    CTX_PRE_DEFINED_USER: Pre Defined
    CTX_PRE_DEFINED_FIRST_ID: abcdefg
```

application.properties

```properties
cucumberTests.scenario-context.CTX_PRE_DEFINED_USER=Pre Defined
cucumberTests.scenario-context.CTX_PRE_DEFINED_FIRST_ID=abcdefg
```

If many tests require the same data over and over again,
it doesn't make sense to copy and maintain it in every feature.

By defining it in the `application` file, it can be defined once and used everywhere.

For an example, please have a look at:

- Configuration: [application.yml](src/test/resources/application.yml)
- Test: [global_config/global_config.feature](src/test/resources/features/global_config/global_config.feature)

# Release 1.31.0

## Supports dynamic URL's

This version supports the usage of `ScenarioContext` variables for all URI parameters.

Now it is allowed to write sentences like:

```gherkin
Scenario: Use dynamic URL
Given that the context contains the following 'key' and 'value' pairs
| ${MY_DYNAMIC_URL} | https://google.com |
And that the API path is "${MY_DYNAMIC_URL}"
```

# Release 1.30.0

## Support for polling APIs

The polling configuration is automatically reset before each scenario.
It can be configured via the background or directly in the scenario.

When the expected HTTP status code and JSON structure has been sent as a response, polling will stop.
This allows an endpoint to be polled until it changes state or fail if the state has not changed during the specified
time and retry configuration.

The configuration can be done in to ways.

As a single line configuration:

```gherkin
Scenario: Single line configuration
Given that a request polls every 1 seconds for 5 times
```

Or as a multiline configuration that supports to specify one of the configurations in the `Background` and the other in
the `Scenario` (or to have it more readable).

```gherkin
Scenario: Multiline configuration
Given that a requests polls every 1 seconds
And that a requests polls for 5 times
```

The `URL`/`URI` and (if required) body have to be preconfigured. Polling itself does simply use previous set body and
path definition.
To execute a request it supports the well known authorized and unauthorized phrases and it supports direct JSON or JSON
from file:

Authorized request with JSON response file:

```gherkin
Scenario: Authorized request with JSON response file
Given that a request polls every 1 seconds for 5 times
And that the API path is "/api/v1/polling"
When executing an authorized GET poll request until the response code is 200 and the body is equal to file "expected.json"
```

Unauthorized request with JSON response file:

```gherkin
Scenario: Unauthorized request with JSON response file
Given that a request polls every 1 seconds for 5 times
And that the API path is "/api/v1/polling"
When executing a GET poll request until the response code is 200 and the body is equal to file "expected.json"
```

Authorized request with direct JSON response:

```gherkin
Scenario: Authorized request with JSON response file
Given that a request polls every 1 seconds for 5 times
And that the API path is "/api/v1/polling"
When executing an authorized GET poll request until the response code is 200 and the body is equal to
"""
    {
      "message": "SUCCESSFUL"
    }
    """
```

Unauthorized request with direct JSON response:

```gherkin
Scenario: Unauthorized request with JSON response file
Given that a request polls every 1 seconds for 5 times
And that the API path is "/api/v1/polling"
When executing a GET poll request until the response code is 200 and the body is equal to
"""
    {
      "message": "SUCCESSFUL"
    }
    """
```

Examples can be found at [src/test/resources/features/polling/](src/test/resources/features/polling/).

# Release 1.29.1

## Support for database-less applications

This version introduces support for applications that do not have a database.
To configure the library to run database-less, it is necessary to set up the following configuration in the
`application.yaml` file:

application.properties:

```properties
cucumberTest.databaseless=true
```

or

application.yaml:

```yaml
cucumberTest:
  databaseless: true
```

If the `databaseless` key is not true or missing, the library tries to instantiate the database related beans.

In some cases it is required to disable the database autoconfiguration of Spring Boot:

application.properties:

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

or

application.yaml:

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

or as annotation:

```java

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }
}
```

Please also make sure that the `@ContextConfiguration` annotation does not contain the `DatabaseExecutorService.class`.

# Release 1.28.0

## Proxy support

This can be useful to use the cucumber tests together
with [burp-scanner](https://portswigger.net/burp/vulnerability-scanner).

The proxy can be configured with:

application.properties:

```properties
cucumberTest.proxy.host=localhost
cucumberTest.proxy.port=8866
```

or

application.yaml:

```yaml
cucumberTest:
  proxy:
    host: localhost
    port: 8866
```

The host can be an IP or a domain. The port must be higher than 0.
If a condition is not met, the proxy is not set.

_Default is deactivated._

## Disable SSL verification

Along with proxy support, it may be necessary to disable SSL validation as well.

This can be configured via:

application.properties:

```properties
cucumberTest.ssl.disableCheck=true
```

or

application.yaml:

```yaml
cucumberTest:
  ssl:
    disableCheck: true
```

_Default is false._

# Release 1.27.0

## Code changes

The Java code was refactored to Kotlin.
This should make the code more robust and better maintainable.

## Enhanced tests

Tests for the database features were added
under [src/test/resources/features/database/](src/test/resources/features/database/).

# Release 1.26.0

Small bugfix release for proper mismatch messages of the matchers.

# Release 1.24.0

## New features

### Validate execution time of requests

```gherkin
Scenario:
Then I ensure that the execution time is less than {long} ms
```

Validates, that the execution of the Scenario has taken less than [n] ms.

Please have a look to the examples
at: [src/test/resources/features/performance/](src/test/resources/features/performance/).

# Release 1.23.0

## Dependency updates

This release updates some dependencies.

## Set the Bearer token dynamically

To be able to set dynamic bearer token, the following sentence can be used:

```gherkin
Feature: Bearer Token

  Scenario: Set bearer token directly
    Given that the Bearer token is "abcdefg"
```

This sentence is primary looking into the context map, if there is a key with the given value.
If this is not the case, it uses the given string directly as the token.

Please have a look to the examples at: [src/test/resources/features/header/](src/test/resources/features/header/)

# Release 1.22.0

The `BddCucumberDateTimeFormat` for custom date/time formatters has been changed from `List<String>` with a list
of patterns as string to `List<DateTimeFormatter>`.

# Release 1.21.0

## Dependencies

Update of dependencies and Cucumber library 6.8.1.

## Add own patterns for `${json-unit.matches:isValidDate}`

To add own `DateTimeFormatter` patterns to extend the `${json-unit.matches:isValidDate}` range
a new class is required that implements the interface `BddCucumberDateTimeFormat`.
The method `pattern()` must return the date patterns as `List<String>`.

To register this custom class it is necessary to add it `@ContextConfiguration` `classes` definition.

Example:

- [Configuration context](src/test/kotlin/com/ragin/bdd/cucumbertests/hooks/CreateContextHooks.kt)
- [Test feature](src/test/resources/features/body_validation/field_compare.feature)

# Release 1.20.7

Added new DateFormatter for database comparison: `yyyy-MM-dd HH:mm:ss.SSSSSS`.

# Release 1.20.6

The relative file paths and the `absolutePath:` keyword are now also valid for loading liquibase scripts.

# Release 1.20.5

The executor of the Liquibase scripts has sometimes problems with H2 memory databases, that the H2 connection is closed
after the JDBC connection is closed.

This problem can now be solved by setting the following properties:

```properties
cucumberTest.liquibase.closeConnection=false
```

or

```yaml
cucumberTest:
  liquibase:
    closeConnection: false
```

The default value is `false`, which means, that the connection will not be closed.

# Release 1.20.4

Adds support to define the target server, if the test library should be used for various tests in an outsourced test
repository.

It is now possible to overwrite the `protocol`, `host` and `port` in the `application.yaml`/`application.properties`:

application.properties:

```properties
cucumberTest.server.protocol=http
cucumberTest.server.host=localhost
cucumberTest.server.port=80
```

or

application.yaml:

```yaml
cucumberTest:
  server:
    protocol: "http"
    host: "localhost"
    port: 80
```

_All parameters are optional. If nothing is being defined, it uses the default `http://localhost:<LocalServerPort>`._

# Release 1.20.3

Now it is allowed to use full paths instead of only URIs.
This may help if between some external systems has to be called.

The library checks if the path starts with `http://` or `https://`.
In these cases, it does not add `localhost`.

# Release 1.20.2

This release fixes the problem, that the sentence to set a header value does not resolve the value first from the
context.

Now it is possible to use static values or (if the value matches to the context) previously stored parameters.

Example:

[src/test/resources/features/header/](src/test/resources/features/header/)

# Release 1.20.1

The REST template has thrown an exception in case of `5xx` response codes.

This version fixes the issue and returns the correct HTTP code and message.

# Release 1.20.0

## Introducing user

### Define user(s)

With the following sentence it is possible to define multiple users:

```gherkin
Feature: User features

  Background:
    Given that the following users and tokens are existing
      | john_doe   | my_auth_token_for_john_doe    |
      | johana_doe | my_auth_token_for_johana_doen |
```

Now every scenario in this feature can use the user with:

```gherkin
  Scenario: Using authorized user john_doe
Given that the user is "john_doe"
```

The library selects the right token from the given list and executes the calls with this user.
It is also possible to define both in the `Background` specification. Then all tests will have this user as default:

```gherkin
Feature: User features in global context

  Background:
    Given that the following users and tokens are existing
      | john_doe | my_auth_token_for_john_doe |
    And that the user is "john_doe"
```

Please have a look to the examples at: [src/test/resources/features/user/](src/test/resources/features/user/)

# Release 1.19.0

## Fewer sentences but still compatible and more possibilities

This release introduces the parameter `{httpMethod}` which replaces all sentences with an HTTP method in the name.
Allowed values are:

- GET
- POST
- DELETE
- PUT
- PATCH

This reduces the number of Gherkin sentences by 24 and adds 24 new possible sentences because the shorter GET sentences
are now also available for all other methods.

## Validate only special fields of the response body

### Validation of one field

```gherkin
  Scenario: Validate field of the body
Then I ensure that the body of the response contains a field "list[0]" with the value "First"
And I ensure that the body of the response contains a field "$.list[1]" with the value "Second"
Then I ensure that the body of the response contains a field "shouldNotExist" with the value "@bdd_lib_not_exist"
```

### Validation of multiple fields

```gherkin
  Scenario: Validate multiple fields
And I ensure that the body of the response contains the following fields and values
| string           | is a string                 |
| number           | 12                          |
| uuid             | ${json-unit.matches:isUUID} |
| $.number         | @bdd_lib_not 15             |
| list             | ["First","Second"]          |
| list[0]          | First                       |
| $.list[0]        | BDD_TEST_LIST_FIRST_ELEMENT |
| $.list[1]        | Second                      |
| object.firstname | John                        |
| object.lastname  | Doe                         |
| shouldNotExist   | @bdd_lib_not_exist          |
```

In this case, the fields that should be compared can be given as a data table map.
The first column is the field name, the second the expected value.

### Description

This sentence compares only the given field of the response.
The field can be a JSON path. The library checks if it starts with `$.`.
If it does not start with `$.` it will be added internally.

To test if a field is NOT present, the reserved word `@bdd_lib_not_exist` can be used as the value.

To test if a value is NOT the expected value, the reserved word `@bdd_lib_not ` can be used to negate the comparison.
It is not possible to use a `!` as a negation prefix, because it can also be a valid result.

The library also tries to resolve the value from the context map.
If nothing was found, the original value is used.

It is also possible to use JSON-Matcher (user-defined and bdd-cucumber-lib).
These are written with the notation `${json-unit.matches:isUUID}` (as an example for the UUID-Matcher).

**_ATTENTION: Only unparameterized custom matchers or bdd lib-matchers can be used for field validation!_**

Find examples for this feature
under: [src/test/resources/features/body_validation/](src/test/resources/features/body_validation/).

## Reset the scenario context

```gherkin
Scenario: Reset the scenario context
Given that the stored data in the scenario context map has been reset
```

Reset the context state map.

# Release 1.18.0

## Configure the JSON compare to ignore extra elements in arrays

```gherkin
Scenario:
Given that the response JSON can contain arrays with extra elements
```

_It is also possible to use the `@bdd_lib_json_ignore_new_array_elements` annotation on `Feature` or `Scenario` level._

With this sentence or annotation, the JSON comparison will ignore new array elements.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.

## Configure the JSON compare to ignore the order of arrays

```gherkin
Scenario:
Given that the response JSON can contain arrays in a different order
```

_It is also possible to use the `@bdd_lib_json_ignore_array_order` annotation on `Feature` or `Scenario` level._

With this sentence or annotation, the JSON comparison will ignore the order of arrays.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.

## Configure the JSON compare to ignore extra fields

```gherkin
Scenario:
Given that the response JSON can contain extra fields
```

_It is also possible to use the `@bdd_lib_json_ignore_extra_fields` annotation on `Feature` or `Scenario` level._

With this sentence or annotation, the JSON comparison will ignore new/not defined fields in the response.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.

# Release 1.17.0

## Absolute file path support

Files can be added as a relative path to a previously given base path or with an "absolute" path with the prefix
`absolutePath:`.
In the last case, the system is using the base classpath as root.

## Validate response HTTP code and body together

```gherkin
Scenario:
Then I ensure that the response code is 201 and the body is equal to
"""
    {
      "field": "value",
    }
    """
```

In this case, the response status code is part of the sentence, and the JSON is written directly under the sentence and
enclosed in three double quotation marks.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic
elements.

## Validate response HTTP code and body together with a JSON file

```gherkin
Scenario:
Then I ensure that the response code is 200 and the body is equal to the file "response.json"
```

In this case, the response status code, and the JSON file are written together in one sentence.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic
elements.

# Release 1.16.0

- Changing the body manipulation with creating numbers from `<number> characters` to `<number> bdd_lib_numbers` (e.g.
  `10 bdd_lib_numbers`).
- Adding `bdd_lib_uuid` to generate random UUIDs
- Adding a `${json-unit.matches:isValidUUID}` which checks, if the string is a valid UUID
- Fixed Authorization header, that this header is only available once, when it is overwritten
- Adding a lot of tests as examples

# Release 1.15.0

Adding support in the paths to support templates.
If the path contains something like:

```
/api/v1/${dynamicElement}/
```

In this case, the `dynamicElement` will be replaced, if it exists in the ScenarioContext.

Support for adding static key/value pairs to the context:

- [Set a static value to the context](README.md)
- [Set multiple static values to the context](README.md)

# Release 1.14.0

Adding support for the JSON path to the "I set the value of" sentence.
It now also tries to resolve the value from the ScenarioContext.
If nothing is found, it uses the original value.

# Release 1.13.0

Adding support for JSON path to the "I store" sentence.

The JSON path can be used with:

```
$.firstElement[3].nextElement
```

The library detects, if the path has the prefix `$.`. If it is not available, it adds this prefix.

# Release 1.12.0

- Adding support for adding and manipulating headers.
- Restructured the documentation: HTTP Methods have now their own md files.

# Release 1.11.0

- Adding support for JSON matcher which can compare to values in the ScenarioContext.
  More information: [README.md](README.md)
- Adding support for own custom matcher.
  More information: [README.md](README.md)

# Release 1.9.0

- Correction of the sentence `^that the body of the response is$` to `^that the body of the request is$`
