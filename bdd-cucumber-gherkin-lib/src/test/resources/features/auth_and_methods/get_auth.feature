Feature: Authorization with GET tests
  Background:
    Given that all file paths are relative to "features/auth_and_methods/responses/"

  Scenario: Unauthorized GET data from an external source
    When executing a GET call to "https://www.googleapis.com/fitness/v1/users/me/dataSources"
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response contains a field "$.error.code" with the value "401"

  Scenario: Unauthorized GET call with direct API path should be rejected with a 401 error
    When executing a GET call to "/api/v1/unauthorized"
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to
    """
    {
      "error": "unauthorized",
      "error_description": "Full authentication is required to access this resource"
    }
    """


  Scenario: Unauthorized GET call with previously given API path should be rejected with a 401 error
    Given that the API path is "/api/v1/unauthorized"
    When executing a GET call with previously given URI
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to the file "response_unauthorized.json"


  Scenario: Unauthorized GET call with previously given API path and dynamic path elements is successful
    Given that the API path is "/api/v1/{apiPath}"
    When executing a GET call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values |
      | apiPath       | unauthorized |
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to the file "response_unauthorized.json"


  Scenario: Authorized GET call with previously given API path and dynamic path elements from context is successful
    Given that the context contains the key "apiPath" with the value "unauthorized"
    And that the API path is "/api/v1/${apiPath}"
    When executing a GET call with previously given URI
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to the file "response_unauthorized.json"


  Scenario: Authorized GET call with direct API path is successful
    When executing an authorized GET call to "/api/v1/authorized"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "response_authorized.json"


  Scenario: Authorized GET call with previously given API path is successful
    Given that the API path is "/api/v1/authorized"
    When executing an authorized GET call with previously given URI
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "response_authorized.json"


  Scenario: Authorized GET call with previously given API path and dynamic path elements is successful
    Given that the API path is "/api/v1/{apiPath}"
    When executing an authorized GET call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values |
      | apiPath       | authorized |
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "response_authorized.json"


  Scenario: Authorized GET call with previously given API path and dynamic path elements from context is successful
    Given that the context contains the key "apiPath" with the value "authorized"
    And that the API path is "/api/v1/${apiPath}"
    When executing an authorized GET call with previously given URI
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "response_authorized.json"
