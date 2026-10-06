package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.models.SecurityRisk
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val SITE = "http://host.testcontainers.internal:50000"

internal class ZapHtmlReportTests {
    @Test
    internal fun `counts the alerts and their instances per risk`() {
        val html = render(
            alert(risk = SecurityRisk.HIGH, instances = listOf(instance(), instance())),
            alert(risk = SecurityRisk.HIGH, name = "Second high"),
            alert(risk = SecurityRisk.LOW, instances = listOf(instance()))
        )

        assertTrue(actual = html.contains(tile(risk = "high", alerts = 2, instances = "2 instance(s)")))
        assertTrue(actual = html.contains(tile(risk = "medium", alerts = 0, instances = "0 instance(s)")))
        assertTrue(actual = html.contains(tile(risk = "low", alerts = 1, instances = "1 instance(s)")))
        assertTrue(actual = html.contains(tile(risk = "informational", alerts = 0, instances = "0 instance(s)")))
    }

    @Test
    internal fun `lists the alerts most severe first and links each to its section`() {
        val html = render(
            alert(risk = SecurityRisk.LOW, name = "Low alert"),
            alert(risk = SecurityRisk.HIGH, name = "High alert")
        )

        assertTrue(actual = html.contains("<a href=\"#alert-1\">High alert</a>"))
        assertTrue(actual = html.contains("<a href=\"#alert-2\">Low alert</a>"))
        assertTrue(actual = html.indexOf("id=\"alert-1\"") < html.indexOf("id=\"alert-2\""))
    }

    @Test
    internal fun `says so when there is no alert`() {
        val html = render()

        assertTrue(actual = html.contains("<p class=\"empty\">No alerts.</p>"))
        assertFalse(actual = html.contains("<table>"))
    }

    @Test
    internal fun `leaves false positives out of the summary and the alert list`() {
        val html = render(
            alert(risk = SecurityRisk.HIGH, name = "Open finding"),
            alert(risk = SecurityRisk.HIGH, name = "Suppressed finding", confidence = ZapReportAlert.FALSE_POSITIVE)
        )

        assertTrue(actual = html.contains(tile(risk = "high", alerts = 1, instances = "0 instance(s)")))
        assertTrue(actual = html.contains("<h2>Alerts <span class=\"badge-count\">1</span></h2>"))
        assertTrue(actual = html.contains("<a href=\"#alert-1\">Open finding</a>"))
        assertFalse(actual = html.contains("id=\"alert-2\""))
    }

    @Test
    internal fun `lists suppressed alerts in a section of their own after the findings`() {
        val html = render(
            alert(name = "Suppressed finding", confidence = ZapReportAlert.FALSE_POSITIVE),
            alert(name = "Open finding")
        )

        val section = html.indexOf("<div class=\"suppressed-alerts\">")
        assertTrue(actual = section > html.indexOf("id=\"alert-1\""))
        assertTrue(
            actual = html.contains(
                "<h2>Suppressed alerts <span class=\"badge-count\">1</span></h2>"
            )
        )
        assertTrue(actual = html.contains("<a href=\"#suppressed-1\">Suppressed finding</a>"))
        assertTrue(actual = html.indexOf("id=\"suppressed-1\"") > section)
        assertTrue(actual = html.contains("<dt>Confidence</dt><dd>False Positive</dd>"))
    }

    @Test
    internal fun `shows no suppressed section without suppressed alerts`() {
        val html = render(alert())

        assertFalse(actual = html.contains("<div class=\"suppressed-alerts\">"))
        assertFalse(actual = html.contains("<h2>Suppressed alerts"))
    }

    @Test
    internal fun `says there is no open alert when every alert is a false positive`() {
        val html = render(alert(confidence = ZapReportAlert.FALSE_POSITIVE))

        assertTrue(actual = html.contains("<p class=\"empty\">No alerts.</p>"))
        assertTrue(actual = html.contains("<a href=\"#suppressed-1\">"))
    }

    @Test
    internal fun `folds every instance with its request and response into details`() {
        val html = render(alert(instances = listOf(instance())))

        assertTrue(
            actual = html.contains(
                "<details>\n<summary class=\"mono\">GET $SITE/api/v1/labelling</summary>"
            )
        )
        assertTrue(actual = html.contains("<dt>Evidence</dt><dd>X-Powered-By</dd>"))
        assertFalse(actual = html.contains("<dt>Attack</dt>"))
        assertTrue(
            actual = html.contains(
                "<h4>Request</h4>\n<pre>GET $SITE/api/v1/labelling HTTP/1.1</pre>"
            )
        )
        assertTrue(
            actual = html.contains(
                "<h4>Response</h4>\n<pre>HTTP/1.1 200\n\n{&quot;id&quot;:&quot;42&quot;}</pre>"
            )
        )
    }

    @Test
    internal fun `escapes everything taken from the scanned traffic`() {
        val html = render(
            alert(
                name = "<img src=x onerror=alert(1)>",
                description = listOf("It detects <script> injection."),
                instances = listOf(instance(evidence = "<script>alert(1)</script>"))
            )
        )

        assertFalse(actual = html.contains("<script>"))
        assertFalse(actual = html.contains("<img"))
        assertTrue(actual = html.contains("<p>It detects &lt;script&gt; injection.</p>"))
        assertTrue(actual = html.contains("<dd>&lt;script&gt;alert(1)&lt;/script&gt;</dd>"))
    }

