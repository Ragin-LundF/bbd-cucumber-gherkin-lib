package com.ragin.bdd.cucumber.security.zap

import com.github.dockerjava.api.model.LogConfig
import com.ragin.bdd.cucumber.security.config.ScannerProperties
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The command line the container is started with; building it starts nothing. */
internal class ZapContainerTests {
    @Test
    internal fun `switches the database recovery log off by default`() {
        val command = ZapContainer(properties = SecurityScanProperties()).buildCommand().toList()

        assertEquals(expected = listOf("database.recoverylog=false"), actual = recoveryLogConfigs(command = command))
    }

    @Test
    internal fun `writes the database recovery log when enabled`() {
        val properties = SecurityScanProperties(scanner = ScannerProperties(databaseRecoveryLog = true))

        val command = ZapContainer(properties = properties).buildCommand().toList()

        assertEquals(expected = listOf("database.recoverylog=true"), actual = recoveryLogConfigs(command = command))
    }

    @Test
    internal fun `keeps the API configuration next to the recovery log setting`() {
        val command = ZapContainer(properties = SecurityScanProperties()).buildCommand().toList()

        assertEquals(expected = listOf("zap.sh", "-daemon"), actual = command.take(n = 2))
        assertTrue(actual = command.windowed(size = 2).contains(listOf("-config", "api.disablekey=true")))
    }

    @Test
    internal fun `leaves the stored body sizes to ZAP by default`() {
        val command = ZapContainer(properties = SecurityScanProperties()).buildCommand().toList()

        assertEquals(expected = emptyList(), actual = bodySizeConfigs(command = command))
    }

    @Test
    internal fun `limits the stored request and response bodies when configured`() {
        val scanner = ScannerProperties(maxRequestBodySize = 1048576, maxResponseBodySize = 65536)

        val command = ZapContainer(properties = SecurityScanProperties(scanner = scanner)).buildCommand().toList()

        assertEquals(
            expected = listOf("database.request.bodysize=1048576", "database.response.bodysize=65536"),
            actual = bodySizeConfigs(command = command)
        )
    }

    @Test
    internal fun `limits only the response body when only that is configured`() {
        val scanner = ScannerProperties(maxResponseBodySize = 65536)

        val command = ZapContainer(properties = SecurityScanProperties(scanner = scanner)).buildCommand().toList()

        assertEquals(expected = listOf("database.response.bodysize=65536"), actual = bodySizeConfigs(command = command))
    }

    @Test
    internal fun `keeps the log settings of the Docker daemon by default`() {
        assertNull(actual = ZapContainer(properties = SecurityScanProperties()).logConfig())
    }

    @Test
    internal fun `keeps the log settings of the Docker daemon when the log size is blank`() {
        val properties = SecurityScanProperties(scanner = ScannerProperties(containerLogMaxSize = ""))

        assertNull(actual = ZapContainer(properties = properties).logConfig())
    }

    @Test
    internal fun `caps the container log to one json file of the configured size`() {
        val properties = SecurityScanProperties(scanner = ScannerProperties(containerLogMaxSize = "50m"))

        val logConfig = ZapContainer(properties = properties).logConfig()

        assertEquals(expected = LogConfig.LoggingType.JSON_FILE, actual = logConfig?.type)
        assertEquals(expected = mapOf("max-size" to "50m", "max-file" to "1"), actual = logConfig?.config)
    }

    private fun recoveryLogConfigs(command: List<String>): List<String> {
        return configs(command = command).filter { config -> config.startsWith(prefix = "database.recoverylog=") }
    }

    private fun bodySizeConfigs(command: List<String>): List<String> {
        return configs(command = command).filter { config -> ".bodysize=" in config }
    }

    private fun configs(command: List<String>): List<String> {
        return command.windowed(size = 2)
            .filter { (option, _) -> option == "-config" }
            .map { (_, value) -> value }
    }
}
