package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.models.SecurityRisk
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlAlertSection.appendAlert
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.cssClass
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.cweLink
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.escape
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.label
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.riskBadge
import java.nio.file.Files
import java.nio.file.Path

/**
 * Renders a [ZapReport] as one self-contained, static HTML file: every alert with description,
 * solution, references and the request and response of each instance.
 *
 * One file on purpose: CI servers archive and publish reports file by file, and a stylesheet next
 * to the page is easily lost on the way. The styles are therefore embedded, and there is no
 * JavaScript and no external resource at all - the instances fold away in `<details>`, see
 * [ZapHtmlAlertSection].
 *
 * Jenkins serves published HTML with `style-src 'self'` by default, which drops the embedded
 * styles. The page is plain semantic HTML - headings, tables, the risk as text - so it stays
 * complete and readable unstyled; allowing `'unsafe-inline'` styles in the Jenkins
 * Content-Security-Policy brings the look back.
 */
object ZapHtmlReport {
    private val RISKS_DESCENDING = SecurityRisk.entries.sortedByDescending { it.level }

    private val STYLESHEET: String by lazy {
        checkNotNull(ZapHtmlReport::class.java.getResource("zap-report.css")) {
            "report stylesheet missing"
        }.readText()
    }

    /** Writes the page to [destination], creating its directory. */
    @JvmStatic
    fun write(report: ZapReport, title: String, destination: Path) {
        Files.createDirectories(destination.toAbsolutePath().parent)
        Files.writeString(destination, render(report = report, title = title))
    }

    @JvmStatic
    fun render(report: ZapReport, title: String): String {
        val alerts = report.alerts.sortedWith(
            compareByDescending<ZapReportAlert> { it.risk.level }.thenBy { it.name }.thenBy { it.site }
        )
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("<meta charset=\"utf-8\">")
            appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            appendLine("<title>${escape(value = title)}</title>")
            appendLine("<style>")
            append(STYLESHEET)
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<main>")
            appendHeader(report = report, title = title)
            appendSummary(alerts = alerts)
            appendIndex(alerts = alerts)
            alerts.forEachIndexed { index, alert -> appendAlert(anchor = anchor(index = index), alert = alert) }
            appendLine("</main>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }

    private fun StringBuilder.appendHeader(report: ZapReport, title: String) {
        appendLine("<header>")
        appendLine("<p class=\"eyebrow\">Security scan report</p>")
        appendLine("<h1>${escape(value = title)}</h1>")
        val version = escape(value = report.zapVersion)
        appendLine("<p class=\"meta\">ZAP $version &middot; ${escape(value = report.generated)}</p>")
        if (report.sites.isNotEmpty()) {
            appendLine("<ul class=\"sites\">")
            report.sites.forEach { site -> appendLine("<li class=\"mono\">${escape(value = site)}</li>") }
            appendLine("</ul>")
        }
        appendLine("</header>")
    }

    private fun StringBuilder.appendSummary(alerts: List<ZapReportAlert>) {
        appendLine("<section class=\"summary\" aria-label=\"Alerts per risk\">")
        RISKS_DESCENDING.forEach { risk ->
            val ofRisk = alerts.filter { it.risk == risk }
            appendLine("<div class=\"tile tile-${cssClass(risk = risk)}\">")
            append("<span class=\"count\">${ofRisk.size}</span>")
            append("<span class=\"label\">${label(risk = risk)}</span>")
            appendLine("<span class=\"muted\">${ofRisk.sumOf { it.instances.size }} instances</span>")
            appendLine("</div>")
        }
        appendLine("</section>")
    }

    private fun StringBuilder.appendIndex(alerts: List<ZapReportAlert>) {
        if (alerts.isEmpty()) {
            appendLine("<p class=\"empty\">No alerts.</p>")
            return
        }
        appendLine("<section>")
        appendLine("<h2>Alerts <span class=\"badge-count\">${alerts.size}</span></h2>")
        appendLine("<div class=\"table-wrap\">")
        appendLine("<table>")
        append("<thead><tr><th>Risk</th><th>Alert</th><th>Rule</th><th>CWE</th>")
        appendLine("<th>Site</th><th>Instances</th></tr></thead>")
        appendLine("<tbody>")
        alerts.forEachIndexed { index, alert ->
            append("<tr>")
            append("<td>${riskBadge(risk = alert.risk)}</td>")
            append("<td><a href=\"#${anchor(index = index)}\">${escape(value = alert.name)}</a></td>")
            append("<td class=\"mono nowrap\">${escape(value = alert.ruleId)}</td>")
            append("<td class=\"mono nowrap\">${cweLink(cweId = alert.cweId)}</td>")
            append("<td class=\"mono\">${escape(value = alert.site)}</td>")
            append("<td class=\"mono\">${alert.instances.size}</td>")
            appendLine("</tr>")
        }
        appendLine("</tbody>")
        appendLine("</table>")
        appendLine("</div>")
        appendLine("</section>")
    }

    private fun anchor(index: Int): String {
        return "alert-${index + 1}"
    }
}
