Feature: Authorization with PATCH tests
  Background:
    Given that all file paths are relative to "features/auth_and_methods/"


  Scenario: Unauthorized PATCH call with direct API path and inner body should be rejected with a 401 error
    Given that the body of the request is
    """
    {
      "name": "John Doe"
    }
    """
    When executing a PATCH call to "/api/v1/unauthorized" with previously given body
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to
    """
    {
      "error": "unauthorized",
      "error_description": "Full authentication is required to access this resource"
    }
    """


  Scenario: Unauthorized PATCH call with direct API path and file as body should be rejected with a 401 error
    When executing a PATCH call to "/api/v1/unauthorized" with the body from file "requests/request.json"
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to
    """
    {
      "error": "unauthorized",
      "error_description": "Full authentication is required to access this resource"
    }
    """


  Scenario: Unauthorized PATCH call with previously given API path and previously given files as body should be rejected with a 401 error
    Given that the API path is "/api/v1/unauthorized"
    And that the file "requests/request.json" is used as the body
    When executing a PATCH call with previously given URI and body
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to the file "responses/response_unauthorized.json"


  Scenario: Unauthorized PATCH call with previously given API path and dynamic path elements is successful
    Given that the API path is "/api/v1/{apiPath}"
    And that the file "requests/request.json" is used as the body
    When executing a PATCH call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values |
      | apiPath       | unauthorized |
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to the file "responses/response_unauthorized.json"


  Scenario: Authorized PATCH call with previously given API path and dynamic path elements from context is successful
    Given that the context contains the key "apiPath" with the value "unauthorized"
    And that the API path is "/api/v1/${apiPath}"
    And that the file "requests/request.json" is used as the body
    When executing a PATCH call with previously given URI and body
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to the file "responses/response_unauthorized.json"


  Scenario: Authorized PATCH call with direct API path is successful
    Given that the file "requests/request.json" is used as the body
    When executing an authorized PATCH call to "/api/v1/authorized" with previously given body
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "responses/response_authorized.json"


  Scenario: Authorized PATCH call with previously given API path is successful
    Given that the API path is "/api/v1/authorized"
    And that the file "requests/request.json" is used as the body
    When executing an authorized PATCH call with previously given URI and body
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "responses/response_authorized.json"


  Scenario: Authorized PATCH call with previously given API path and dynamic path elements is successful
    Given that the API path is "/api/v1/{apiPath}"
    And that the file "requests/request.json" is used as the body
    When executing an authorized PATCH call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values |
      | apiPath       | authorized |
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "responses/response_authorized.json"


  Scenario: Authorized PATCH call with previously given API path and dynamic path elements from context is successful
    Given that the context contains the key "apiPath" with the value "authorized"
    And that the file "requests/request.json" is used as the body
    And that the API path is "/api/v1/${apiPath}"
    When executing an authorized PATCH call with previously given URI and body
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to the file "responses/response_authorized.json"
