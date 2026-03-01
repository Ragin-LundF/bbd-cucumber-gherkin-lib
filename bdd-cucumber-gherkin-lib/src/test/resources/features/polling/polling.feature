Feature: Polling test
  Background:
    Given that all file paths are relative to "features/polling/responses/"

  Scenario: Polling unauthorized until response code is correct with long config
    Given that a requests polls every 1 seconds
    And that a requests polls for 5 times
    And that the API path is "/api/v1/polling"
    When executing a GET poll request until the response code is 200

  Scenario: Polling authorized until response code is correct with short config
    Given that a request polls every 1 seconds for 5 times
    And that the API path is "/api/v1/pollingAuth"
    When executing an authorized GET poll request until the response code is 200

  Scenario: Polling unauthorized until response is correct with long config
    Given that a requests polls every 1 seconds
    And that a requests polls for 5 times
    And that the API path is "/api/v1/polling"
    When executing a GET poll request until the response code is 200 and the body is equal to
    """
    {
      "message": "SUCCESSFUL"
    }
    """

  Scenario: Polling authorized until response is correct with short config
    Given that a request polls every 1 seconds for 5 times
    And that the API path is "/api/v1/pollingAuth"
    When executing an authorized GET poll request until the response code is 200 and the body is equal to
    """
    {
      "message": "SUCCESSFUL"
    }
    """

  Scenario: Polling unauthorized until response is equal to a file with long config
    Given that a requests polls every 1 seconds
    And that a requests polls for 5 times
    And that the API path is "/api/v1/polling"
    When executing a GET poll request until the response code is 200 and the body is equal to file "expected.json"

  Scenario: Polling authorized until response is equal to a file with short config
    Given that a request polls every 1 seconds for 5 times
    And that the API path is "/api/v1/pollingAuth"
    When executing an authorized GET poll request until the response code is 200 and the body is equal to file "expected.json"

  Scenario: Polling with body until response is correct with short config
    Given that a request polls every 1 seconds for 5 times
    And that the API path is "/api/v1/polling"
    And that the body of the request is
    """
    {
      "postExample": "myBody"
    }
    """
    When executing an authorized POST poll request until the response code is 200 and the body is equal to
    """
    {
      "message": "SUCCESSFUL"
    }
    """
