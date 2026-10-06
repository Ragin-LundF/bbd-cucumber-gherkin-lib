package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.models.SecurityRisk
import kotlin.test.Test
import kotlin.test.assertEquals

/** Against a trimmed `traditional-json-plus` report as ZAP writes it. */
internal class ZapJsonPlusParserTests {
    private val report = ZapJsonPlusParser.parse(
        json = checkNotNull(javaClass.getResource("zap-report-json-plus.json")).readText()
    )

    @Test
    internal fun `reads the version and every scanned site`() {
        assertEquals(expected = "2.16.1", actual = report.zapVersion)
        assertEquals(expected = "Fri, 2 Oct 2026 09:16:33", actual = report.generated)
        assertEquals(
            expected = listOf("http://host.testcontainers.internal:50000", "http://host.testcontainers.internal:8088"),
            actual = report.sites
        )
    }

    @Test
    internal fun `keeps the site each alert was raised on`() {
        assertEquals(
            expected = listOf(
                "10038" to "http://host.testcontainers.internal:50000",
                "40042" to "http://host.testcontainers.internal:8088",
                "90022" to "http://host.testcontainers.internal:8088"
            ),
            actual = report.alerts.map { it.ruleId to it.site }
        )
    }

    @Test
    internal fun `maps the ZAP risk and confidence codes`() {
        assertEquals(
            expected = listOf(
                SecurityRisk.MEDIUM to "High",
                SecurityRisk.LOW to "Medium",
                SecurityRisk.HIGH to "Confirmed"
            ),
            actual = report.alerts.map { it.risk to it.confidence }
        )
    }

    @Test
    internal fun `splits the paragraphs ZAP wraps around the raw text`() {
        val alert = report.alerts.first()

        assertEquals(
            expected = listOf(
                "Content Security Policy (CSP) is an added layer of security.",
                "It detects <script> injection."
            ),
            actual = alert.description
        )
        assertEquals(
            expected = listOf("Ensure that your web server sets the Content-Security-Policy header."),
            actual = alert.solution
        )
        assertEquals(
            expected = listOf("https://developer.mozilla.org/en-US/docs/Web/Security/CSP", "javascript:alert(1)"),
            actual = alert.references
        )
        assertEquals(expected = emptyList(), actual = alert.otherInfo)
    }

    @Test
    internal fun `reads classification and tags`() {
        val alert = report.alerts.first()

        assertEquals(expected = "693", actual = alert.cweId)
        assertEquals(expected = "15", actual = alert.wascId)
        assertEquals(
            expected = mapOf("OWASP_2021_A05" to "https://owasp.org/Top10/A05_2021-Security_Misconfiguration/"),
            actual = alert.tags
        )
    }

    @Test
    internal fun `reads an instance with its HTTP message`() {
        assertEquals(
            expected = ZapReportInstance(
                uri = "http://host.testcontainers.internal:50000/api/v1/labelling",
                method = "GET",
                parameter = "",
                attack = "",
                evidence = "<script>alert(1)</script>",
                otherInfo = "",
                requestHeader = "GET http://host.testcontainers.internal:50000/api/v1/labelling HTTP/1.1\r\n" +
                    "Host: host.testcontainers.internal:50000\r\n\r\n",
                requestBody = "",
                responseHeader = "HTTP/1.1 200\r\nContent-Type: application/json\r\n\r\n",
                responseBody = """{"id":"42"}"""
            ),
            actual = report.alerts.first().instances.first()
        )
    }

    @Test
    internal fun `tolerates fields ZAP leaves out`() {
        val withoutMessage = report.alerts.first().instances.last()
        val bare = report.alerts.last()

        assertEquals(expected = "", actual = withoutMessage.requestHeader)
        assertEquals(expected = "", actual = withoutMessage.responseBody)
        assertEquals(expected = "Application Error Disclosure", actual = bare.name)
        assertEquals(expected = emptyList(), actual = bare.description)
        assertEquals(expected = emptyMap(), actual = bare.tags)
        assertEquals(expected = "", actual = bare.cweId)
    }

    @Test
    internal fun `reads a report without sites as empty`() {
        val empty = ZapJsonPlusParser.parse(json = """{"@version":"2.16.1","site":[]}""")

        assertEquals(expected = emptyList(), actual = empty.sites)
        assertEquals(expected = emptyList(), actual = empty.alerts)
    }

    @Test
    internal fun `recognises the false positive confidence`() {
        val alerts = ZapJsonPlusParser.parse(
            json = """{"site":[{"@name":"s","alerts":[""" +
                """{"pluginid":"1","confidence":"0"},{"pluginid":"2","confidence":"1"}]}]}"""
        ).alerts

        assertEquals(
            expected = listOf(ZapReportAlert.FALSE_POSITIVE to true, "Low" to false),
            actual = alerts.map { it.confidence to it.falsePositive }
        )
    }
}
