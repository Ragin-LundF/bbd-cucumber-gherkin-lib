Feature: Custom matcher tests
  Scenario: Using a simple and parameterized matcher
    Given that the context contains the following 'key' and 'value' pairs
      | resourceId    | abc-def |
      | subResourceId | ghi-jkl |
    And that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "${json-unit.matches:isInContextAvailable}resourceId",
      "subResourceId": "${json-unit.matches:isInContextAvailable}subResourceId",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.matches:isUUID}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """

  Scenario Outline: Using a simple and parameterized matcher with outline
    Given that the context contains the following 'key' and 'value' pairs
      | <firstContextKey>    | <firstContextValue>  |
      | <secondContextKey>   | <secondContextValue> |
    And that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "${json-unit.matches:isInContextAvailable}<firstContextKey>",
      "subResourceId": "${json-unit.matches:isInContextAvailable}<secondContextKey>",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.matches:isUUID}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
    Examples:
    | firstContextKey | firstContextValue | secondContextKey | secondContextValue |
    | resourceId      | abc-def           | subResourceId    | ghi-jkl            |

  Scenario: Using a multi parameterized matcher
    Given that the context contains the following 'key' and 'value' pairs
      | resourceId    | abc-def |
      | subResourceId | ghi-jkl |
    And that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "${json-unit.matches:containsOneOf}{\"firstArgument\": \"abc-def\", \"secondArgument\": \"ghi-jkl\"}",
      "subResourceId": "${json-unit.matches:containsOneOf}{\"firstArgument\": \"abc-def\", \"secondArgument\": \"ghi-jkl\"}",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.matches:isUUID}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
