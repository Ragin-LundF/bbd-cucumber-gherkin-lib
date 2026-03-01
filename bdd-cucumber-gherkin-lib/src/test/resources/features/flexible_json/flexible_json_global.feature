# ignore new array elements for the feature by annotation
@bdd_lib_json_ignore_new_array_elements
Feature: Flexible JSON assert configuration
  Background:
    # ignore array order for this feature by background
    Given that the response JSON can contain arrays in different order

  Scenario: Compare JSON with different sorted JSON arrays set as global background
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

  Scenario: Compare JSON with response with extra array elements set as global annotation
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "Last Element"
        ]
      }
      """

  Scenario: Compare JSON with response with extra array elements and unsorted order set as global annotation and background
    When executing a GET call to "/api/v1/jsonWithUnsortedArray"
    Then I ensure that the response code is 200 and the body is equal to
      """
      {
        "unsorted": [
          "First Element"
        ]
      }
      """
