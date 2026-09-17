@multi_port
Feature: Call endpoints that run on a second port
  The demo application starts the actuator on its own random port.
  Neither protocol, host nor port appear in these scenarios.

  Scenario: The management port is reached without any URL in the test
    Given that the response JSON can contain extra fields
    When executing a GET call to "/actuator/health"
    Then I ensure that the status code of the response is 200
    And I ensure that the body of the response is equal to
    """
    {
      "status": "UP"
    }
    """


  Scenario: Select the service explicitly
    Given that the service "management" is used
    When executing a GET call to "/actuator/health"
    Then I ensure that the status code of the response is 200


  Scenario: The application port stays the default for every other path
    When executing an authorized GET call to "/api/v1/authorized"
    Then I ensure that the status code of the response is 200


  Scenario: Switch back to the application from an explicitly selected service
    Given that the service "management" is used
    And that the service "server" is used
    When executing an authorized GET call to "/api/v1/authorized"
    Then I ensure that the status code of the response is 200
