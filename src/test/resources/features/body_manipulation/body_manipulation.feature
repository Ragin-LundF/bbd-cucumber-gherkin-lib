Feature: Manipulation of the body
  Background:
    Given that all file paths are relative to "features/body_manipulation/"


  Scenario: Manipulate the body directly
    Given that the body of the request is
    """
    {
      "name": "John Doe",
      "ids" : [
        "first",
        "second",
        "third"
      ]
    }
    """
    And I set the value of the previously given body property "name" to "Max Doe"
    And I set the value of the previously given body property "ids[1]" to "2."
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "Max Doe",
      "newIds" : [
        "first",
        "2.",
        "third"
      ]
    }
    """


  Scenario: Manipulate the body from a context variable
    Given that the file "requests/request.json" is used as the body
    And that the context contains the key "newUserNameInContext" with the value "Max Done"
    Given that the context contains the key "secondEntry" with the value "unknown"
    Then I set the value of the previously given body property "name" to "newUserNameInContext"
    And I set the value of the previously given body property "ids[1]" to "secondEntry"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "Max Done",
      "newIds" : [
        "first",
        "unknown",
        "third"
      ]
    }
    """


  Scenario: Manipulate the body from a context variable and compare it by context matcher
    Given that the file "requests/request.json" is used as the body
    And that the context contains the key "newUserNameInContext" with the value "Max Done"
    Given that the context contains the key "secondEntry" with the value "unknown"
    Then I set the value of the previously given body property "name" to "newUserNameInContext"
    And I set the value of the previously given body property "ids[1]" to "secondEntry"
    # It is also possible to write the JSON path with official notation.
    And I set the value of the previously given body property "$.ids[2]" to "thirdEntry"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "${json-unit.matches:isEqualToScenarioContext}newUserNameInContext",
      "newIds" : [
        "first",
        "${json-unit.matches:isEqualToScenarioContext}secondEntry",
        "thirdEntry"
      ]
    }
    """


  Scenario: Manipulate the body and remove the name
    Given that the file "requests/request.json" is used as the body
    And I set the value of the previously given body property "name" to "null"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newIds" : [
        "first",
        "second",
        "third"
      ]
    }
    """

  Scenario: Manipulate the body with name as 20 numbers
    Given that the file "requests/request.json" is used as the body
    And I set the value of the previously given body property "name" to "20 bdd_lib_numbers"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "12345678901234567890",
      "newIds" : [
        "first",
        "second",
        "third"
      ]
    }
    """

  Scenario: Manipulate the body with name as 3 numbers
    Given that the file "requests/request.json" is used as the body
    And I set the value of the previously given body property "name" to "3 bdd_lib_numbers"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "123",
      "newIds" : [
        "first",
        "second",
        "third"
      ]
    }
    """

  Scenario: Manipulate the body with name as UUID
    Given that the file "requests/request.json" is used as the body
    And I set the value of the previously given body property "name" to "bdd_lib_uuid"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "${json-unit.matches:isValidUUID}",
      "newIds" : [
        "first",
        "second",
        "third"
      ]
    }
    """
