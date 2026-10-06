package com.ragin.bdd.cucumber.security.zap

import com.ragin.bdd.cucumber.security.config.ScannerProperties
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import kotlin.test.Test
import kotlin.test.assertEquals
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

    private fun recoveryLogConfigs(command: List<String>): List<String> {
        return command.windowed(size = 2)
            .filter { (option, value) -> option == "-config" && value.startsWith("database.recoverylog=") }
            .map { (_, value) -> value }
    }
}
