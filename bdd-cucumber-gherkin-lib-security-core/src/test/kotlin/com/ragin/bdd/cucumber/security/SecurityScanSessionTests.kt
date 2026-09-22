package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.config.RecordingProperties
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import java.nio.file.Path
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PORT = 50000
private const val INTRANET_PORT = 8088
private const val PROXY_PORT = 8090

/**
 * The lifecycle a runner that is not Cucumber relies on. The orchestration behind it is covered by
 * [SecurityScanTests], so these tests only assert what the session adds: the start sequence, the
 * proxy it hands out and the shutdown.
 */
internal class SecurityScanSessionTests {
    private val exposedPorts = mutableListOf<Set<Int>>()
    private var recordingImported = false
    private var stopped = false

    @Test
    internal fun `start exposes the host ports and remembers them as scan targets`() {
        val session = sessionFor(properties = SecurityScanProperties())

        session.start(hostPorts = setOf(PORT, INTRANET_PORT))

        assertEquals(expected = listOf(setOf(PORT, INTRANET_PORT)), actual = exposedPorts)
        assertEquals(
            expected = listOf(targetUrl(port = INTRANET_PORT), targetUrl(port = PORT)),
            actual = session.scan.targets
        )
    }

    @Test
    internal fun `start returns the proxy the scanner listens on`() {
        val session = sessionFor(properties = SecurityScanProperties())

        val proxy = session.start(hostPorts = setOf(PORT))

        assertEquals(expected = ProxyEndpoint(host = "localhost", port = PROXY_PORT), actual = proxy)
        assertEquals(expected = proxy, actual = session.proxy)
    }

    @Test
    internal fun `start does not import a recording when none is configured`() {
        sessionFor(properties = SecurityScanProperties()).start(hostPorts = setOf(PORT))

        assertFalse(actual = recordingImported)
    }

    @Test
    internal fun `start imports the recording when one is configured for replay`() {
        val properties = SecurityScanProperties(
            recording = RecordingProperties(replayFrom = "build/reports/security/recording.har")
        )

        sessionFor(properties = properties).start(hostPorts = setOf(PORT))

        assertTrue(actual = recordingImported)
    }

    @Test
    internal fun `close stops the scanner`() {
        sessionFor(properties = SecurityScanProperties()).use { session ->
            session.start(hostPorts = setOf(PORT))
            assertFalse(actual = stopped)
        }

        assertTrue(actual = stopped)
    }

    private fun sessionFor(properties: SecurityScanProperties): SecurityScanSession {
        return SecurityScanSession(properties = properties, scanner = fakeScanner())
    }

    /**
     * Records what the session asked of a scanner.
     *
     * Hand-written rather than mocked, like the fake in [SecurityScanTests]: the interface is
     * small, and it keeps this module free of a mocking framework.
     */
    private fun fakeScanner(): SecurityScanner {
        return object : SecurityScanner {
            override val isRunning: Boolean = false

            override val proxy: ProxyEndpoint = ProxyEndpoint(host = "localhost", port = PROXY_PORT)

            override fun start(exposedHostPorts: Set<Int>) {
                exposedPorts += exposedHostPorts
            }

            override fun stop() {
                stopped = true
            }

            override fun importRecording() {
                recordingImported = true
            }

            override fun importApiDefinition(url: String) = Unit

            override fun scan(target: String, budget: Duration, pollInterval: Duration) = Unit

            override fun awaitAnalysis(maxDuration: Duration, pollInterval: Duration) = Unit

            override fun alertsFor(target: String): List<SecurityAlert> = emptyList()

            override fun exportRecording(destination: Path) = Unit

            override fun storeReport(destination: Path) = Unit
        }
    }

    private fun targetUrl(port: Int): String {
        return "http://host.testcontainers.internal:$port"
    }
}
