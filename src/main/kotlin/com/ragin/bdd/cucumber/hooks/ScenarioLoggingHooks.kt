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
        log.info("Entering scenario ${scenario.id}")
    }

    @After(order = 1)
    fun logAfterScenario(scenario: Scenario) {
        if (!scenario.isFailed) {
            log.info("Scenario PASS ${scenario.id} with status ${scenario.status}")
        } else {
            val latestResponse: ResponseEntity<String>? = ScenarioStateContext.latestResponse
            if (latestResponse != null) {
                log.error(
                    "Scenario FAIL: Exiting ${scenario.id}" +
                            "%nResponse status:${latestResponse.statusCode.value()}" +
                            "%nResponse body:%n${latestResponse.body}" +
                            "%n----------------%n"
                )
            } else {
                log.error("Scenario FAIL: Exiting ${scenario.id}%n----------------%n")
            }
        }
    }
}
