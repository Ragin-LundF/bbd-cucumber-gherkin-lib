Feature: Testing handling of error codes

  Scenario Outline: Application returns a 400, 401 or 5xx error to check that application errors are handled by REST template
    When executing a GET call to "/api/v1/error/<ErrorCode>"
    Then I ensure that the status code of the response is <ErrorCode>
    And I ensure that the body of the response is equal to
    """
    {
      "error": "CONTAINS_AN_ERROR",
      "message": "Something went wrong."
    }
    """
  Examples:
    | ErrorCode |
    | 400       |
    | 401       |
    | 500       |
    | 501       |
    | 502       |
    | 503       |
    | 504       |
    | 505       |
    | 506       |
    | 507       |
    | 508       |
    | 509       |
    | 510       |
    | 511       |