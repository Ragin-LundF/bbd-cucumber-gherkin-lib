Feature: E2E Stateful 02 - Second feature for stateful e2e test, which validates data from previous executed feature

  Scenario: Call body endpoint to validate that the data from previous static feature context is present
    Given that the API path is "/api/v1/body/manipulate"
    And that the body of the request is
    """
    {
      "name": "previously stored body values from context",
      "ids": [
        "placeholder",
        "placeholder",
      ]
    }
    """
    Then I set the value of the previously given body property "ids[0]" to "staticResourceId"
    And I set the value of the previously given body property "ids[1]" to "staticSubResourceId"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "previously stored body values from context",
      "newIds" : [
        "myStaticResourceId",
        "myStaticSubResourceId"
      ]
    }
    """


  Scenario Outline: Call body endpoint to validate that the data from previous dynamic feature context is present
    Given that the API path is "/api/v1/body/manipulate"
    And that the body of the request is
    """
    {
      "name": "previously stored body values from context",
      "ids": [
        "placeholder",
        "placeholder",
      ]
    }
    """
    Then I set the value of the previously given body property "ids[0]" to "<contextResourceName>"
    And I set the value of the previously given body property "ids[1]" to "<contextSubResourceName>"
    When executing an authorized POST call with previously given URI and body
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "newName": "previously stored body values from context",
      "newIds" : [
        "<resource>",
        "<subresource>"
      ]
    }
    """
    Examples:
      | resource | subresource | contextResourceName      | contextSubResourceName      |
      | abc-def  | ghi-jkl     | firstOutlineCtxResource  | firstOutlineCtxSubResource  |
      | ghj-oid  | jks-dfg     | secondOutlineCtxResource | secondOutlineCtxSubResource |