    @Test
    internal fun `cuts a long message and says by how much`() {
        val body = "a".repeat(n = ZapHtmlAlertSection.MAX_MESSAGE_LENGTH + 1)
        val html = render(alert(instances = listOf(instance(responseHeader = "", responseBody = body))))

        assertTrue(
            actual = html.contains(
                "a".repeat(n = ZapHtmlAlertSection.MAX_MESSAGE_LENGTH) +
                    "\n\n[truncated, ${ZapHtmlAlertSection.MAX_MESSAGE_LENGTH + 1} characters in total]</pre>"
            )
        )
    }

    @Test
    internal fun `keeps a message of exactly the limit complete`() {
        val body = "a".repeat(n = ZapHtmlAlertSection.MAX_MESSAGE_LENGTH)
        val html = render(alert(instances = listOf(instance(responseHeader = "", responseBody = body))))

        assertTrue(actual = html.contains("<pre>$body</pre>"))
        assertFalse(actual = html.contains("[truncated"))
    }

    @Test
    internal fun `links only web references and tags`() {
        val html = render(
            alert(
                references = listOf("https://developer.mozilla.org/CSP", "javascript:alert(1)"),
                tags = mapOf("OWASP_2021_A05" to "https://owasp.org/Top10/A05/", "local" to "file:///etc/passwd")
            )
        )

        assertTrue(
            actual = html.contains(
                "<a href=\"https://developer.mozilla.org/CSP\" rel=\"noopener noreferrer\">" +
                    "https://developer.mozilla.org/CSP</a>"
            )
        )
        assertTrue(actual = html.contains("<li>javascript:alert(1)</li>"))
        assertTrue(
            actual = html.contains(
                "<a href=\"https://owasp.org/Top10/A05/\" rel=\"noopener noreferrer\">OWASP_2021_A05</a>"
            )
        )
        assertTrue(actual = html.contains("<li>local</li>"))
    }

    @Test
    internal fun `links the CWE only when the rule has one`() {
        val withCwe = render(alert(cweId = "693"))
        val withoutCwe = render(alert(cweId = "-1"))

        assertTrue(
            actual = withCwe.contains(
                "<a href=\"https://cwe.mitre.org/data/definitions/693.html\" rel=\"noopener noreferrer\">CWE-693</a>"
            )
        )
        assertFalse(actual = withoutCwe.contains("CWE-"))
        assertFalse(actual = withoutCwe.contains("<dt>CWE</dt>"))
    }

    @Test
    internal fun `is one self-contained file without scripts`() {
        val html = render(alert())

        assertTrue(actual = html.contains("<style>"))
        assertTrue(actual = html.contains(".sev-high"))
        assertFalse(actual = html.contains("<script"))
        assertFalse(actual = html.contains("<link"))
    }

    @Test
    internal fun `shows title, ZAP version and the scanned sites`() {
        val html = ZapHtmlReport.render(
            report = ZapReport(zapVersion = "2.16.1", generated = "today", sites = listOf(SITE), alerts = emptyList()),
            title = "Scan <of> labelling"
        )

        assertTrue(actual = html.contains("<title>Scan &lt;of&gt; labelling</title>"))
        assertTrue(actual = html.contains("<p class=\"meta\">ZAP 2.16.1 &middot; today</p>"))
        assertTrue(actual = html.contains("<li class=\"mono\">$SITE</li>"))
    }

    @Test
    internal fun `writes the page and creates its directory`() {
        val directory = Files.createTempDirectory("zap-report")
        val destination = directory.resolve("nested/security-report.html")

        ZapHtmlReport.write(report = report(alert()), title = "Scan", destination = destination)

        assertEquals(expected = render(alert()), actual = Files.readString(destination))
        directory.toFile().deleteRecursively()
    }

    private fun render(vararg alerts: ZapReportAlert): String {
        return ZapHtmlReport.render(report = report(*alerts), title = "Scan")
    }

    private fun report(vararg alerts: ZapReportAlert): ZapReport {
        return ZapReport(zapVersion = "2.16.1", generated = "today", sites = listOf(SITE), alerts = alerts.toList())
    }

    private fun tile(risk: String, alerts: Int, instances: String): String {
        val label = risk.replaceFirstChar(Char::uppercase)
        return "<div class=\"tile tile-$risk\">\n<span class=\"count\">$alerts</span>" +
            "<span class=\"label\">$label</span><span class=\"muted\">$instances</span>"
    }

    @Suppress("LongParameterList") // a test builder: every field can be varied by name
    private fun alert(
        risk: SecurityRisk = SecurityRisk.MEDIUM,
        name: String = "Content Security Policy (CSP) Header Not Set",
        confidence: String = "High",
        description: List<String> = emptyList(),
        references: List<String> = emptyList(),
        tags: Map<String, String> = emptyMap(),
        cweId: String = "",
        instances: List<ZapReportInstance> = emptyList()
    ): ZapReportAlert {
        return ZapReportAlert(
            site = SITE,
            ruleId = "10038",
            name = name,
            risk = risk,
            confidence = confidence,
            description = description,
            solution = emptyList(),
            otherInfo = emptyList(),
            references = references,
            cweId = cweId,
            wascId = "",
            tags = tags,
            instances = instances
        )
    }

    private fun instance(
        evidence: String = "X-Powered-By",
        responseHeader: String = "HTTP/1.1 200\r\n\r\n",
        responseBody: String = """{"id":"42"}"""
    ): ZapReportInstance {
        return ZapReportInstance(
            uri = "$SITE/api/v1/labelling",
            method = "GET",
            parameter = "",
            attack = "",
            evidence = evidence,
            otherInfo = "",
            requestHeader = "GET $SITE/api/v1/labelling HTTP/1.1\r\n\r\n",
            requestBody = "",
            responseHeader = responseHeader,
            responseBody = responseBody
        )
    }
}
