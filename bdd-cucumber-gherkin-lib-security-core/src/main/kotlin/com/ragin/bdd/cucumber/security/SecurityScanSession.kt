package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import java.time.Duration

/**
 * The whole scan lifecycle in one object, for a runner that is not Cucumber: start the scanner,
 * hand out its proxy, attack what the suite produced, gate the findings, shut down.
 *
 * ```
 * SecurityScanSession(properties, ZapSecurityScanner.create(properties)).use { session ->
 *     val proxy = session.start(hostPorts = setOf(port))
 *     // route the HTTP client of the test through proxy.host:proxy.port, then run the traffic
 *     session.scanAndVerify(maxDuration = Duration.ofMinutes(30), failFrom = SecurityRisk.MEDIUM)
 * }
 * ```
 *
 * The scanner is a required argument rather than a default: this class must stay free of any
 * product name. `ZapSecurityScanner.create(properties)` builds the one shipped with the library.
 */
class SecurityScanSession(
    private val properties: SecurityScanProperties,
    private val scanner: SecurityScanner
) : AutoCloseable {
    /** The granular operations - import a definition, store a report, export a recording. */
    val scan: SecurityScan = SecurityScan(properties = properties, scanner = scanner)

    /** Where the scanner's proxy listens. Valid once [start] has returned. */
    val proxy: ProxyEndpoint get() = scanner.proxy

    /**
     * Starts the scanner, makes [hostPorts] reachable from inside it and seeds it from a recording
     * when one is configured.
     *
     * Idempotent, like [SecurityScanner.start], so it is safe to call once per test class.
     */
    fun start(hostPorts: Set<Int>): ProxyEndpoint {
        scan.rememberTargets(ports = hostPorts)
        scanner.start(exposedHostPorts = hostPorts)

        if (properties.recording.replayEnabled) {
            scan.importRecording()
        }
        return scanner.proxy
    }

    /**
     * Exports the recording, attacks every target within [maxDuration], writes the report and
     * fails when a relevant finding reaches [failFrom].
     *
     * Both limits are parameters rather than properties on purpose, so no profile can silently
     * weaken the gate.
     */
    fun scanAndVerify(maxDuration: Duration, failFrom: SecurityRisk) {
        scan.scanAndVerify(maxDuration = maxDuration, failFrom = failFrom)
    }

    override fun close() {
        scanner.stop()
    }
}
