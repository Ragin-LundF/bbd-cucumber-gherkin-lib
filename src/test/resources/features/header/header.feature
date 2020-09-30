Feature: Header manipulation

  Scenario: Add custom header
    Given I set the header "X-My-Custom-Header" to "ABC_DEF"
    When executing an authorized GET call to "/api/v1/customHeader"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "header": "ABC_DEF"
    }
    """


  Scenario: Set own/dynamic authorization header
    Given I set the header "Authorization" to "MyAuth"
    When executing a GET call to "/api/v1/overwrittenAuthHeader"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "header": "MyAuth"
    }
    """


  Scenario: Set authorization header from context map
    Given that the context contains the key "DYAMIC_TOKEN" with the value "MyTokenInTheContext"
    And I set the header "Authorization" to "DYAMIC_TOKEN"
    When executing a GET call to "/api/v1/overwrittenAuthHeader"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "header": "MyTokenInTheContext"
    }
    """


  Scenario: Overwrite authorization header
    Given I set the header "Authorization" to "Bearer abcdefg"
    When executing an authorized GET call to "/api/v1/overwrittenAuthHeader"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "header": "Bearer abcdefg"
    }
    """

