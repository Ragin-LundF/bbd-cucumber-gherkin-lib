package com.ragin.bdd.cucumber.hooks;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.ResponseEntity;

/**
 * Hooks for logging the Scenarios
 */
@CommonsLog
public class ScenarioLoggingHooks {

	@Before(order = 1)
	public void logBeforeScenario(Scenario scenario) {
		log.info(String.format(
				"Entering scenario %s",
				scenario.getId()
		));
	}

	@After(order = 1)
	public void logAfterScenario(final Scenario scenario) {
		if (!scenario.isFailed()) {
			log.info(String.format(
					"Scenario PASS %s with status %s",
					scenario.getId(),
					scenario.getStatus()
			));
		} else {
			final ResponseEntity<String> latestResponse = ScenarioStateContext.current().getLatestResponse();
			if (latestResponse != null) {
				log.error(String.format(
						"Scenario FAIL: Exiting %s%nResponse status:%d%nResponse body:%n%s%n----------------%n",
						scenario.getId(),
						latestResponse.getStatusCodeValue(),
						latestResponse.getBody()
				));
			} else {
				log.error(String.format(
						"Scenario FAIL: Exiting %s%n----------------%n",
						scenario.getId()
				));
			}
		}
	}
}
