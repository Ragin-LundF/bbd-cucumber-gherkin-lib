package com.ragin.bdd.cucumber.security.hooks

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.rest.httpclient.ClientHttpRequestFactory
import com.ragin.bdd.cucumber.security.SecurityScanSession
import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import io.cucumber.java.Before
import io.github.oshai.kotlinlogging.KotlinLogging
import org.opentest4j.TestAbortedException
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.core.env.Environment

/**
 * Brings the scanner up and routes the whole suite through it, so that no feature file has to
 * know that a security scan is happening.
 *
 * Both hooks sit in the tag-guarded slot (order 10 and above), so they run after the state reset
 * at order 3. That is deliberate: the proxy is configured on `ScenarioStateContext`, which the
 * reset would otherwise undo.
 */
open class SecurityScanHooks(
    private val properties: SecurityScanProperties,
    private val scanner: SecurityScanner,
    private val session: SecurityScanSession,
    private val bddProperties: BddProperties,
    private val restTemplate: TestRestTemplate,
    private val environment: Environment
) {
    /**
     * Starts the scanner before the first scenario and points the HTTP client of the library at
     * it. Idempotent - it runs before every scenario but only acts once.
     */
    @Before(order = 10)
    fun startScannerAndConfigureProxy() {
        if (!properties.enabled || scanner.isRunning) {
            return
        }

        val targetPort = properties.target.port ?: serverPort()
        session.start(hostPorts = (properties.target.exposedPorts + targetPort).toSet())

        configureProxy()
    }

    /**
     * In replay mode the traffic comes from a stored recording, so the functional scenarios
     * have nothing to contribute and are skipped. Only the scan scenario is allowed to run.
     */
    @Before(order = 11, value = "not @securityExecuteScan")
    fun skipFunctionalScenariosWhenReplaying() {
        if (properties.enabled && properties.recording.replayEnabled) {
            throw TestAbortedException("Skipped: replaying ${properties.recording.replayFrom}")
        }
    }

    /**
     * The port the application under test listens on.
     *
     * Read from the environment rather than injected with `@LocalServerPort`: this bean is
     * created while the context is still refreshing, at which point `local.server.port` does
     * not exist yet. By the time the hook runs, the server is up.
     */
    private fun serverPort(): Int {
        return environment.getProperty("local.server.port", Int::class.java)
            ?: environment.getProperty("server.port", Int::class.java)
            ?: error(
                "Cannot determine the port of the application under test. " +
                    "Set 'cucumbertest.security.target.port'."
            )
    }

    /**
     * The library reads [ScenarioStateContext.dynamicProxyHost]/`Port` whenever it builds its
     * request factory, so setting them is what actually enables the proxy. The factory of the
     * shared TestRestTemplate is rebuilt on top of that, because a glue bean constructed before
     * this hook would otherwise keep a factory that was built without a proxy.
     */
    private fun configureProxy() {
        val proxy = scanner.proxy
        ScenarioStateContext.dynamicProxyHost = proxy.host
        ScenarioStateContext.dynamicProxyPort = proxy.port
        restTemplate.restTemplate.requestFactory =
            ClientHttpRequestFactory(bddProperties = bddProperties).createRequestFactory()

        log.info { "routing cucumber traffic through the security scanner at ${proxy.host}:${proxy.port}" }
    }

    private companion object {
        val log = KotlinLogging.logger {}
    }
}
