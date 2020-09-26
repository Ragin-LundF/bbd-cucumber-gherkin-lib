Feature: E2E Stateful 01 - Execute calls against an API and store it to the context for later usage

  Scenario: Call the dynamic path endpoints and remember value in context for other features
    Given that the stored data in the scenario context map has been reset
    Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
    When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values            |
      | resourceId    | myStaticResourceId    |
      | subResourceId | myStaticSubResourceId |
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to
    """
    {
      "resourceId": "${json-unit.ignore}",
      "subResourceId": "${json-unit.ignore}",
      "ignorableValue": "${json-unit.ignore}",
      "regexValue": "${json-unit.regex}[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
      "validDate": "${json-unit.matches:isValidDate}"
    }
    """
    And I store the string of the field "resourceId" in the context "staticResourceId" for later usage
    And I store the string of the field "subResourceId" in the context "staticSubResourceId" for later usage


  Scenario Outline: Call the dynamic path endpoints with outlines and remember value in context for other features
    Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
    When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements  | URI Values    |
      | resourceId    | <resource>    |
      | subResourceId | <subresource> |
    Then I ensure that the status code of the response is 201
    And I store the string of the field "resourceId" in the context "<contextResourceName>" for later usage
    And I store the string of the field "subResourceId" in the context "<contextSubResourceName>" for later usage
    Examples:
      | resource | subresource | contextResourceName      | contextSubResourceName      |
      | abc-def  | ghi-jkl     | firstOutlineCtxResource  | firstOutlineCtxSubResource  |
      | ghj-oid  | jks-dfg     | secondOutlineCtxResource | secondOutlineCtxSubResource |
