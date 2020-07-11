package com.ragin.bdd.cucumber.hooks;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import com.ragin.bdd.cucumber.core.Loggable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.springframework.http.ResponseEntity;

/**
 * Hooks for logging the Scenarios
 */
public class ScenarioLoggingHooks extends Loggable {

	@Before(order = 1)
	public void logBeforeScenario(Scenario scenario) {
		LOG.info(String.format(
				"Entering scenario %s",
				scenario.getId()
		));
	}

	@After(order = 1)
	public void logAfterScenario(Scenario scenario) {
		if (!scenario.isFailed()) {
			LOG.info(String.format(
					"Scenario PASS %s with status %s",
					scenario.getId(),
					scenario.getStatus()
			));
		} else {
			ResponseEntity<String> latestResponse = BaseCucumberCore.latestResponse;
			if (latestResponse != null) {
				LOG.error(String.format(
						"Scenario FAIL: Exiting %s\nResponse status:%d\nResponse body:\n%s\n----------------\n",
						scenario.getId(),
						latestResponse.getStatusCodeValue(),
						latestResponse.getBody()
				));
			} else {
				LOG.error(String.format(
						"Scenario FAIL: Exiting %s\n----------------\n",
						scenario.getId()
				));
			}
		}
	}
}
