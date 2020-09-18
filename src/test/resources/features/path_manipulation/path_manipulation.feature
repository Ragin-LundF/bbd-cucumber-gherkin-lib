Feature: Multiple resources

  Background: 
    Given that all file paths are relative to "features/path_manipulation/"

  Scenario: Execute API call to endpoint with dynamic resources and manipulate them with data table
    Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
    When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values |
      | resourceId    | abc-def    |
      | subResourceId | ghi-jkl    |
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to the file "responses/response.json"

  Scenario Outline: Execute API calls to endpoint with dynamic resources and manipulate them with data table
    Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
    When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values    |
      | resourceId    | <resource>    |
      | subResourceId | <subresource> |
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "<resource>",
      "subResourceId": "<subresource>",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.regex}[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
    Examples:
      | resource | subresource |
      | abc-def  | ghi-jkl     |
      | ghj-oid  | jks-dfg     |

  Scenario: Execute API call to endpoint with dynamic resources and manipulate them with the context from a static data table
    Given that the context contains the following 'key' and 'value' pairs
      | resourceId    | abc-def |
      | subResourceId | ghi-jkl |
    Given that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to the file "responses/response.json"

  Scenario Outline: Execute API calls to endpoint with dynamic resources and manipulate them with the context from a static data table
    Given that the context contains the following 'key' and 'value' pairs
      | resourceId    | <resource>    |
      | subResourceId | <subresource> |
    Given that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "<resource>",
      "subResourceId": "<subresource>",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.regex}[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
    Examples:
      | resource | subresource |
      | abc-def  | ghi-jkl     |
      | ghj-oid  | jks-dfg     |

  Scenario: Execute API call to endpoint with dynamic resources and manipulate them with the context from static keys
    Given that the context contains the key "resourceId" with the value "abc-def"
    Given that the context contains the key "subResourceId" with the value "ghi-jkl"
    Given that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to the file "responses/response.json"

  Scenario Outline: Execute API calls to endpoint with dynamic resources and manipulate them with the context from static keys
    Given that the context contains the key "resourceId" with the value "<resource>"
    Given that the context contains the key "subResourceId" with the value "<subresource>"
    Given that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "<resource>",
      "subResourceId": "<subresource>",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.regex}[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
    Examples:
    | resource | subresource |
    | abc-def  | ghi-jkl     |
    | ghj-oid  | jks-dfg     |