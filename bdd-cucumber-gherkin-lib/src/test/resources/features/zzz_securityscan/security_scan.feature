# The scan can only attack what the proxy recorded, so it has to run after every feature that
# seeds it. The runner pins the execution order to lexical and this directory sorts last.
#
# Only the composite sentence is exercised here. It already covers the whole default case: export
# the recording, import the configured API definitions, scan every target, wait for the analysis,
# write the report and gate on the findings. The granular sentences split exactly those steps up
# and cannot be demonstrated in a second scenario, because the composite one stops the scanner.
@securityScan
Feature: Security scan of the application under test

  @securityExecuteScan
  Scenario: The recorded traffic contains no finding of high risk
    # The application under test is the dummy app of this repository: a plain Spring Boot service
    # without any hardening, no security headers and a deliberately open actuator. A real project
    # states "MEDIUM" here. Raising the threshold for a test fixture is not the same as weakening
    # the gate of a production service.
    Then I run the security scan for max. 10 minutes and fail on findings of risk "HIGH" or higher
