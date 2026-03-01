package com.ragin.bdd.cucumber.database.hooks

import com.ragin.bdd.cucumber.database.executor.IDatabaseExecutorService
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.transaction.annotation.Transactional

open class DatabaseResetHook(
    private val databaseExecutorService: IDatabaseExecutorService
) {

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
            log.debug { "No reset database file ($RESET_DATABASE_FILE) found." }
        } else {
            // Execute the liquibase script to reset the DB
            log.info { "Reset database for scenario ${scenario.name}" }
            databaseExecutorService.executeLiquibaseScript(liquibaseScript = RESET_DATABASE_FILE)
        }
    }

    companion object {
        private const val RESET_DATABASE_FILE = "database/reset_database.xml"
        private val log = KotlinLogging.logger { }
    }
}
