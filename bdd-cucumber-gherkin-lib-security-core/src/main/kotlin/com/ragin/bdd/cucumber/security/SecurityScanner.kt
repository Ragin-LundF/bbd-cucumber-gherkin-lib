package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import java.nio.file.Path
import java.time.Duration

/**
 * Everything a concrete security scanner has to provide.
 *
 * This is the seam: [SecurityScan], the hooks, the Gherkin steps and the configuration are all
 * scanner independent and talk only to this interface. Replacing the product means writing one
 * new implementation and one `@Bean`; no feature file, tag, property or step changes.
 */
interface SecurityScanner {
    val isRunning: Boolean

    /** Where the scanner's proxy listens, once started. */
    val proxy: ProxyEndpoint

    /**
     * Starts the scanner and makes [exposedHostPorts] of the host reachable from inside it.
     *
     * Must be idempotent: it is called before every scenario but has to act only once.
     */
    fun start(exposedHostPorts: Set<Int>)

    fun stop()

    /** Seeds the scanner from the recording configured as `cucumbertest.security.recording.replay-from`. */
    fun importRecording()

    /** Imports an API definition so endpoints no scenario touched are attacked too. */
    fun importApiDefinition(url: String)

    /** Attacks [target] and blocks until the scan completes or [budget] is used up. */
    fun scan(target: String, budget: Duration, pollInterval: Duration)

    /** Waits until analysis of the recorded traffic has caught up, so no finding is missed. */
    fun awaitAnalysis(maxDuration: Duration, pollInterval: Duration)

    /** All findings below [target], unfiltered. */
    fun alertsFor(target: String): List<SecurityAlert>

    fun exportRecording(destination: Path)

    fun storeReport(destination: Path)
}
