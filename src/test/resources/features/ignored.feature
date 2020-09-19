@ignore
Feature: This test was ignored

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
