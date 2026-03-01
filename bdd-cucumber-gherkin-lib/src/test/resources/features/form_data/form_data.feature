Feature: Form Data
  Background:
    Given that all file paths are relative to "features/form_data/"

  Scenario: Post form data to a public endpoint
    Given that the file "test.txt" is stored as "FORM_FILE"
    When executing a form-data POST call to "/api/v1/filespublic" with the fields
    | identifier  | MY-ID      |
    | fileContext | MY-CONTEXT |
    | file        | FORM_FILE  |
    | filename    | myfile.txt |
    Then I ensure that the response code is 201 and the body is equal to
      """
      {
        "fileContext": "MY-CONTEXT",
        "identifier":"MY-ID"
      }
      """

  Scenario: Post form data to a secured endpoint
    Given that the file "test.txt" is stored as "FORM_FILE"
    When executing an authorized form-data POST call to "/api/v1/filessecured" with the fields
    | identifier  | MY-ID      |
    | fileContext | MY-CONTEXT |
    | file        | FORM_FILE  |
    | filename    | myfile.txt |
    Then I ensure that the response code is 201 and the body is equal to
      """
      {
        "fileContext": "MY-CONTEXT",
        "identifier":"MY-ID"
      }
      """
