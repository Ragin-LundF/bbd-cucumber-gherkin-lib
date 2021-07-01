Feature: Performance measurement

  Scenario: Bad Performance
    When executing a GET call to "/api/v1/performance/bad"
    Then I ensure that the status code of the response is 200
    And I ensure that the execution time is less than 600 ms

  Scenario: Good Performance
    When executing a GET call to "/api/v1/performance/good"
    Then I ensure that the status code of the response is 200
    And I ensure that the execution time is less than 300 ms

  Scenario: Wait for something
    When executing a GET call to "/api/v1/performance/good"
    Then I ensure that the status code of the response is 200
    And I wait for 1000 ms

