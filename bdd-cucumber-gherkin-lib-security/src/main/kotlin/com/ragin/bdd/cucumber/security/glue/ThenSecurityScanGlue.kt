package com.ragin.bdd.cucumber.security.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.security.SecurityScan
import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import com.ragin.bdd.cucumber.utils.BddJsonUtils
import io.cucumber.java.en.Then
import java.nio.file.Path
import java.time.Duration

/**
 * The Gherkin surface of the security scan - deliberately free of any product name, so that
 * replacing the scanner leaves every feature file untouched.
 *
 * Starting the scanner and wiring the proxy is done by the security scan hooks, so a feature
 * file only has to express the gate. The composite sentence covers the default case; the
 * granular ones exist for projects that need a different order or want to opt out of a part.
 */
class ThenSecurityScanGlue(
    jsonUtils: BddJsonUtils,
    bddProperties: BddProperties,
    private val properties: SecurityScanProperties,
    private val scanner: SecurityScanner,
    private val securityScan: SecurityScan
) : BaseCucumberCore(
        jsonUtils = jsonUtils,
        bddProperties = bddProperties
    ) {
    /**
     * The one sentence that covers a whole scan feature: imports the optional API definitions,
     * scans, writes report and recording, and fails on findings at or above the given risk.
     */
    @Then("I run the security scan for max. {int} minutes and fail on findings of risk {string} or higher")
    fun runScanAndVerify(minutes: Int, failFrom: String) {
        if (skipWhenDisabled()) {
            return
        }
        try {
            securityScan.scanAndVerify(
                maxDuration = Duration.ofMinutes(minutes.toLong()),
                failFrom = SecurityRisk.valueOf(failFrom.uppercase())
            )
        } finally {
            scanner.stop()
        }
    }

    @Then("I import the API definition {string} into the security scanner")
    fun importApiDefinition(url: String) {
        if (skipWhenDisabled()) {
            return
        }
        securityScan.importApiDefinition(url = url)
    }

    @Then("I run the security scan for max. {int} minutes")
    fun runScan(minutes: Int) {
        if (skipWhenDisabled()) {
            return
        }
        securityScan.runScan(maxDuration = Duration.ofMinutes(minutes.toLong()))
        securityScan.awaitAnalysis()
    }

    @Then("I store the security scan report to the file {string}")
    fun storeReport(filePath: String) {
        if (skipWhenDisabled()) {
            return
        }
        securityScan.storeReport(destination = Path.of(filePath))
    }

    @Then("I export the recorded security scan traffic to the file {string}")
    fun exportRecording(filePath: String) {
        if (skipWhenDisabled()) {
            return
        }
        securityScan.exportRecording(destination = Path.of(filePath))
    }

    @Then("I ensure that no security finding has a risk of {string} or higher")
    fun verifyAlerts(failFrom: String) {
        if (skipWhenDisabled()) {
            return
        }
        securityScan.verifyAlerts(failFrom = SecurityRisk.valueOf(failFrom.uppercase()))
    }

    @Then("I make sure that the security scanner is stopped")
    fun stopScanner() {
        scanner.stop()
    }

    private fun skipWhenDisabled(): Boolean {
        if (!properties.enabled) {
            reporter.summary(line = "'cucumbertest.security.enabled' is false - skipping the security scan step")
            return true
        }
        return false
    }
}
