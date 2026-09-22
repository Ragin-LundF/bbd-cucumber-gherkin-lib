package com.ragin.bdd.cucumber.security.junit

import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.AlertProperties
import com.ragin.bdd.cucumber.security.config.RecordingProperties
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.config.TargetProperties
import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import java.nio.file.Path
import java.time.Duration
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val PORT = 50000
private const val INTRANET_PORT = 8088
private const val PROXY_PORT = 8090

/**
 * The jUnit lifecycle around the scan. The scan itself is covered by the session and orchestration
 * tests, so these only assert what the extension decides: when it acts and when it cleans up.
 *
 * The lifecycle is driven through the internal seam rather than the jUnit callbacks: an
 * `ExtensionContext` cannot be built outside a running engine, and the extension never reads it.
 */
internal class SecurityScanExtensionTests {
    private val exposedPorts = mutableListOf<Set<Int>>()
    private val scannedTargets = mutableListOf<String>()
    private var stopped = false

    @Test
    internal fun `does nothing while the security scan is disabled`() {
        val extension = extensionFor(properties = SecurityScanProperties())

        extension.startScanner()
        extension.scanAndStop()

        assertTrue(actual = exposedPorts.isEmpty())
        assertTrue(actual = scannedTargets.isEmpty())
        assertFalse(actual = stopped)
        assertNull(actual = extension.proxy)
    }

    @Test
    internal fun `starts the scanner with the ports of the supplier and publishes the proxy`() {
        val extension = extensionFor(
            properties = enabled(),
            hostPorts = Supplier { setOf(PORT, INTRANET_PORT) }
        )

        extension.startScanner()

        assertEquals(expected = listOf(setOf(PORT, INTRANET_PORT)), actual = exposedPorts)
        assertEquals(
            expected = ProxyEndpoint(host = "localhost", port = PROXY_PORT),
            actual = extension.proxy
        )
    }

    @Test
    internal fun `falls back to the ports the configuration names`() {
        val properties = enabled().copy(
            target = TargetProperties(port = PORT, exposedPorts = listOf(INTRANET_PORT))
        )

        extensionFor(properties = properties).startScanner()

        assertEquals(expected = listOf(setOf(INTRANET_PORT, PORT)), actual = exposedPorts)
    }

    @Test
    internal fun `scans and stops the scanner after the last test`() {
        val extension = extensionFor(properties = enabled(), hostPorts = Supplier { setOf(PORT) })

        extension.startScanner()
        extension.scanAndStop()

        assertEquals(expected = listOf(targetUrl(port = PORT)), actual = scannedTargets)
        assertTrue(actual = stopped)
        assertNull(actual = extension.proxy)
    }

    @Test
    internal fun `stops the scanner even when the gate fails the build`() {
        val extension = extensionFor(
            properties = enabled(),
            hostPorts = Supplier { setOf(PORT) },
            alerts = listOf(blockingAlert())
        )

        extension.startScanner()

        assertFailsWith<IllegalStateException> { extension.scanAndStop() }
        assertTrue(actual = stopped)
    }

    private fun enabled(): SecurityScanProperties {
        // the gate has to see the finding, so nothing is filtered out by confidence
        return SecurityScanProperties(
            enabled = true,
            alerts = AlertProperties(minConfidence = SecurityRisk.INFORMATIONAL),
            recording = RecordingProperties(export = false)
        )
    }

    /** A `null` [hostPorts] leaves the argument out, so the extension's own fallback applies. */
    private fun extensionFor(
        properties: SecurityScanProperties,
        hostPorts: Supplier<Set<Int>>? = null,
        alerts: List<SecurityAlert> = emptyList()
    ): SecurityScanExtension {
        val scanner = fakeScanner(alerts = alerts)
        val maxDuration = Duration.ofMinutes(1)

        if (hostPorts == null) {
            return SecurityScanExtension(
                properties = properties,
                scanner = scanner,
                maxDuration = maxDuration,
                failFrom = SecurityRisk.MEDIUM
            )
        }
        return SecurityScanExtension(
            properties = properties,
            scanner = scanner,
            maxDuration = maxDuration,
            failFrom = SecurityRisk.MEDIUM,
            hostPorts = hostPorts
        )
    }

    /** Hand-written rather than mocked, like the fakes in the session and orchestration tests. */
    private fun fakeScanner(alerts: List<SecurityAlert>): SecurityScanner {
        return object : SecurityScanner {
            override val isRunning: Boolean = false

            override val proxy: ProxyEndpoint = ProxyEndpoint(host = "localhost", port = PROXY_PORT)

            override fun start(exposedHostPorts: Set<Int>) {
                exposedPorts += exposedHostPorts
            }

            override fun stop() {
                stopped = true
            }

            override fun importRecording() = Unit

            override fun importApiDefinition(url: String) = Unit

            override fun scan(target: String, budget: Duration, pollInterval: Duration) {
                scannedTargets += target
            }

            override fun awaitAnalysis(maxDuration: Duration, pollInterval: Duration) = Unit

            override fun alertsFor(target: String): List<SecurityAlert> = alerts

            override fun exportRecording(destination: Path) = Unit

            override fun storeReport(destination: Path) = Unit
        }
    }

    private fun blockingAlert(): SecurityAlert {
        return SecurityAlert(
            ruleId = "10000",
            name = "some finding",
            risk = SecurityRisk.HIGH,
            confidence = SecurityRisk.HIGH,
            url = "${targetUrl(port = PORT)}/api/v1/labelling",
            method = "POST",
            parameter = ""
        )
    }

    private fun targetUrl(port: Int): String {
        return "http://host.testcontainers.internal:$port"
    }
}
