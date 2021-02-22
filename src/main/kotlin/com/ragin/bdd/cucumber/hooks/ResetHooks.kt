package com.ragin.bdd.cucumber.hooks

import com.ragin.bdd.cucumber.core.IDatabaseExecutorService
import com.ragin.bdd.cucumber.core.ScenarioStateContext.addJsonIgnoringArrayOrder
import com.ragin.bdd.cucumber.core.ScenarioStateContext.addJsonIgnoringExtraArrayElements
import com.ragin.bdd.cucumber.core.ScenarioStateContext.addJsonIgnoringExtraFields
import com.ragin.bdd.cucumber.core.ScenarioStateContext.reset
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import org.apache.commons.logging.LogFactory
import org.springframework.transaction.annotation.Transactional

open class ResetHooks(private val databaseExecutorService: IDatabaseExecutorService) {
    companion object {
        private const val RESET_DATABASE_FILE = "database/reset_database.xml"
        private val log = LogFactory.getLog(ResetHooks::class.java)
    }

    /**
     * Execute the [.RESET_DATABASE_FILE] liquibase script to reset the database.
     *
     * @param scenario      Current Cucumber Scenario
     * @throws Exception    Not able to execute the database script
     */
    @Before(order = 2)
    @Transactional
    @Throws(Exception::class)
    open fun resetDatabase(scenario: Scenario) {
        // Check if the file exist
        if (javaClass.classLoader.getResource(RESET_DATABASE_FILE) == null) {
            log.debug(String.format("No reset database file (%s) found.", RESET_DATABASE_FILE))
        } else {
            // Execute the liquibase script to reset the DB
            log.info(String.format("Reset database for scenario %s", scenario.name))
            databaseExecutorService.executeLiquibaseScript(RESET_DATABASE_FILE)
        }
    }

    /**
     * Reset state of BaseCucumberCore
     * @param scenario  Current Cucumber Scenario
     */
    @Before(order = 3)
    fun resetBaseCucumberCoreState(scenario: Scenario) {
        log.info("""Cleanup test state for scenario ${scenario.name}""")
        reset()
    }

    @Before(order = 10, value = "@bdd_lib_json_ignore_new_array_elements")
    fun jsonIgnoreExtraArrayElements(scenario: Scenario) {
        log.info("""Set JSON ignore for extra array elements for scenario ${scenario.name}""")
        addJsonIgnoringExtraArrayElements()
    }

    @Before(order = 10, value = "@bdd_lib_json_ignore_extra_fields")
    fun jsonIgnoreNewFields(scenario: Scenario) {
        log.info("""Set JSON ignore for extra fields for scenario ${scenario.name}""")
        addJsonIgnoringExtraFields()
    }

    @Before(order = 10, value = "@bdd_lib_json_ignore_array_order")
    fun jsonIgnoreArrayOrder(scenario: Scenario) {
        log.info("""Set JSON ignore array order for scenario ${scenario.name}""")
        addJsonIgnoringArrayOrder()
    }
}