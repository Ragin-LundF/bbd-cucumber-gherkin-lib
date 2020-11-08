# Cucumber REST Gherkin library

![Java CI with Gradle](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ragin-LundF_bbd-cucumber-gherkin-lib&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ragin-LundF_bbd-cucumber-gherkin-lib)

This library supports some basic sentences to handle REST API calls and basic database operations.

It is based on [Cucumber](https://cucumber.io) and helps to support [Behaviour-Driven Development (BDD)](https://cucumber.io/docs/bdd/).

Cucumber executes `Steps` in form of [Gherkin](https://cucumber.io/docs/gherkin/) language.

Read also about [Anti-Patterns](https://cucumber.io/docs/guides/anti-patterns/) of Cucumber to avoid problems and to have a clear style.

See [Changelog](CHANGELOG.md) for release information.

The library tests with itself and with a dummy application in the test sources to have a lot of examples for the usage.
There you can also find custom matcher for JSON-Assert.
See [src/test](src/test) folder for examples.

## How to integrate

The library is available on jcenter. Please add jcenter to your build tool.

### Maven
```xml
<dependency>
	<groupId>com.ragin.bdd</groupId>
	<artifactId>bdd-cucumber-gherkin-lib</artifactId>
	<version>${version.bdd-cucumber-gherkin-lib}</version>
	<scope>test</scope>
</dependency>
```

### Gradle
```groovy
dependencies {
    testImplementation "com.ragin.bdd:bdd-cucumber-gherkin-lib:${version.bdd-cucumber-gherkin-lib}"
}
```

# Table of content

- [Cucumber REST Gherkin library](#cucumber-rest-gherkin-library)
- [Table of content](#table-of-content)
- [Support JUnit 5](#support-junit-5)
- [Base Configuration](#base-configuration)
  - [Base token definition](#base-token-definition)
  - [Base URL definition](#base-url-definition)
- [Basic Concept](#basic-concept)
- [Steps](#steps)
  - [Database](#database)
    - [Given](#given)
      - [Liquibase script initialization](#liquibase-script-initialization)
      - [SQL statement execution](#sql-statement-execution)
    - [Then](#then)
      - [Database data comparison](#database-data-comparison)
  - [REST](#rest)
    - [JSON-Unit](#json-unit)
      - [Adding own pattern for `${json-unit.matches:isValidDate}`](#adding-own-pattern-for-json-unitmatchesisvaliddate)
    - [Given](#given-1)
      - [Define user(s)](#define-users)
      - [Set path base directory for request/result/database files](#set-path-base-directory-for-requestresultdatabase-files)
      - [Set a static value to the context](#set-a-static-value-to-the-context)
      - [Set multiple static values to the context](#set-multiple-static-values-to-the-context)
      - [Set base path for URLs](#set-base-path-for-urls)
      - [Define that a token without scopes should be used.](#define-that-a-token-without-scopes-should-be-used)
      - [Set a URI path for later execution](#set-a-uri-path-for-later-execution)
      - [Set a body from JSON file for later execution](#set-a-body-from-json-file-for-later-execution)
      - [Set a body directly for later execution](#set-a-body-directly-for-later-execution)
      - [Configure the JSON compare to ignore extra elements in arrays](#configure-the-json-compare-to-ignore-extra-elements-in-arrays)
      - [Configure the JSON compare to ignore the order of arrays](#configure-the-json-compare-to-ignore-the-order-of-arrays)
      - [Configure the JSON compare to ignore extra fields](#configure-the-json-compare-to-ignore-extra-fields)
    - [When](#when)
      - [Header manipulation](#header-manipulation)
      - [Body manipulation](#body-manipulation)
      - [Execute requests](#execute-requests)
    - [Then](#then-1)
      - [Validate HTTP response code](#validate-http-response-code)
      - [Validate response body with JSON file](#validate-response-body-with-json-file)
      - [Validate response body with given String](#validate-response-body-with-given-string)
      - [Validate response HTTP code and body together](#validate-response-http-code-and-body-together)
      - [Validate response HTTP code and body together with a JSON file](#validate-response-http-code-and-body-together-with-a-json-file)
      - [Validate only special fields of the response body](#validate-only-special-fields-of-the-response-body)
      - [Read from Response and set it to a `Feature` context](#read-from-response-and-set-it-to-a-feature-context)
- [Extension of JSON Unit Matcher](#extension-of-json-unit-matcher)
  - [Simple matcher](#simple-matcher)
  - [Matcher with parameter](#matcher-with-parameter)
  
# Support JUnit 5
To support JUnit 5 add the `org.junit.vintage:junit-vintage-engine` dependency to your project.
This allows JUnit 5 to execute JUnit 3 and 4 tests.

```groovy
testRuntimeOnly('org.junit.vintage:junit-vintage-engine') {
    because("allows JUnit 3 and JUnit 4 tests to run")
}
```

# Base Configuration

## Base token definition
To define a default token those two parameters can be set in the properties:

`application.properties`:
```properties
cucumberTest.authorization.bearerToken.default=default_token_for_authorized_endpoints 
cucumberTest.authorization.bearerToken.noscope=second_token_for_alternatives
```

or

`application.yaml`:
```yaml
cucumberTest:
  authorization:
    bearerToken:
      default: "default_token_for_authorized_endpoints" 
      noscope: "second_token_for_alternatives"
```


## Base URL definition
If the test library should be used for various tests in an outsourced test repository.

It is possible to overwrite the `protocol`, `host` and `port` in the `application.yaml`/`application.properties`:

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

# Basic Concept

This library defines a set of sentences and tries to harmonize them and provide a context-related beginning of sentences.
This is very helpful for IDEs with code completion.

| Step | Sentence start | Main usage |
| --- | --- | --- |
| `Given` | `that` | Prepare something |
| `When` | `executing a` or `I set` | Do something |
| `Then` | `I ensure` or `I store` | Validate something |

There are some basic examples in the [src/test](src/test) directory.


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
Before every Scenario the library looks for a `database/reset_database.xml` file (`$projectDir/src/test/resources/database/reset_database.xml`).
This file has to be a [Liquibase](https://www.liquibase.org) definition, which can contain everything to reset a database (`truncate`, `delete`, `insert`...).

The executor of the Liquibase scripts has sometimes problems with H2 memory databases, that the H2 connection is closed after the JDBC connection is closed.

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


### Given
#### Liquibase script initialization
```gherkin
Scenario:
  Given that the database was initialized with the liquibase file {string}
```

Executes a liquibase script to prepare the database.

#### SQL statement execution
```gherkin
Scenario:
    Given that the SQL statements from the SQL file {string} was executed
```

Executes an SQL script to prepare/change the database.

### Then
#### Database data comparison
```gherkin
Scenario:
  Then I ensure that the result of the query of the file {string} is equal to the CSV file {string}
```

Executes an SQL query from a file and compares the result in CSV format with the contents of the second file.
The conversion of the database result to CSV is done internally.

## REST
The REST API steps can be prepared with some given steps.

If a path contains a template placeholder with `${}` like `${elementFromContext}` the library tries to replace this with `elementFromContext` in the context, if it exists. 

Files can be added as relative path to a previously given base path or with an "absolute" path with the prefix `absolutePath:`.
In the last case the system is using the base classpath as root.

### JSON-Unit

The library contains already two matchers:
- `${json-unit.matches:isValidDate}` which checks, if the date can be a valid date by parsing it into date formats
- `${json-unit.matches:isValidUUID}` which checks, if the string is a valid UUID
- `${json-unit.matches:isEqualToScenarioContext}create_id` which compares the content of the actual JSON to a variable in the ScenarioContext.
  The context has to be set before with the [I store the string of the field "<field>" in the context "<context-id>" for later usage](#read-from-response-and-set-it-to-a-feature-context) sentence.

There are more details about how to extend it at the [Extension of JSON Unit Matcher](#extension-of-json-unit-matcher) section.

For the comparison of the results the library uses `JSON` files, which can be enhanced with [JSON Unit](https://github.com/lukas-krecan/JsonUnit) to validate dynamic responses with things like
- Regex compare
- Ignoring values
- Ignoring elements
- Ignoring paths
- Type placeholders
- Custom matchers
- ...

**_ATTENTION: Only unparameterized custom matchers or bdd lib-matchers can be used for field validation!_**

#### Adding own pattern for `${json-unit.matches:isValidDate}`

To add own `DateTimeFormatter` patterns to extend the `${json-unit.matches:isValidDate}` range
a new class is required that implements the interface `BddCucumberDateTimeFormat`.
The method `pattern()` must return the date patterns as `List<String>`.

To register this custom class it is necessary to add it `@ContextConfiguration` `classes` definition.

Example:

- [Configuration context](src/test/java/com/ragin/bdd/cucumbertests/hooks/CreateContextHooks.java)
- [Test feature](src/test/resources/features/body_validation/field_compare.feature)


### Given

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

#### Configure the JSON compare to ignore extra elements in arrays
```gherkin
Scenario:
  Given that the response JSON can contain arrays with extra elements
```

_It is also possible to use the `@bdd_lib_json_ignore_new_array_elements` annotation on `Feature` or `Scenario` level._


With this sentence or annotation, the JSON comparison will ignore new array elements.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.


#### Configure the JSON compare to ignore the order of arrays
```gherkin
Scenario:
  Given that the response JSON can contain arrays in a different order
```

_It is also possible to use the `@bdd_lib_json_ignore_array_order` annotation on `Feature` or `Scenario` level._


With this sentence or annotation, the JSON comparison will ignore the order of arrays.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.


#### Configure the JSON compare to ignore extra fields
```gherkin
Scenario:
  Given that the response JSON can contain extra fields
```

_It is also possible to use the `@bdd_lib_json_ignore_extra_fields` annotation on `Feature` or `Scenario` level._


With this sentence or annotation, the JSON comparison will ignore new/not defined fields in the response.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.


### When
The paths that are used here can be shortened by set a base URL path with [Set base path for URLs](#set-base-path-for-urls) with a `Given` Step before.

#### Header manipulation
```gherkin
Scenario:
  When I set the header {string} to {string}
```

This sentence allows adding or manipulating headers. The first argument is the header name and the second the header value.

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

# Extension of JSON Unit Matcher

It is possible to extend the JSON matchers by creating a new matcher and extending the `org.hamcrest.BaseMatcher` class and implementing the `com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher` interface.

After they are created, you have to add them to the `@ContextConfiguration` classes definition.
See [CreateContextHooks.java](src/test/java/com/ragin/bdd/cucumbertests/hooks/CreateContextHooks.java) for an example how the configuration should look like.

## Simple matcher
A simple matcher to validate the current object as it is, can look like this:

```java
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

@Component
public class DividableByTwoMatcher extends BaseMatcher<Object> implements BddCucumberJsonMatcher {
    public boolean matches(Object item) {
        if (StringUtils.isNumeric(String.valueOf(item))) {
            // never do that, but it should show something ;)
            return Integer.parseInt((String) item) % 2 == 0;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        // nothing to describe here
    }

    @Override
    public String matcherName() {
        return "isDividableByTwo";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }
}
```

Now you can use this matcher with the following statement in your expected JSON:

```json
{
  "number": "${json-unit.matches:isDividableByTwo}"
}
```

The `matcherName()` result is now part of the `json-unit.matches:` definition.

## Matcher with parameter

If you need parameter, you can implement also the `net.javacrumbs.jsonunit.core.ParametrizedMatcher` interface.
If there are several arguments, you can pass the arguments as JSON to the matcher and parse it here.
 
```java
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

@Component
public class DividableByNumberMatcher extends BaseMatcher<Object> implements ParametrizedMatcher, BddCucumberJsonMatcher {
    private String parameter;

    public boolean matches(Object item) {
        if (StringUtils.isNumeric(String.valueOf(item))) {
            // never do that, but it should show something ;)
            return Integer.parseInt((String) item) % Integer.parseInt(parameter) == 0;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        // nothing to describe here
    }

    @Override
    public String matcherName() {
        return "isDividableByNumber";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }

    @Override
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
```

To pass the parameter to the matcher, the JSON has to look like this:


```json
{
  "number": "${json-unit.matches:isDividableByNumber}5"
}
```

If you want to pass a JSON, you have to do it with single quotes:


```json
{
  "number": "${json-unit.matches:isDividableByNumber}{\"myarg1\": \"A\"}"
}
```
