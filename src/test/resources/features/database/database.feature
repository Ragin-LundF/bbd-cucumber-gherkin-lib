Feature: Database examples

  Background: 
    Given that all file paths are relative to "features/database/"
    Given that the database was initialized with the liquibase file "scripts/database_reset.xml"

  Scenario: Insert a user and check in database
    Given that the body of the request is
    """
    {
      "userName": "mytestuser"
    }
    """
    When executing a POST call to "/api/v1/user/db"
    Then I ensure that the status code of the response is 200
    And I ensure that the result of the query of the file "scripts/selectuser.sql" is equal to the CSV file "scripts/expectedselect.csv"
    
  Scenario: Insert user in database with Liquibase and load with REST
    Given that the database was initialized with the liquibase file "scripts/database_insert.xml"
    # User ID was inserted with database_insert.xml
    When executing a GET call to "/api/v1/user/db/1f086281-d28d-4dfa-9cd9-4f609afb0295"
    Then I ensure that the response code is 200 and the body is equal to
    """
    {
      "userId": "1f086281-d28d-4dfa-9cd9-4f609afb0295",
      "userName": "liquibase_user"
    }
    """

  Scenario: Insert user in database with SQL and load with REST
    Given that the SQL statements from the SQL file "scripts/database_insert.sql" was executed
    # User ID was inserted with database_insert.sql
    When executing a GET call to "/api/v1/user/db/00109a35-308a-4d05-a380-6c8166c1422b"
    Then I ensure that the response code is 200 and the body is equal to
    """
    {
      "userId": "00109a35-308a-4d05-a380-6c8166c1422b",
      "userName": "sql_user"
    }
    """
