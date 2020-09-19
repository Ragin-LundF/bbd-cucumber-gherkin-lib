package com.ragin.bdd.cucumber.hooks;

import com.ragin.bdd.cucumber.core.DatabaseExecutorService;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
public class ResetHooks {
    private static final String RESET_DATABASE_FILE = "database/reset_database.xml";

    @Autowired
    DatabaseExecutorService databaseExecutorService;

    /**
     * Execute the {@link #RESET_DATABASE_FILE} liquibase script to reset the database.
     *
     * @param scenario      Current Cucumber Scenario
     * @throws Exception    Not able to execute the database script
     */
    @Before(order = 2)
    @Transactional
    public void resetDatabase(final Scenario scenario) throws Exception {
        // Check if the file exist
        if (getClass().getClassLoader().getResource(RESET_DATABASE_FILE) == null) {
            log.debug(String.format("No reset database file (%s) found.", RESET_DATABASE_FILE));
        } else {
            // Execute the liquibase script to reset the DB
            log.info(String.format("Reset database for scenario %s", scenario.getName()));
            databaseExecutorService.executeLiquibaseScript(RESET_DATABASE_FILE);
        }
    }

    /**
     * Reset state of BaseCucumberCore
     * @param scenario  Current Cucumber Scenario
     */
    @Before(order = 3)
    public void resetBaseCucumberCoreState(final Scenario scenario) {
        log.info(String.format("Cleanup test state for scenario %s", scenario.getName()));
        ScenarioStateContext.current().reset();
    }
}
