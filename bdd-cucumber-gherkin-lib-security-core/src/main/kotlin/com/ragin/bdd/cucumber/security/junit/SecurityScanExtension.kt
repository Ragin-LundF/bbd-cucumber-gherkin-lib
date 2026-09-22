package com.ragin.bdd.cucumber.security.junit

import com.ragin.bdd.cucumber.security.SecurityScanSession
import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.models.ProxyEndpoint
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import java.time.Duration
import java.util.function.Supplier
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Drives the security scan around a plain jUnit test class: the scanner comes up before the first
 * test, the tests produce the traffic, and the scan plus the gate run after the last one.
 *
 * ```
 * @TestInstance(TestInstance.Lifecycle.PER_CLASS)
 * class MySecurityTests {
 *     @RegisterExtension
 *     val securityScan = SecurityScanExtension(
 *         properties = properties,
 *         scanner = ZapSecurityScanner.create(properties),
 *         maxDuration = Duration.ofMinutes(30),
 *         failFrom = SecurityRisk.MEDIUM,
 *         hostPorts = Supplier { setOf(port) }
 *     )
 * }
 * ```
 *
 * [hostPorts] is resolved in `beforeAll` rather than at construction, so a port the server only
 * assigns while the context starts can still be supplied. A randomly assigned port therefore needs
 * a **non static** `@RegisterExtension` field on a `PER_CLASS` class - class level callbacks still
 * fire there - or an explicit `cucumbertest.security.target.port`.
 *
 * [maxDuration] and [failFrom] are constructor parameters rather than properties, the same rule the
 * Gherkin sentence follows, so no profile can silently weaken the gate.
 *
 * The scanner is a required argument: this class must stay free of any product name.
 */
class SecurityScanExtension @JvmOverloads constructor(
    private val properties: SecurityScanProperties,
    private val scanner: SecurityScanner,
    private val maxDuration: Duration,
    private val failFrom: SecurityRisk,
    private val hostPorts: Supplier<Set<Int>> = Supplier { defaultHostPorts(properties = properties) }
) : BeforeAllCallback, AfterAllCallback {
    private var session: SecurityScanSession? = null

    /** Where the scanner's proxy listens. `null` while the scan is disabled or not started yet. */
    val proxy: ProxyEndpoint? get() = session?.proxy

    override fun beforeAll(context: ExtensionContext) {
        startScanner()
    }

    override fun afterAll(context: ExtensionContext) {
        scanAndStop()
    }

    /**
     * Starts the scanner unless the scan is switched off, the same guard the Gherkin steps use.
     *
     * Internal rather than private so the tests can drive the lifecycle: jUnit's
     * [ExtensionContext] cannot be built outside a running engine, and this class never reads it.
     */
    internal fun startScanner() {
        if (!properties.enabled) {
            return
        }
        session = SecurityScanSession(properties = properties, scanner = scanner).apply {
            start(hostPorts = hostPorts.get())
        }
    }

    /**
     * Scans and gates, then stops the scanner through [SecurityScanSession.close] so a failing
     * gate cannot leave the container behind.
     */
    internal fun scanAndStop() {
        val running = session ?: return
        session = null
        running.use { it.scanAndVerify(maxDuration = maxDuration, failFrom = failFrom) }
    }

    private companion object {
        /** Every port the configuration names, for a suite that knows its port up front. */
        fun defaultHostPorts(properties: SecurityScanProperties): Set<Int> {
            return (properties.target.exposedPorts + listOfNotNull(properties.target.port)).toSet()
        }
    }
}
