# Additional Docs

## Configuration

Here the possibilities for the configuration of the Cucumber BDD library are described. This includes things like:

- Default token configuration
- HTTP base path setup
- Proxy support
- SSL verification
- Database-less configuration

[configuration.md](configuration.md)

## Database support

To enable end-to-end testing from the REST API to the database, the library supports various functionalities for database operations.

For example:
- Execution of Liquibase scripts.
- Validation of SQL select results

[database.md](database.md)

## REST HTTP method sentences

This documentation discusses the possibilities of sending HTTP requests using different HTTP methods.

[httpmethod_sentences.md](httpmethod_sentences.md)

## JSON Matcher

Answers can be validated with the help of JSONUnit.
In addition, there is the possibility to use matchers to simplify complex or repetitive validations or to make them more readable.

[json_matcher.md](json_matcher.md)
