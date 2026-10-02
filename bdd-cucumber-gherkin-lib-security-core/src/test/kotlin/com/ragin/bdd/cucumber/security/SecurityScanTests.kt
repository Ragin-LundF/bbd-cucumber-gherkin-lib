package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.config.ReportProperties
import com.ragin.bdd.cucumber.security.config.ReportTemplateProperties
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import java.nio.file.Path
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val PORT = 50000
private const val INTRANET_PORT = 8088
private const val PROXY_PORT = 8090
private const val BROKEN_TEMPLATE = "does-not-exist"

/**
 * Orchestration only - the pass/fail decision itself is covered by [SecurityAlertGateTests].
 *
 * These tests go through the [SecurityScanner] interface, so they stay valid when the concrete
 * scanner is replaced.
 */
internal class SecurityScanTests {
    private val scannedTargets = mutableListOf<String>()
    private val storedReports = mutableListOf<Pair<String, Path>>()

    @Test
    internal fun `derives one base url per distinct port in ascending order`() {
        val scan = SecurityScan(properties = SecurityScanProperties(), scanner = fakeScanner())

        scan.rememberTargets(ports = listOf(PORT, INTRANET_PORT, PORT))

        assertEquals(
            expected = listOf(targetUrl(port = INTRANET_PORT), targetUrl(port = PORT)),
            actual = scan.targets
        )
    }

    @Test
    internal fun `collects findings from every target base url`() {
        val scan = scanFor(
            ports = listOf(PORT, INTRANET_PORT),
            alertsByTarget = mapOf(
                targetUrl(port = INTRANET_PORT) to listOf(alert(name = "on intranet")),
                targetUrl(port = PORT) to listOf(alert(name = "on public"))
            )
        )

        assertEquals(
            expected = setOf("on intranet", "on public"),
            actual = scan.relevantAlerts().map { found -> found.name }.toSet()
        )
    }

    @Test
    internal fun `fails the scan when a target reports a blocking finding`() {
        val scan = scanFor(
            ports = listOf(PORT),
            alertsByTarget = mapOf(targetUrl(port = PORT) to listOf(alert(risk = SecurityRisk.HIGH)))
        )

        assertFailsWith<IllegalStateException> { scan.verifyAlerts(failFrom = SecurityRisk.MEDIUM) }
    }

    @Test
    internal fun `attacks every target it was given`() {
        val scan = scanFor(ports = listOf(PORT, INTRANET_PORT))

        scan.runScan(maxDuration = Duration.ofMinutes(1))

        assertEquals(
            expected = listOf(targetUrl(port = INTRANET_PORT), targetUrl(port = PORT)),
            actual = scannedTargets
        )
    }

    @Test
    internal fun `refuses to scan before the hook determined the targets`() {
        val scan = SecurityScan(properties = SecurityScanProperties(), scanner = fakeScanner())

        assertFailsWith<IllegalStateException> { scan.runScan(maxDuration = Duration.ofMinutes(1)) }
    }

    @Test
    internal fun `writes every configured report into the output directory`() {
        val scan = SecurityScan(
            properties = SecurityScanProperties(report = ReportProperties(outputDir = "build/reports/security")),
            scanner = fakeScanner()
        )

        scan.storeReports()

        assertEquals(
            expected = listOf(
                ReportProperties.OWN_HTML_TEMPLATE to Path.of("build/reports/security/security-report.html"),
                "traditional-xml" to Path.of("build/reports/security/security-report.xml")
            ),
            actual = storedReports
        )
    }

    @Test
    internal fun `writes the remaining reports before failing on a broken template`() {
        val scan = SecurityScan(
            properties = SecurityScanProperties(
                report = ReportProperties(
                    templates = listOf(
                        ReportTemplateProperties(template = BROKEN_TEMPLATE, fileName = "first.html"),
                        ReportTemplateProperties(template = "traditional-xml", fileName = "report.xml"),
                        ReportTemplateProperties(template = BROKEN_TEMPLATE, fileName = "third.html")
                    )
                )
            ),
            scanner = fakeScanner()
        )

        val failure = assertFailsWith<IllegalStateException> { scan.storeReports(outputDir = Path.of("out")) }

        assertEquals(expected = listOf("traditional-xml" to Path.of("out/report.xml")), actual = storedReports)
        assertEquals(expected = "no template $BROKEN_TEMPLATE for first.html", actual = failure.message)
        assertEquals(
            expected = listOf("no template $BROKEN_TEMPLATE for third.html"),
            actual = failure.suppressed.map { it.message }
        )
    }

    private fun scanFor(
        ports: List<Int>,
        alertsByTarget: Map<String, List<SecurityAlert>> = emptyMap()
    ): SecurityScan {
        val scan = SecurityScan(
            properties = SecurityScanProperties(),
            scanner = fakeScanner(alertsByTarget = alertsByTarget)
        )
        scan.rememberTargets(ports = ports)
        return scan
    }

    /**
     * Records what the orchestration asked of a scanner and answers with the findings it was
     * primed with.
     *
     * Hand-written rather than mocked: the interface is small, and a fake keeps this module free
     * of a mocking framework that no other module of the library needs.
     */
    private fun fakeScanner(alertsByTarget: Map<String, List<SecurityAlert>> = emptyMap()): SecurityScanner {
        return object : SecurityScanner {
            override val isRunning: Boolean = false

            override val proxy: ProxyEndpoint = ProxyEndpoint(host = "localhost", port = PROXY_PORT)

            override fun start(exposedHostPorts: Set<Int>) = Unit

            override fun stop() = Unit

            override fun importRecording() = Unit

            override fun importApiDefinition(url: String) = Unit

            override fun scan(target: String, budget: Duration, pollInterval: Duration) {
                scannedTargets += target
            }

            override fun awaitAnalysis(maxDuration: Duration, pollInterval: Duration) = Unit

            override fun alertsFor(target: String): List<SecurityAlert> {
                return alertsByTarget[target].orEmpty()
            }

            override fun exportRecording(destination: Path) = Unit

            override fun storeReport(template: String, destination: Path) {
                check(template != BROKEN_TEMPLATE) { "no template $template for ${destination.fileName}" }
                storedReports += template to destination
            }
        }
    }

    private fun targetUrl(port: Int): String {
        return "http://host.testcontainers.internal:$port"
    }

    private fun alert(
        name: String = "some finding",
        risk: SecurityRisk = SecurityRisk.LOW
    ): SecurityAlert {
        return SecurityAlert(
            ruleId = "10000",
            name = name,
            risk = risk,
            confidence = SecurityRisk.HIGH,
            url = "${targetUrl(port = PORT)}/api/v1/labelling",
            method = "POST",
            parameter = ""
        )
    }
}
