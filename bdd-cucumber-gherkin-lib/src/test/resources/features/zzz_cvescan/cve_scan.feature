# The CVE scan needs no traffic, so unlike the DAST scan it neither depends on other features nor
# on the execution order. It only runs through the 'cucumberCve' Gradle task.
@cveScan
Feature: CVE scan of the dependencies of the application under test

  Scenario: No dependency of the application has a known critical vulnerability
    # Scans every archive on the classpath of the test JVM. The test tooling is excluded through
    # 'cucumbertest.security.cve.vulnerabilities.excluded-packages', because it never ships with
    # the application.
    Then I scan the dependencies for known vulnerabilities and fail on findings of severity "CRITICAL" or higher

  Scenario: The packaged library has no known critical vulnerability
    # A single archive or a directory of archives, e.g. the packaged application in 'build/libs'
    Then I scan the artifacts "build/libs" for known vulnerabilities and fail on findings of severity "CRITICAL" or higher
