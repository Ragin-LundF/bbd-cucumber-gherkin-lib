# Cucumber REST Gherkin library

This library supports some basic sentences to handle REST API calls and basic database operations.

It is based on [Cucumber](https://cucumber.io) and helps to support [Behaviour-Driven Development (BDD)](https://cucumber.io/docs/bdd/).


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
This is very helpful for IDE's with code completion.

| Step | Sentence start |
| --- | --- |
| `Given` | `that` |
| `And` | `that` (reuse of given) |
| `When` | `executing a` or `I set` |
| `Then` | `I ensure` or `I store` |

# Steps
## Database
Before every Scenario the library looks for a `database/reset_database.xml` file (`$projectDir/src/test/resources/database/reset_database.xml`).
This file has to be a [Liquibase](https://www.liquibase.org) definition, which can contain everything to reset a database (`truncate`, `delete`, `insert`...).

### Given
#### Liquibase script initialization
```gherkin
Given: that the database was initialized with the liquibase file {string}
```

Executes a liquibase script to prepare the database.

### Then / And
#### Database data comparision
```gherkin
Then: I ensure that the result of the query of the file {string} is equal to the CSV file {string}
```

Executes an SQL query from a file and compares the result in CSV format with the contents of the second file.
The conversion of the database result to CSV is done internally.

## REST
The REST API steps can be prepared with some given steps.

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
Given: that all file paths are relative to {string}
```

Sets an internal `base file path` for all files in the `Scenario` or `Feature`.
It is used for:
- Request files
- Response files (for compare)
- Database query files (`.sql`)
- Database CSV result files (`.csv`)

#### Set base path for URLs
```gherkin
Given: that all URLs are relative to {string}
```

Sets an internal `base URL path` for all URLs in the `Scenario` or `Feature`.
This is very useful to avoid repeating e.g. `/api/v1/myapitotest` before every concrete endpoint.

#### Define that a token without scopes should be used.
```gherkin
Given: that a bearer token without scopes is used
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
(can also be used as `And:` step, if a `Given:` step has been written before)
```gherkin
Given: that the API path is {string}
```
This sets a URI, path which can be executed later.
It is required to use this `Given`/`And` step in cases when it is necessary to manipulate e.g. dynamic elements in the URI.

#### Set a body from JSON file for later execution
(can also be used as `And:` step, if a `Given:` step has been written before)
```gherkin
Given: that the file {string} is used as the body
```

This sets the JSON file for the body for later execution.
It is required to use this `Given`/`And` step in cases when it is necessary to manipulate e.g. dynamic elements in the URI.

