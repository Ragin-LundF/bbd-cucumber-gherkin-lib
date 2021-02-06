package com.ragin.bdd.cucumber.hooks

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import org.apache.commons.logging.LogFactory
import org.springframework.http.ResponseEntity

/**
 * Hooks for logging the Scenarios
 */
class ScenarioLoggingHooks {
    companion object {
        private val log = LogFactory.getLog(ScenarioLoggingHooks::class.java)
    }

    @Before(order = 1)
    fun logBeforeScenario(scenario: Scenario) {
        log.info(String.format(
                "Entering scenario %s",
                scenario.id
        ))
    }

    @After(order = 1)
    fun logAfterScenario(scenario: Scenario) {
        if (!scenario.isFailed) {
            log.info(String.format(
                    "Scenario PASS %s with status %s",
                    scenario.id,
                    scenario.status
            ))
        } else {
            val latestResponse: ResponseEntity<String>? = ScenarioStateContext.latestResponse
            if (latestResponse != null) {
                log.error(String.format(
                        "Scenario FAIL: Exiting %s%nResponse status:%d%nResponse body:%n%s%n----------------%n",
                        scenario.id,
                        latestResponse.statusCodeValue,
                        latestResponse.body
                ))
            } else {
                log.error(String.format(
                        "Scenario FAIL: Exiting %s%n----------------%n",
                        scenario.id
                ))
            }
        }
    }
}