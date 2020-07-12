Feature: Multiple resources

  Background: 
    Given that all file paths are relative to "features/mutlipath/"
  
  Scenario: Execute API call to endpoint with multiple resources
    Given that the API path is "/api/v1/{resourceId}/{subResourceId}"
    And that the file "response.json" is used as the body
    When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
      | URI Elements | URI Values |
      | resourceId | abc-def |
      | subResourceId | ghi-jkl |
    Then I ensure that the status code of the response is 201
    And I ensure that the body of the response is equal to the file "response.json"