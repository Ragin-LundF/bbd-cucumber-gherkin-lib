# Cucumber REST Gherkin library

![Java CI with Gradle](https://github.com/Ragin-LundF/bbd-cucumber-gherkin-lib/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ragin-LundF_bbd-cucumber-gherkin-lib&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ragin-LundF_bbd-cucumber-gherkin-lib)

This library supports some basic sentences to handle REST API calls and basic database operations.

It is based on [Cucumber](https://cucumber.io) and helps to support [Behaviour-Driven Development (BDD)](https://cucumber.io/docs/bdd/).

Cucumber executes `Steps` in form of [Gherkin](https://cucumber.io/docs/gherkin/) language.

Read also about [Anti-Patterns](https://cucumber.io/docs/guides/anti-patterns/) of Cucumber to avoid problems and to have a clear style.

See [Changelog](CHANGELOG.md) for release information.

# Table of content

- [Cucumber REST Gherkin library](#cucumber-rest-gherkin-library)
- [Table of content](#table-of-content)
- [Support JUnit 5](#support-junit-5)
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
    - [Given](#given-1)
      - [Set path base directory for request/result/database files](#set-path-base-directory-for-requestresultdatabase-files)
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
      - [Validate HTTP response code](#validate-http-response-code)
      - [Validate response body with JSON file](#validate-response-body-with-json-file)
      - [Validate response body with given String](#validate-response-body-with-given-string)
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

### JSON-Unit

The library contains already two matchers:
- `${json-unit.matches:isValidDate}` which checks, if the date can be a valid date by parsing it into date formats
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

### Given
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

#### Set base path for URLs
```gherkin
Scenario:
  Given that all URLs are relative to {string}
```

Sets an internal `base URL path` for all URLs in the `Scenario` or `Feature`.
This is very useful to avoid repeating e.g. `/api/v1/myapitotest` before every concrete endpoint.

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
  Given that the body of the response is
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
**Requires the `Step` [Set a body from JSON file for later execution](#set-a-body-from-json-file-for-later-execution)!**

#### Execute requests

A list and description of sentences to execute a request is linked in the next table.

| Request type | Link |
| --- | --- |
| GET Requests | [docs/get_sentences.md](docs/get_sentences.md) |
| POST Requests | [docs/post_sentences.md](docs/post_sentences.md) |
| DELETE Requests | [docs/delete_sentences.md](docs/delete_sentences.md) |
| PUT Requests | [docs/put_sentences.md](docs/put_sentences.md) |
| PATCH Requests | [docs/patch_sentences.md](docs/patch_sentences.md) |

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
  "number": "${json-unit.matches:isDividableByNumber}'{"myarg1": "A"}'"
}
```

