package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

/**
 * Scanner independent orchestration: what to attack and in which order. The product specific
 * parts live behind [SecurityScanner], the pass/fail decision in [SecurityAlertGate].
 */
class SecurityScan(
    private val properties: SecurityScanProperties,
    private val scanner: SecurityScanner
) {
    private val gate = SecurityAlertGate(properties = properties.alerts)

    /**
     * Base URLs of the application under test as seen from inside the scanner.
     *
     * A service usually listens on more than one port (public, intranet, applications) and
     * scanners keep a separate tree per host:port, so both the scan and the alert query run per
     * base URL. Filtering on a single one would silently drop findings on the others.
     */
    var targets: List<String> = emptyList()
        private set

    fun rememberTargets(ports: Collection<Int>) {
        targets = ports.distinct().sorted().map { port -> "http://${properties.target.host}:$port" }
    }

    /** Imports every configured API definition. Lenient - see [importApiDefinition]. */
    fun importApiDefinitions() {
        properties.api.definitionUrls.forEach(::importApiDefinition)
    }

    /**
     * Imports one API definition.
     *
     * A failure is logged and swallowed: the definition is an optional extra on top of the
     * recorded traffic, which is the mandatory source of coverage.
     */
    fun importApiDefinition(url: String) {
        log.info { "importing API definition $url" }
        runCatching {
            scanner.importApiDefinition(url = url)
        }.onFailure { error ->
            log.warn(throwable = error) { "could not import the API definition $url - continuing without it" }
        }
    }

    fun importRecording() {
        scanner.importRecording()
    }

    /**
     * Scans every target and blocks until they all finish.
     *
     * [maxDuration] is the budget for the whole run, so it is shared between the targets rather
     * than granted to each of them.
     */
    fun runScan(maxDuration: Duration) {
        check(targets.isNotEmpty()) { "No target known. Did the security scan hook run?" }

        val deadline = Instant.now().plus(maxDuration)
        targets.forEach { target ->
            val remaining = Duration.between(Instant.now(), deadline)
            check(!remaining.isNegative && !remaining.isZero) {
                "The security scan did not finish within $maxDuration."
            }
            log.info { "scanning $target" }
            scanner.scan(target = target, budget = remaining, pollInterval = properties.scan.pollInterval)
        }
    }

    fun awaitAnalysis() {
        scanner.awaitAnalysis(
            maxDuration = properties.scan.pollInterval.multipliedBy(ANALYSIS_POLL_FACTOR),
            pollInterval = properties.scan.pollInterval
        )
    }

    /** Findings of every target that survive the ignore list and the confidence threshold. */
    fun relevantAlerts(): List<SecurityAlert> {
        return gate.relevant(alerts = targets.flatMap { target -> scanner.alertsFor(target = target) })
    }

    /** Fails with a readable summary if any relevant finding reaches [failFrom]. */
    fun verifyAlerts(failFrom: SecurityRisk) {
        gate.verify(alerts = relevantAlerts(), failFrom = failFrom)
    }

    fun storeReport(template: String, destination: Path) {
        scanner.storeReport(template = template, destination = destination)
    }

    /**
     * Writes every configured report into [outputDir].
     *
     * One broken template must not cost the other reports, so all of them are attempted before
     * the first failure is rethrown, with the later ones attached as suppressed.
     */
    fun storeReports(outputDir: Path = Path.of(properties.report.outputDir)) {
        val failures = properties.report.templates.mapNotNull { report ->
            runCatching {
                storeReport(template = report.template, destination = outputDir.resolve(report.fileName))
            }.onFailure { error ->
                log.error(throwable = error) { "could not write the report ${report.fileName} (${report.template})" }
            }.exceptionOrNull()
        }
        failures.firstOrNull()?.let { first ->
            failures.drop(n = 1).forEach(first::addSuppressed)
            throw first
        }
    }

    fun exportRecording(destination: Path) {
        scanner.exportRecording(destination = destination)
    }

    /**
     * The whole finalization in one call: export the recording, scan, report, gate.
     *
     * The recording is written *before* the scan on purpose. At that point it holds exactly the
     * traffic the functional scenarios produced, which is what replaying it should reproduce.
     * Exporting afterwards would fold the scanner's own attack requests into the recording, and
     * replaying that both re-raises every finding the attacks provoked and makes the next scan
     * roughly ten times larger.
     */
    fun scanAndVerify(maxDuration: Duration, failFrom: SecurityRisk) {
        if (properties.recording.export) {
            exportRecording(destination = Path.of(properties.recording.exportPath))
        }

        importApiDefinitions()
        runScan(maxDuration = maxDuration)
        awaitAnalysis()

        storeReports()
        verifyAlerts(failFrom = failFrom)
    }

    private companion object {
        const val ANALYSIS_POLL_FACTOR = 30L
        val log = KotlinLogging.logger {}
    }
}
