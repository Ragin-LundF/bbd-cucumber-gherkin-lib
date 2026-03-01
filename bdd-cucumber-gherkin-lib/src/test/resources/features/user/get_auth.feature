Feature: User features
  Background:
    Given that the following users and tokens are existing
    | john_doe    | my_auth_token_for_john_doe    |
    | johana_doe  | my_auth_token_for_johana_doen |
    | maxi_marble | my_auth_token_for_maxi_marble |

  Scenario: Using authorized user john_doe
    Given that the user is "john_doe"
    When executing an authorized GET call to "/api/v1/user/john_doe"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "username": "john_doe",
      "token": "Bearer my_auth_token_for_john_doe"
    }
    """

  Scenario: Using authorized user maxi_marble
    Given that the user is "maxi_marble"
    When executing an authorized GET call to "/api/v1/user/maxi_marble"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "username": "maxi_marble",
      "token": "Bearer my_auth_token_for_maxi_marble"
    }
    """

  Scenario Outline: Authorized test with different users
    Given that the user is "<username>"
    When executing an authorized GET call to "/api/v1/user/<username>"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "username": "<username>",
      "token": "${json-unit.regex}(Bearer .*)"
    }
    """
    Examples:
    | username    |
    | john_doe    |
    | johana_doe  |
    | maxi_marble |
