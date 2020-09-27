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


  Scenario: Execute API call to endpoint with dynamic resources and manipulate them with data table and data fom context
    Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
    And that the context contains the key "ctxResourceId" with the value "abc-def"
    And that the context contains the key "ctxSubResourceId" with the value "ghi-jkl"
    When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values       |
      | resourceId    | ctxResourceId    |
      | subResourceId | ctxSubResourceId |
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
      "regexValue": "${json-unit.matches:isValidUUID}",
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
    And that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to the file "responses/response.json"


  Scenario Outline: Execute API calls to endpoint with dynamic resources and manipulate them with the context from a static data table
    Given that the context contains the following 'key' and 'value' pairs
      | resourceId    | <resource>    |
      | subResourceId | <subresource> |
    And that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "<resource>",
      "subResourceId": "<subresource>",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.matches:isValidUUID}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
    Examples:
      | resource | subresource |
      | abc-def  | ghi-jkl     |
      | ghj-oid  | jks-dfg     |


  Scenario: Execute API call to endpoint with dynamic resources and manipulate them with the context from static keys
    Given that the context contains the key "resourceId" with the value "abc-def"
        * that the context contains the key "subResourceId" with the value "ghi-jkl"
        * that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the response code is 201 and the body is equal to the file "responses/response.json"


  Scenario Outline: Execute API calls to endpoint with dynamic resources and manipulate them with the context from static keys
    Given that the context contains the key "resourceId" with the value "<resource>"
        * that the context contains the key "subResourceId" with the value "<subresource>"
        * that the API path is "/api/v1/${resourceId}/${subResourceId}"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the response code is 201 and the body is equal to
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