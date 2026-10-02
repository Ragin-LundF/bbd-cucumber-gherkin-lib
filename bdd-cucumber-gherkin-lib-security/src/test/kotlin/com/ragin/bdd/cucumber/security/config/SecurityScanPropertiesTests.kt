package com.ragin.bdd.cucumber.security.config

import com.ragin.bdd.cucumber.security.models.SecurityRisk
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
            "cucumbertest.security.scanner.browser-enabled" to "true",
            "cucumbertest.security.target.host" to "host.testcontainers.internal",
            "cucumbertest.security.target.exposed-ports[0]" to "50000",
            "cucumbertest.security.api.definition-urls[0]" to "http://localhost:50000/openapi.yaml",
            "cucumbertest.security.scan.poll-interval" to "5s",
            "cucumbertest.security.alerts.min-confidence" to "MEDIUM",
            "cucumbertest.security.alerts.ignored-rule-ids[0]" to "40042",
            "cucumbertest.security.report.templates[0].template" to "traditional-html-plus",
            "cucumbertest.security.report.templates[0].file-name" to "scan.html",
            "cucumbertest.security.report.templates[1].template" to "traditional-xml",
            "cucumbertest.security.report.templates[1].file-name" to "scan.xml",
            "cucumbertest.security.recording.export-path" to "build/recording.har"
        )

        assertTrue(actual = properties.enabled)
        assertEquals(expected = "zaproxy/zap-stable:latest", actual = properties.scanner.image)
        assertTrue(actual = properties.scanner.browserEnabled)
        assertEquals(expected = listOf(50000), actual = properties.target.exposedPorts)
        assertEquals(
            expected = listOf("http://localhost:50000/openapi.yaml"),
            actual = properties.api.definitionUrls
        )
        assertEquals(expected = 5, actual = properties.scan.pollInterval.seconds)
        assertEquals(expected = SecurityRisk.MEDIUM, actual = properties.alerts.minConfidence)
        assertEquals(expected = setOf("40042"), actual = properties.alerts.ignoredRuleIds)
        assertEquals(
            expected = listOf(
                ReportTemplateProperties(template = "traditional-html-plus", fileName = "scan.html"),
                ReportTemplateProperties(template = "traditional-xml", fileName = "scan.xml")
            ),
            actual = properties.report.templates
        )
        assertEquals(expected = "build/recording.har", actual = properties.recording.exportPath)
    }

    @Test
    internal fun `binds the alert filters with the field names of the ZAP automation framework`() {
        val filters = bind(
            "cucumbertest.security.alerts.alert-filter[0].rule-id" to "10038",
            "cucumbertest.security.alerts.alert-filter[0].rule-name" to "CSP Header Not Set",
            "cucumbertest.security.alerts.alert-filter[0].new-risk" to "False Positive",
            "cucumbertest.security.alerts.alert-filter[0].url" to ".*/actuator/.*",
            "cucumbertest.security.alerts.alert-filter[0].url-regex" to "true",
            "cucumbertest.security.alerts.alert-filter[0].parameter" to "id",
            "cucumbertest.security.alerts.alert-filter[0].parameter-regex" to "true",
            "cucumbertest.security.alerts.alert-filter[0].attack" to "<script>",
            "cucumbertest.security.alerts.alert-filter[0].attack-regex" to "true",
            "cucumbertest.security.alerts.alert-filter[0].evidence" to "Server",
            "cucumbertest.security.alerts.alert-filter[0].evidence-regex" to "true",
            "cucumbertest.security.alerts.alert-filter[0].methods[0]" to "GET",
            "cucumbertest.security.alerts.alert-filter[0].methods[1]" to "POST",
            "cucumbertest.security.alerts.alert-filter[1].rule-id" to "10021",
            "cucumbertest.security.alerts.alert-filter[1].new-risk" to "Info"
        ).alerts.alertFilter

        assertEquals(
            expected = listOf(
                AlertFilterProperties(
                    ruleId = "10038",
                    ruleName = "CSP Header Not Set",
                    newRisk = AlertFilterRisk.FALSE_POSITIVE,
                    url = ".*/actuator/.*",
                    urlRegex = true,
                    parameter = "id",
                    parameterRegex = true,
                    attack = "<script>",
                    attackRegex = true,
                    evidence = "Server",
                    evidenceRegex = true,
                    methods = listOf("GET", "POST")
                ),
                AlertFilterProperties(ruleId = "10021", newRisk = AlertFilterRisk.INFO)
            ),
            actual = filters
        )
    }

    @Test
    internal fun `defaults to a disabled scan when nothing is configured`() {
        val properties = bind()

        assertEquals(expected = emptyList(), actual = properties.alerts.alertFilter)
        assertFalse(actual = properties.enabled)
        assertFalse(actual = properties.scanner.browserEnabled)
        assertEquals(expected = SecurityRisk.LOW, actual = properties.alerts.minConfidence)
        assertFalse(actual = properties.recording.replayEnabled)
        assertEquals(
            expected = listOf(
                ReportTemplateProperties(
                    template = ReportProperties.OWN_HTML_TEMPLATE,
                    fileName = "security-report.html"
                ),
                ReportTemplateProperties(template = "traditional-xml", fileName = "security-report.xml")
            ),
            actual = properties.report.templates
        )
    }

    @Test
    internal fun `refuses a report with a blank file name`() {
        val failure = assertFailsWith<BindException> {
            bind(
                "cucumbertest.security.report.templates[0].template" to "traditional-xml",
                "cucumbertest.security.report.templates[0].file-name" to " "
            )
        }

        assertIs<IllegalArgumentException>(value = failure.rootCause())
    }

    @Test
    internal fun `enables replay only for a non-blank recording path`() {
        assertTrue(
            actual = bind("cucumbertest.security.recording.replay-from" to "recording.har").recording.replayEnabled
        )
        assertFalse(actual = bind("cucumbertest.security.recording.replay-from" to " ").recording.replayEnabled)
    }

    private fun Throwable.rootCause(): Throwable {
        return generateSequence(this) { it.cause }.last()
    }

    private fun bind(vararg entries: Pair<String, String>): SecurityScanProperties {
        val source = MapConfigurationPropertySource(entries.toMap())
        return Binder(source)
            .bind("cucumbertest.security", SecurityScanProperties::class.java)
            .orElseGet { SecurityScanProperties() }
    }
}
