Feature: Flexible JSON assert configuration

  Scenario: Compare JSON with different sorted JSON arrays
    Given that the response JSON can contain arrays in different order
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "First Element",
          "Second Element",
          "Last Element"
        ]
      }
      """

  @bdd_lib_json_ignore_array_order
  Scenario: Compare JSON with different sorted JSON arrays by annotation
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "First Element",
          "Second Element",
          "Last Element"
        ]
      }
      """

  Scenario: Compare JSON with right sorted JSON arrays
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "Last Element",
          "First Element",
          "Second Element"
        ]
      }
      """

  Scenario: Compare JSON with response with extra array elements
    Given that the response JSON can contain arrays with extra elements
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "Last Element"
        ]
      }
      """

  @bdd_lib_json_ignore_new_array_elements
  Scenario: Compare JSON with response with extra array elements by annotation
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "Last Element"
        ]
      }
      """

  Scenario: Compare JSON with response and ignore extra fields
      Given that the response JSON can contain extra fields
      When executing a GET call to "/api/v1/jsonWithExtraFields"
      Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "exists": "this field exists"
      }
      """

  @bdd_lib_json_ignore_extra_fields
  Scenario: Compare JSON with response and ignore extra fields by annotation
      When executing a GET call to "/api/v1/jsonWithExtraFields"
      Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "exists": "this field exists"
      }
      """

  Scenario: Compare JSON with response with extra fields
    Given that the response JSON can contain extra fields
    When executing a GET call to "/api/v1/jsonWithExtraFields"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "exists": "this field exists",
        "isIgnored": "New field is ignored",
        "isIgnoredToo": "New field is ignored too"
      }
      """


  @bdd_lib_json_ignore_array_order
  @bdd_lib_json_ignore_new_array_elements
  Scenario: Compare JSON with response and combine ignore extra fields and order by annotation
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "First Element",
          "Second Element"
        ]
      }
      """

