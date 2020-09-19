Feature: Define the base path with Given
  Background:
    Given that all URLs are relative to "/api"


  Scenario: Use the base path from the background and use only the relevant part
    # executes /api/v1/unauthorized
    When executing an authorized GET call to "/v1/authorized"
    Then I ensure that the status code of the response is 200


  Scenario: Use the base path from the background and use only the relevant part
    # Set a base path for this scenario
    Given that all URLs are relative to "/api/v1"
    # executes /api/v1/unauthorized
    When executing an authorized GET call to "/authorized"
    Then I ensure that the status code of the response is 200

