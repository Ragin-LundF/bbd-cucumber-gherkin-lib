package com.ragin.bdd.cucumber.security.zap

import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

/**
 * OWASP ZAP behind the [SecurityScanner] interface.
 *
 * This is the only class that knows ZAP exists. Everything product specific - the container,
 * the API dialect, the `Host: zap` quirk, the add-on names - stops here.
 */
class ZapSecurityScanner(
    private val properties: SecurityScanProperties,
    private val container: ZapContainer,
    private val client: ZapApiClient
) : SecurityScanner {
    override val isRunning: Boolean get() = container.isRunning

    override val proxy: ProxyEndpoint
        get() = ProxyEndpoint(host = container.host, port = container.port)

    /**
     * Starts ZAP and turns every ignored rule into a false positive, so the report leaves out
     * what the gate ignores. The filters go in before any traffic, because ZAP applies them only
     * to alerts raised afterwards.
     */
    override fun start(exposedHostPorts: Set<Int>) {
        if (container.isRunning) {
            return
        }
        container.start(hostPorts = exposedHostPorts)
        properties.alerts.ignoredRuleIds.forEach(::ignoreRule)
    }

    /**
     * Lenient: without the filter only the report still lists the rule. The gate drops it by rule
     * id either way, so the verdict does not depend on this call.
     */
    private fun ignoreRule(ruleId: String) {
        log.info { "ignoring scanner rule $ruleId" }
        runCatching {
            client.addGlobalAlertFilter(ruleId = ruleId)
        }.onFailure { error ->
            log.warn(throwable = error) {
                "could not ignore rule $ruleId in the report - is the 'alertFilters' add-on installed?"
            }
        }
    }

    override fun stop() {
        container.stop()
    }

    override fun importRecording() {
        log.info { "replaying ${properties.recording.replayFrom} into ZAP" }
        client.importHar(containerFilePath = ZapContainer.REPLAY_PATH)
    }

    override fun importApiDefinition(url: String) {
        client.importOpenApi(url = url)
    }

    override fun scan(target: String, budget: Duration, pollInterval: Duration) {
        val scanId = client.startActiveScan(
            url = target,
            recurse = properties.scan.recurse,
            inScopeOnly = properties.scan.inScopeOnly
        )
        log.info { "active scan $scanId started against $target" }

        val completed = pollUntil(
            maxDuration = budget,
            pollInterval = pollInterval,
            what = "active scan $scanId on $target"
        ) {
            val status = client.activeScanStatus(scanId = scanId)
            log.info { "active scan $scanId ($target) at $status%" }
            status >= COMPLETE
        }
        check(completed) { "The ZAP active scan of $target did not finish within $budget." }
    }

    override fun awaitAnalysis(maxDuration: Duration, pollInterval: Duration) {
        val drained = pollUntil(
            maxDuration = maxDuration,
            pollInterval = pollInterval,
            what = "passive scan queue"
        ) {
            val remaining = client.passiveScanRecordsToScan()
            log.info { "passive scan queue: $remaining records left" }
            remaining == 0
        }
        if (!drained) {
            log.warn { "the passive scan queue did not drain within $maxDuration - findings may be incomplete" }
        }
    }

    override fun alertsFor(target: String): List<SecurityAlert> {
        return client.alerts(baseUrl = target)
    }

    /**
     * Needs the `exim` add-on; when it is missing the export is skipped with a warning rather
     * than failing the scan, because the recording is a debugging aid, not the result.
     */
    override fun exportRecording(destination: Path) {
        runCatching {
            val har = client.exportHar()
            Files.createDirectories(destination.toAbsolutePath().parent)
            Files.write(destination, har)
            log.info { "recorded traffic written to ${destination.toAbsolutePath()} (${har.size} bytes)" }
        }.onFailure { error ->
            log.warn(throwable = error) { "could not export the recording - is the 'exim' add-on installed?" }
        }
    }

    override fun storeReport(destination: Path) {
        val containerPath = client.generateReport(
            title = properties.report.title,
            template = properties.report.template,
            fileName = destination.fileName.toString()
        )
        container.copyFileFromContainer(containerPath = containerPath, hostPath = destination)
        log.info { "security scan report written to ${destination.toAbsolutePath()}" }
    }

    private fun pollUntil(
        maxDuration: Duration,
        pollInterval: Duration,
        what: String,
        condition: () -> Boolean
    ): Boolean {
        val deadline = Instant.now().plus(maxDuration)
        while (Instant.now().isBefore(deadline)) {
            if (condition()) {
                return true
            }
            Thread.sleep(pollInterval.toMillis())
        }
        log.warn { "timed out after $maxDuration while waiting for $what" }
        return false
    }

    companion object {
        /**
         * The wired ZAP stack: container, API client and scanner.
         *
         * The one place a project that does not use Spring has to name the product. Everything it
         * touches afterwards is the scanner independent [SecurityScanner] interface.
         */
        @JvmStatic
        fun create(properties: SecurityScanProperties): SecurityScanner {
            val container = ZapContainer(properties = properties)
            return ZapSecurityScanner(
                properties = properties,
                container = container,
                client = ZapApiClient(container = container)
            )
        }

        private const val COMPLETE = 100
        private val log = KotlinLogging.logger {}
    }
}
