Feature: Auth should be set-up
  Scenario: Unauthorized call should be rejected with a 401 error
    When executing a GET call to "/api/v1/auth"
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to
    """
    {
      "error": "unauthorized",
      "error_description": "Full authentication is required to access this resource"
    }
    """
