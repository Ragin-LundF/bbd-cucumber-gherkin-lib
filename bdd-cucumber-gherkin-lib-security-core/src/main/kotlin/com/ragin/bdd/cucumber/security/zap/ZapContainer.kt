package com.ragin.bdd.cucumber.security.zap

import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.utils.KLoggerLogConsumer
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile

/**
 * Owns the lifecycle of the OWASP ZAP daemon container.
 *
 * ZAP serves the proxy and the REST API on the same port, so a single exposed port is enough.
 * The container is started at most once per JVM; [start] is idempotent.
 */
class ZapContainer(private val properties: SecurityScanProperties) {
    private var container: GenericContainer<*>? = null

    val isRunning: Boolean get() = container?.isRunning == true

    val host: String get() = requireStarted().host

    val port: Int get() = requireStarted().getMappedPort(ZAP_PORT)

    val apiBaseUrl: String get() = "http://$host:$port"

    fun start(hostPorts: Set<Int>) {
        if (isRunning) {
            return
        }

        if (hostPorts.isNotEmpty()) {
            log.info { "exposing host ports $hostPorts to the ZAP container" }
            Testcontainers.exposeHostPorts(*hostPorts.toIntArray())
        }

        val started = GenericContainer(properties.scanner.image).apply {
            withExposedPorts(ZAP_PORT)
            withCommand(*buildCommand())
            withLogConsumer(KLoggerLogConsumer(logger = zapLog))
            waitingFor(
                Wait.forHttp(VERSION_PATH)
                    .forPort(ZAP_PORT)
                    .withHeader(HOST_HEADER, API_HOST)
                    .forStatusCode(HTTP_OK)
            )
            withStartupTimeout(properties.scanner.startupTimeout)
            replayFile()?.let { withCopyFileToContainer(MountableFile.forHostPath(it), REPLAY_PATH) }
        }

        log.info { "starting ZAP from image ${properties.scanner.image}" }
        started.start()
        container = started
        log.info { "ZAP proxy and API are available at ${started.host}:${started.getMappedPort(ZAP_PORT)}" }
    }

    fun stop() {
        container?.let { running ->
            log.info { "stopping ZAP" }
            running.stop()
        }
        container = null
    }

    /** Copies a file out of the container onto the host, creating parent directories. */
    fun copyFileFromContainer(containerPath: String, hostPath: Path) {
        Files.createDirectories(hostPath.toAbsolutePath().parent)
        requireStarted().copyFileFromContainer(containerPath, hostPath.toAbsolutePath().toString())
    }

    private fun replayFile(): Path? {
        val configured = properties.recording.replayFrom?.takeIf { it.isNotBlank() } ?: return null
        val path = Path.of(configured)
        require(Files.isRegularFile(path)) { "Recording to replay does not exist: ${path.toAbsolutePath()}" }
        return path
    }

    private fun buildCommand(): Array<String> {
        val command = mutableListOf(
            "zap.sh",
            "-daemon",
            "-host", "0.0.0.0",
            "-port", ZAP_PORT.toString()
        )
        properties.scanner.plugins.forEach { plugin ->
            command += listOf("-addoninstall", plugin)
        }
        // The API is only reachable through the ephemeral port Testcontainers maps on loopback,
        // so an API key would add nothing but a shared secret to pass around.
        listOf(
            "api.disablekey=true",
            "api.addrs.addr.name=.*",
            "api.addrs.addr.regex=true",
            "api.incerrordetails=true"
        ).forEach { config ->
            command += listOf("-config", config)
        }
        return command.toTypedArray()
    }

    private fun requireStarted(): GenericContainer<*> {
        return checkNotNull(container) { "ZAP is not running. Is 'cucumbertest.security.enabled' set to true?" }
    }

    companion object {
        const val ZAP_PORT = 8080
        const val REPLAY_PATH = "/home/zap/replay.har"

        /**
         * ZAP listens for the proxy and for its own API on the same port and tells them apart
         * by the `Host` header: only the reserved name `zap` reaches the API. Addressing it as
         * `localhost:<mappedPort>` - which is all Testcontainers can offer - would be
         * interpreted as "please proxy a request to localhost:<mappedPort>" instead.
         */
        const val API_HOST = "zap"
        const val HOST_HEADER = "Host"
        private const val VERSION_PATH = "/JSON/core/view/version/"
        private const val HTTP_OK = 200
        private val log = KotlinLogging.logger {}
        private val zapLog = KotlinLogging.logger("com.ragin.bdd.cucumber.security.zap.output")
    }
}
