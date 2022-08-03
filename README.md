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

### Configuration

The library supports some additional configurations to set up, for example, default tokens, base URL settings, proxy support, and SSL handling.

To learn more, read the [docs/configuration.md](docs/configuration.md) Guide.

# Table of content

- [Cucumber REST Gherkin library](#cucumber-rest-gherkin-library)
- [Table of content](#table-of-content)
- [Basic Concept](#basic-concept)
- [JSONUnit extensions for better testing of JSON responses](#jsonunit-extensions-for-better-testing-of-json-responses)
- [Steps](#steps)
  - [Database](#database)
  - [REST](#rest)
    - [JSON-Unit](#json-unit)
    - [Polling](#polling)
    - [Given](#given-1)
      - [Date operations](#date-operations)
      - [Define user(s)](#define-users)
      - [Set path base directory for request/result/database files](#set-path-base-directory-for-requestresultdatabase-files)
      - [Set a static value to the context](#set-a-static-value-to-the-context)
      - [Set multiple static values to the context](#set-multiple-static-values-to-the-context)
      - [Set base path for URLs](#set-base-path-for-urls)
      - [Define that a token without scopes should be used.](#define-that-a-token-without-scopes-should-be-used)
      - [Set a URI path for later execution](#set-a-uri-path-for-later-execution)
      - [Set a body from JSON file for later execution](#set-a-body-from-json-file-for-later-execution)
      - [Set a body directly for later execution](#set-a-body-directly-for-later-execution)
    - [When](#when)
      - [Header manipulation](#header-manipulation)
      - [Body manipulation](#body-manipulation)
      - [Execute requests](#execute-requests)
    - [Then](#then-1)
      - [Additional validation configuration](#additional-validation-configuration)
      - [Wait for some ms](#wait-for-some-ms)
      - [Validate execution time of requests](#validate-execution-time-of-requests)
      - [Validate HTTP response code](#validate-http-response-code)
      - [Validate response body with JSON file](#validate-response-body-with-json-file)
      - [Validate response body with given String](#validate-response-body-with-given-string)
      - [Validate response HTTP header contains a value](#validate-response-http-header-contains-a-value)
      - [Validate response HTTP code and body together](#validate-response-http-code-and-body-together)
      - [Validate response HTTP code and body together with a JSON file](#validate-response-http-code-and-body-together-with-a-json-file)
      - [Validate only special fields of the response body](#validate-only-special-fields-of-the-response-body)
      - [Read from Response and set it to a `Feature` context](#read-from-response-and-set-it-to-a-feature-context)


# Basic Concept

This library defines a set of sentences and tries to harmonize them and provide a context-related beginning of sentences.
This is very helpful for IDEs with code completion.

| Step    | Sentence start           | Main usage         |
| ---     | ---                      | ---                |
| `Given` | `that`                   | Prepare something  |
| `When`  | `executing a` or `I set` | Do something       |
| `Then`  | `I ensure` or `I store`  | Validate something |

There are a lot of examples in the [src/test](src/test) directory.

## JSONUnit extensions for better testing of JSON responses

To compare and validate JSON responses, JSONUnit is used.

Also included in the library are some extensions that make it possible to write more readable validations in JSON for example:

- Date validator
- UUID validator
- ...

To learn more about JSONUnit integration and the ability to extend and write your own validators, see [docs/json_matcher.md](docs/json_matcher.md).

# Steps
It is a best-practice to not use syntax like this:
```gherkin
Scenario:
  Given that something was done
  Given that something else was done
  Given that something more was done
```

For that `Gherkin` offers `Steps` like `And` to make the definition more readable. In general every sentence can be reused with another `Step`.
It is recommended to follow the basic definition like described under [Basic concept](#basic-concept).

This transforms the upper example to:
```gherkin
Scenario:
  Given that something was done
  And that something else was done
  And that something more was done
```

This sounds much better, didn't it?

## Database

To enable end-to-end testing from the REST API to the database, the library supports various functionalities for database operations.

For example:
- Execution of Liquibase scripts.
- Validation of SQL select results

[docs/database.md](docs/database.md)


## REST
The REST API steps can be prepared with some given steps.

If a path contains a template placeholder with `${}` like `${elementFromContext}` the library tries to replace this with `elementFromContext` in the context, if it exists. 

Files can be added as relative path to a previously given base path or with an "absolute" path with the prefix `absolutePath:`.
In the last case the system is using the base classpath as root.

### JSON-Unit

The library contains already some matchers:
- `${json-unit.matches:isValidDate}` which checks, if the date can be a valid date by parsing it into date formats
- `${json-unit.matches:isDateOfContext}<var>` which checks, if the date is equal to a previous stored in context
- `${json-unit.matches:isValidUUID}` which checks, if the string is a valid UUID
- `${json-unit.matches:isEqualToScenarioContext}create_id` which compares the content of the actual JSON to a variable in the ScenarioContext.
  The context has to be set before with the [I store the string of the field "<field>" in the context "<context-id>" for later usage](#read-from-response-and-set-it-to-a-feature-context) sentence.

There are more details about how to extend it at the [Extension of JSON Unit Matcher](docs/json_matcher.md) section.

Read more about date operations at [docs/date_operations.md](docs/date_operations.md).

### Polling
Polling support combines some `Given` and `When` definitions. For this reason it has its own chapter.

The polling configuration is automatically reset before each scenario.
It can be configured via the background or directly in the scenario.

When the expected HTTP status code and JSON structure has been sent as a response, polling will stop.
This allows an endpoint to be polled until it changes state or fail if the state has not changed during the specified time and retry configuration.

The configuration can be done in to ways.

As a single line configuration:
```gherkin
Scenario: Single line configuration
    Given that a request polls every 1 seconds for 5 times
```

Or as a multiline configuration that supports to specify one of the configurations in the `Background` and the other in the `Scenario` (or to have it more readable).

```gherkin
Scenario: Multiline configuration
    Given that a requests polls every 1 seconds
    And that a requests polls for 5 times
```

The `URL`/`URI` and (if required) body have to be preconfigured. Polling itself does simply use previous set body and path definition.
To execute a request it supports the well known authorized and unauthorized phrases and it supports direct JSON or JSON from file:

#### Polling with JSON response file and HTTP Code check

Authorized:

```gherkin
Scenario: Authorized request with JSON response file
  Given that a request polls every 1 seconds for 5 times
  And that the API path is "/api/v1/polling"
  When executing an authorized GET poll request until the response code is 200 and the body is equal to file "expected.json"
```

Unauthorized:

```gherkin
Scenario: Unauthorized request with JSON response file
  Given that a request polls every 1 seconds for 5 times
  And that the API path is "/api/v1/polling"
  When executing a GET poll request until the response code is 200 and the body is equal to file "expected.json"
```

#### Polling with direct JSON response and HTTP Code check

Authorized:

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

Unauthorized:

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

#### Polling with HTTP Code check only

Unauthorized:

```gherkin
Scenario: Polling unauthorized until response code is correct with long config
  Given that a requests polls every 1 seconds
  And that a requests polls for 5 times
  And that the API path is "/api/v1/polling"
  When executing a GET poll request until the response code is 200
```

Authorized:

```gherkin
Scenario: Polling authorized until response code is correct with short config
  Given that a request polls every 1 seconds for 5 times
  And that the API path is "/api/v1/pollingAuth"
  When executing an authorized GET poll request until the response code is 200
```

Examples can be found at [src/test/resources/features/polling/](src/test/resources/features/polling/).


### Given

#### Date operations

Read more about date operations at [docs/date_operations.md](docs/date_operations.md).

#### Define user(s)
With the following sentence it is possible to define multiple users:
```gherkin
Feature: User features
  Background:
    Given that the following users and tokens are existing
    | john_doe    | my_auth_token_for_john_doe    |
    | johana_doe  | my_auth_token_for_johana_doen |
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
    | john_doe    | my_auth_token_for_john_doe    |
    And that the user is "john_doe"
```

Please have a look to the examples at: [src/test/resources/features/user/](src/test/resources/features/user/)

#### Set the Bearer token dynamically
To be able to set dynamic bearer token, the following sentence can be used:

```gherkin
Feature: Bearer Token
  Scenario: Set bearer token directly
    Given that the Bearer token is "abcdefg"
```

This sentence is primary looking into the context map, if there is a key with the given value.
If this is not the case, it uses the given string directly as the token.

Please have a look to the examples at: [src/test/resources/features/header/](src/test/resources/features/header/)


#### Set path base directory for request/result/database files
```gherkin
Scenario:
  Given that all file paths are relative to {string}
```

Sets an internal `base file path` for all files in the `Scenario` or `Feature`.
It is used for:
- Request files
- Response files (for compare)
- Database query files (`.sql`)
- Database CSV result files (`.csv`)

#### Set a static value to the context
```gherkin
Scenario: Test with static key/value added to the context
    Given that the context contains the key "staticKey" with the value "staticValue" 
    And that the API path is "/api/v1/${staticKey}/myApi"
    When executing a GET call with previously given URI
```

or use it in an outline scenario:

```gherkin
Scenario Outline: Test with static key/value added to the context in an outline scenario
        Given that the context contains the key "<staticKey>" with the value "<staticValue>"
        And that the API path is "/api/v1/${staticURLElement}"
        When executing a GET call with previously given URI
    Examples:
        | staticKey        | staticValue |
        | staticURLElement | resourceA   |
        | staticURLElement | resourceB   |
        | staticURLElement | resourceC   |
```

It adds the key/value pairs to the context. Please ensure, that those static values are unique to avoid overwriting them in later steps.

#### Set multiple static values to the context
```gherkin
Scenario: Test with static key/value added to the context via table
    Given that the context contains the following 'key' and 'value' pairs
      | staticFirstElement  | resourceA |
      | staticSecondElement | resourceB |
    And that the API path is "/api/v1/${staticFirstElement}/${staticSecondElement}"
    When executing a GET call with previously given URI
```

It adds the key/value pairs to the context. Please ensure, that those static values are unique to avoid overwriting them in later steps.

#### Reset the scenario context
```gherkin
Scenario: Reset the scenario context
    Given that the stored data in the scenario context map has been reset
```

Reset the context state map.

#### Set base path for URLs
```gherkin
Scenario:
  Given that all URLs are relative to {string}
```

Sets an internal `base URL path` for all URLs in the `Scenario` or `Feature`.
This is very useful to avoid repeating e.g. `/api/v1/myapitotest` before every concrete endpoint.

It is also possible to use placeholders with `${placeholder}` syntax. They will be replaced from the context.

#### Define that a token without scopes should be used.
```gherkin
Scenario:
  Given that a bearer token without scopes is used
```

This library supports two types of Bearer tokens. With this step it possible to set the token without scope.
Else it uses the default token, which should contain correct `scopes`/`authorities`.

The token has to be configured in the `application.yml` / `application.properties` in the `src/main/test/resources` directory.

The token can be set like:

`application.properties`:
```properties
cucumberTest.authorization.bearerToken.noscope=eyJhbGciOiJI[...]V_adQssw5c
cucumberTest.authorization.bearerToken.default=eyJfgEiooIfS[...]Bs_sadf4de
```

`application.yml`:
```yaml
cucumberTest:
  authorization:
    bearerToken:
      noscope: eyJhbGciOiJI[...]V_adQssw5c
      default: eyJfgEiooIfS[...]Bs_sadf4de
```

#### Set a URI path for later execution
```gherkin
Scenario:
  Given that the API path is {string}
```
This sets a URI, path which can be executed later.
It is required to use this `Given`/`And` step in cases when it is necessary to manipulate e.g. dynamic elements in the URI.

#### Set a body from JSON file for later execution
```gherkin
Scenario:
  Given that the file {string} is used as the body
```

This sets the JSON file for the body for later execution.
It is required to use this `Given` step in cases when it is necessary to manipulate e.g. dynamic elements in the URI.

#### Set a body directly for later execution
```gherkin
Scenario:
  Given that the body of the request is
    """
    {
      "key": "value"
    }
    """
```

This sets the JSON body for later execution.
It is required to use this `Given` step in cases when it is necessary to manipulate e.g. dynamic elements in the URI.


### When
The paths that are used here can be shortened by set a base URL path with [Set base path for URLs](#set-base-path-for-urls) with a `Given` Step before.

#### Header manipulation

##### Simple manipulation
```gherkin
Scenario:
  When I set the header {string} to {string}
```

This sentence allows adding or manipulating headers. The first argument is the header name and the second the header value.

##### Prefix for Header manipulation
```gherkin
Scenario: Add custom header with prefix
  Given I set the header "X-My-Custom-Header" to "ABC_DEF" prefixed by "PRE_"
```

This sets the header `X-My-Custom-Header` to the value of `ABC_DEF` with the prefix `PRE_`.
The prefix and the value can be also a variable name from the context.

Please have a look to the examples at: [src/test/resources/features/header/](src/test/resources/features/header/)

#### Body manipulation
```gherkin
Scenario:
  When I set the value of the previously given body property {string} to {string}
```

This can manipulate a previously given body by exchanging a JSON element with the given value.
**Requires to set the body before!**

The property can be a single string or a JSON path. JSON paths can be written directly as `mystructure.myarray[0].element` or
with the JSON path notation `$.mystructure.myarray[0].element`.

##### bdd_lib_numbers
If a numeric value is needed, it is possible to use the reserved word `bdd_lib_numbers` in the value fields like this:
```gherkin
Scenario:
  When I set the value of the previously given body property "mynumber" to "4 bdd_lib_numbers"
```

This sets the field `mynumber` to `1234`. The first number is then the number of how many numbers should be used.
With the `bdd_lib_numbers` it creates a number that repeats `1234567890` until the limit is reached (or cuts it off before).


##### bdd_lib_uuid
This reserved word creates a random uuid for the field.


#### Execute requests

A list and description of sentences to execute a request can be found at [docs/httpmethod_sentences.md](docs/httpmethod_sentences.md).

### Then

#### Additional validation configuration

Partly there is a requirement to be more flexible with JSON responses, such as:

- Ignore additional fields in the response
- Ignore sorting of arrays
- Ignore additional array elements

This can be achieved using annotations or sentences.

[docs/json_validation_configuration.md](docs/json_validation_configuration.md)


#### Wait for some ms
Sometimes it is required to wait for some time (e.g. for backend processing).
With the following set you can wait a certain time before continuing.

```gherkin
  Scenario: Wait for something
    Then I wait for 1000 ms
```

Examples at:
[src/test/resources/features/performance/performance.feature](src/test/resources/features/performance/performance.feature)


#### Validate execution time of requests
```gherkin
Scenario:
  Then I ensure that the execution time is less than {long} ms
```

Validates, that the execution of the Scenario has taken less than [n] ms.

Please have a look to the examples at: [src/test/resources/features/performance/](src/test/resources/features/performance/).

#### Validate HTTP response code
```gherkin
Scenario:
  Then I ensure that the status code of the response is {int}
```

Validates, that the response is the expected HTTP code (e.g. `200`).


#### Validate response body with JSON file
```gherkin
Scenario:
  Then I ensure that the body of the response is equal to the file {string}
```

Validates, that the body of the response is equal to the given file.
Like mentioned above, this file can contain [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax for dynamic field validation.

#### Validate response body with given String
```gherkin
Scenario:
  Then I ensure that the body of the response is equal to
    """
    {
      "field": "value",
    }
    """
```

In this case, the JSON is written directly under the sentence and enclosed in three double quotation marks.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic elements.


#### Validate response HTTP header contains a value
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


#### Validate response HTTP code and body together
```gherkin
Scenario:
  Then I ensure that the response code is 201 and the body is equal to
    """
    {
      "field": "value",
    }
    """
```

In this case, the response status code is part of the sentence, and the JSON is written directly under the sentence and enclosed in three double quotation marks.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic elements.


#### Validate response HTTP code and body together with a JSON file
```gherkin
Scenario:
  Then I ensure that the response code is 200 and the body is equal to the file "response.json"
```

In this case, the response status code, and the JSON file are written together in one sentence.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic elements.


#### Validate only special fields of the response body
##### Validation of one field
```gherkin
  Scenario: Validate field of the body
    Then I ensure that the body of the response contains a field "list[0]" with the value "First"
    And I ensure that the body of the response contains a field "$.list[1]" with the value "Second"
    Then I ensure that the body of the response contains a field "shouldNotExist" with the value "@bdd_lib_not_exist"
```

##### Validation of multiple fields
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

##### Description
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

Find examples for this feature under: [src/test/resources/features/body_validation/](src/test/resources/features/body_validation/).


#### Read from Response and set it to a `Feature` context
```gherkin
Scenario:
  Then I store the string of the field {string} in the context {string} for later usage
```

**Attention: This is an [Anti-Pattern](https://cucumber.io/docs/guides/anti-patterns/)!**

This can be used to write the value of a JSON element of the response to a context that is available through the `Feature` or other `Scenarios`.

Use this with caution, because at the point where the element is reused, the `Scenario` is hard coupled to this `Step` which ultimately makes it not executable as single `Step`.
On the other hand, this can be useful to support cross-`Features` testing with dynamic values for end-to-end testing.
