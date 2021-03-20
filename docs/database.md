# Database support

- [Database](#database)
    - [Given](#given)
        - [Liquibase script initialization](#liquibase-script-initialization)
        - [SQL statement execution](#sql-statement-execution)
    - [Then](#then)
        - [Database data comparison](#database-data-comparison)

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

See examples under [src/test/resources/features/database/](../src/test/resources/features/database/).

#### SQL statement execution
```gherkin
Scenario:
    Given that the SQL statements from the SQL file {string} was executed
```

Executes an SQL script to prepare/change the database.

See examples under [src/test/resources/features/database/](../src/test/resources/features/database/).

### Then
#### Database data comparison
```gherkin
Scenario:
  Then I ensure that the result of the query of the file {string} is equal to the CSV file {string}
```

Executes an SQL query from a file and compares the result in CSV format with the contents of the second file.
The conversion of the database result to CSV is done internally.

See examples under [src/test/resources/features/database/](../src/test/resources/features/database/).
