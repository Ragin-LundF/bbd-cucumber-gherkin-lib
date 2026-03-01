Feature: Date operations
  Rule: Scenarios with date in the past
    Scenario: Set date 3 days in the past to context and compare
      Given that a date 3 days in the past is stored as "3_DAYS_IN_PAST"
      When executing a GET call to "/api/v1/date/past/days/3"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}3_DAYS_IN_PAST"
      }
      """

    Scenario: Set date 4 months in the past to context and compare
      Given that a date 4 months in the past is stored as "4_MONTHS_IN_PAST"
      When executing a GET call to "/api/v1/date/past/months/4"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}4_MONTHS_IN_PAST"
      }
      """

    Scenario: Set date 5 years in the past to context and compare
      Given that a date 5 years in the past is stored as "5_YEARS_IN_PAST"
      When executing a GET call to "/api/v1/date/past/years/5"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}5_YEARS_IN_PAST"
      }
      """

  Rule: Scenarios with date in the future
    Scenario: Set date 3 days in the future to context and compare
      Given that a date 3 days in the future is stored as "3_DAYS_IN_FUTURE"
      When executing a GET call to "/api/v1/date/future/days/3"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}3_DAYS_IN_FUTURE"
      }
      """

    Scenario: Set date 4 months in the future to context and compare
      Given that a date 4 months in the future is stored as "4_MONTHS_IN_FUTURE"
      When executing a GET call to "/api/v1/date/future/months/4"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}4_MONTHS_IN_FUTURE"
      }
      """

    Scenario: Set date 5 years in the future to context and compare
      Given that a date 5 years in the future is stored as "5_YEARS_IN_FUTURE"
      When executing a GET call to "/api/v1/date/future/years/5"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}5_YEARS_IN_FUTURE"
      }
      """

  Rule: Scenario to check, that date compare works with datetime too
    Scenario: Set datetime 15 days in the future to context and compare
      Given that a date 15 days in the future is stored as "15_DAYS_IN_FUTURE"
      When executing a GET call to "/api/v1/datetime/future/days/15"
      Then I ensure that the status code of the response is 200
      And I ensure that the body of the response is equal to
      """
      {
        "date": "${json-unit.matches:isDateOfContext}15_DAYS_IN_FUTURE"
      }
      """
