Feature: Field validation
  Scenario: Validate field with a string
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "string" with the value "is a string"

  Scenario: Validate field with a string that it is not the value
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "string" with the value "@bdd_lib_not is this string"

  Scenario: Validate field with a number
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "number" with the value "12"

  Scenario: Validate field with a custom JSON Assert matcher
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "uuid" with the value "${json-unit.matches:isUUID}"

  Scenario: Validate field with a boolean
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "boolean" with the value "true"

  Scenario: Validate fields with elements from a list with and without JSON path notation
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "list[0]" with the value "First"
    And I ensure that the body of the response contains a field "$.list[1]" with the value "Second"

  Scenario: Validate field that should not exist
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    Then I ensure that the body of the response contains a field "shouldNotExist" with the value "@bdd_lib_not_exist"

  Scenario: Validate field with list
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "list" with the value "[\"First\",\"Second\"]"

  Scenario: Validate field with object elements
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "object.firstname" with the value "John"
    And I ensure that the body of the response contains a field "object.lastname" with the value "Doe"

  Scenario: Validate field with object elements
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response contains a field "object" with the value "{firstname=John, lastname=Doe}"
    # set this value to the context to test, if context is used in the next scenario
    And I store the string of the field "$.list[0]" in the context "BDD_TEST_LIST_FIRST_ELEMENT" for later usage

  Scenario: Validate fields with object elements
    When executing a GET call to "/api/v1/fieldValidation"
    Then I ensure that the status code of the response is 200
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
