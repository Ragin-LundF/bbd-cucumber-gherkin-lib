Feature: Global configuration

  Scenario: Manipulate the body with values from global config
    Given that the body of the request is
    """
    {
      "name": "John Doe",
      "ids" : [
        "first",
        "second",
        "third"
      ]
    }
    """
      * I set the value of the previously given body property "name" to "CTX_PRE_DEFINED_USER"
      * I set the value of the previously given body property "ids[0]" to "CTX_PRE_DEFINED_FIRST_ID"
    When executing an authorized POST call to "/api/v1/body/manipulate" with previously given body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "Pre Defined",
      "newIds" : [
        "abcdefg",
        "second",
        "third"
      ]
    }
    """
