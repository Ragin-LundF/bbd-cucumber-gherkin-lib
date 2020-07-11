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

#### Database comparision
```gherkin
I ensure that the result of the query of the file {string} is equal to the CSV file {string}
```

Executes an SQL query from a file and compares the result in CSV format with the contents of the second file.
The conversion of the database result to CSV is done internally.

