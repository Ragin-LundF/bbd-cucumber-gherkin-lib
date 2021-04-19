Feature: Some scenarios are ignored

  Scenario: Not ignored test
    When executing a GET call to "/api/v1/unauthorized"
    Then I ensure that the status code of the response is 401
    And I ensure that the body of the response is equal to
    """
    {
      "error": "unauthorized",
      "error_description": "Full authentication is required to access this resource"
    }
    """

  @ignore
  Scenario: Ignored test
    When executing a GET call to "/api/v1/unauthorized"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "error": "unauthorized",
      "error_description": "Full authentication is required to access this resource"
    }
    """
