package com.ragin.bdd.cucumber.hooks

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.constants.BddReportConstants.AttachmentNames
import com.ragin.bdd.cucumber.constants.BddReportConstants.Markers
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.utils.ScenarioReporter
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Announces the scenario and reports what went wrong when it fails.
 *
 * Also publishes the running scenario to [ScenarioStateContext], which is what allows every glue
 * class to report into the Cucumber report. This runs at order 1, so the scenario is available to
 * every other hook and every step.
 */
open class ScenarioLoggingHooks(bddProperties: BddProperties) {
    private val reporter = ScenarioReporter(options = bddProperties.logging)

    @Before(order = 1)
    open fun logBeforeScenario(scenario: Scenario) {
        ScenarioStateContext.scenario = scenario
        // Debug, because a configured report plugin already prints the scenario line. A failure is
        // logged at error below, so the scenario is never anonymous when it matters.
        log.debug { "${Markers.SCENARIO_START} Scenario \"${scenario.name}\" (${locationOf(scenario = scenario)})" }
    }

    @After(order = 1)
    open fun logAfterScenario(scenario: Scenario) {
        if (!scenario.isFailed) {
            log.debug { "${Markers.SCENARIO_END} Scenario \"${scenario.name}\" ${scenario.status}" }
            return
        }

        log.error {
            "${Markers.SCENARIO_FAILED} Scenario \"${scenario.name}\" FAILED " +
                "(${locationOf(scenario = scenario)})${responseSummaryOf()}"
        }
        attachFailedResponse()
    }

    /**
     * Adds the last response to the report of the failed scenario.
     *
     * The body goes into an attachment rather than into the log: the report is where someone looks
     * after a failure, and a large or binary payload must not flood the console.
     */
    private fun attachFailedResponse() {
        val response = ScenarioStateContext.latestResponse ?: return
        reporter.attachBody(
            name = AttachmentNames.FAILED_RESPONSE_BODY,
            body = response.body,
            contentSubtype = response.headers.contentType?.subtype
        )
    }

    private fun responseSummaryOf(): String {
        val response = ScenarioStateContext.latestResponse ?: return ""

        return ", last response ${response.statusCode.value()}"
    }

    /**
     * Feature file and line of the scenario, which is what a reader needs to find it again.
     */
    private fun locationOf(scenario: Scenario): String {
        val uri = scenario.uri.toString().substringAfterLast(delimiter = "/")

        return "$uri:${scenario.line}"
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
