Feature: User features in global context
  Background:
    Given that the following users and tokens are existing
    | john_doe    | my_auth_token_for_john_doe    |
    And that the user is "john_doe"

  Scenario: Using the global authorized user john_doe
    When executing an authorized GET call to "/api/v1/user/john_doe"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "username": "john_doe",
      "token": "Bearer my_auth_token_for_john_doe"
    }
    """
