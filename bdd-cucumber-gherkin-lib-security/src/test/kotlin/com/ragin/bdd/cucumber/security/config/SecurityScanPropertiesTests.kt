package com.ragin.bdd.cucumber.security.config

import com.ragin.bdd.cucumber.security.models.SecurityRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

/**
 * The configuration is split over one file per group, so the kebab-case keys a project writes
 * in its profile have to keep binding onto those separate types.
 */
internal class SecurityScanPropertiesTests {
    @Test
    internal fun `binds the kebab-case keys of every configuration group`() {
        val properties = bind(
            "cucumbertest.security.enabled" to "true",
            "cucumbertest.security.scanner.image" to "zaproxy/zap-stable:latest",
            "cucumbertest.security.target.host" to "host.testcontainers.internal",
            "cucumbertest.security.target.exposed-ports[0]" to "50000",
            "cucumbertest.security.api.definition-urls[0]" to "http://localhost:50000/openapi.yaml",
            "cucumbertest.security.scan.poll-interval" to "5s",
            "cucumbertest.security.alerts.min-confidence" to "MEDIUM",
            "cucumbertest.security.alerts.ignored-rule-ids[0]" to "40042",
            "cucumbertest.security.report.file-name" to "scan.html",
            "cucumbertest.security.recording.export-path" to "build/recording.har"
        )

        assertTrue(actual = properties.enabled)
        assertEquals(expected = "zaproxy/zap-stable:latest", actual = properties.scanner.image)
        assertEquals(expected = listOf(50000), actual = properties.target.exposedPorts)
        assertEquals(
            expected = listOf("http://localhost:50000/openapi.yaml"),
            actual = properties.api.definitionUrls
        )
        assertEquals(expected = 5, actual = properties.scan.pollInterval.seconds)
        assertEquals(expected = SecurityRisk.MEDIUM, actual = properties.alerts.minConfidence)
        assertEquals(expected = setOf("40042"), actual = properties.alerts.ignoredRuleIds)
        assertEquals(expected = "scan.html", actual = properties.report.fileName)
        assertEquals(expected = "build/recording.har", actual = properties.recording.exportPath)
    }

    @Test
    internal fun `defaults to a disabled scan when nothing is configured`() {
        val properties = bind()

        assertFalse(actual = properties.enabled)
        assertEquals(expected = SecurityRisk.LOW, actual = properties.alerts.minConfidence)
        assertFalse(actual = properties.recording.replayEnabled)
    }

    @Test
    internal fun `enables replay only for a non-blank recording path`() {
        assertTrue(
            actual = bind("cucumbertest.security.recording.replay-from" to "recording.har").recording.replayEnabled
        )
        assertFalse(actual = bind("cucumbertest.security.recording.replay-from" to " ").recording.replayEnabled)
    }

    private fun bind(vararg entries: Pair<String, String>): SecurityScanProperties {
        val source = MapConfigurationPropertySource(entries.toMap())
        return Binder(source)
            .bind("cucumbertest.security", SecurityScanProperties::class.java)
            .orElseGet { SecurityScanProperties() }
    }
}
