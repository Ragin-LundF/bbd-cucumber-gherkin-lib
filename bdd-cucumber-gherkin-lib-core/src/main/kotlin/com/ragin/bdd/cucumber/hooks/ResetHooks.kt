package com.ragin.bdd.cucumber.hooks

import com.ragin.bdd.cucumber.core.ScenarioStateContext.addJsonIgnoringArrayOrder
import com.ragin.bdd.cucumber.core.ScenarioStateContext.addJsonIgnoringExtraArrayElements
import com.ragin.bdd.cucumber.core.ScenarioStateContext.addJsonIgnoringExtraFields
import com.ragin.bdd.cucumber.core.ScenarioStateContext.reset
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.github.oshai.kotlinlogging.KotlinLogging

open class ResetHooks {
    /**
     * Reset state of BaseCucumberCore
     * @param scenario  Current Cucumber Scenario
     */
    @Before(order = 3)
    fun resetBaseCucumberCoreState(scenario: Scenario) {
        log.info { "Cleanup test state for scenario ${scenario.name}" }
        reset()
    }

    @Before(order = 10, value = "@bdd_lib_json_ignore_new_array_elements")
    fun jsonIgnoreExtraArrayElements(scenario: Scenario) {
        log.info { "Set JSON ignore for extra array elements for scenario ${scenario.name}" }
        addJsonIgnoringExtraArrayElements()
    }

    @Before(order = 10, value = "@bdd_lib_json_ignore_extra_fields")
    fun jsonIgnoreNewFields(scenario: Scenario) {
        log.info { "Set JSON ignore for extra fields for scenario ${scenario.name}" }
        addJsonIgnoringExtraFields()
    }

    @Before(order = 10, value = "@bdd_lib_json_ignore_array_order")
    fun jsonIgnoreArrayOrder(scenario: Scenario) {
        log.info { "Set JSON ignore array order for scenario ${scenario.name}" }
        addJsonIgnoringArrayOrder()
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
