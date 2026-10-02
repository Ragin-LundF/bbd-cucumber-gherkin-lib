package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.cweLink
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.escape
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.link
import com.ragin.bdd.cucumber.security.zap.report.ZapHtmlFormat.riskBadge

/** The section of one alert in [ZapHtmlReport]: what it is, how to fix it and where it was found. */
internal object ZapHtmlAlertSection {
    /**
     * Longest header plus body shown per message. A response can be a whole page or a large JSON
     * document, and a few hundred of them would make the report too large to open.
     */
    const val MAX_MESSAGE_LENGTH = 20_000

    fun StringBuilder.appendAlert(anchor: String, alert: ZapReportAlert) {
        appendLine("<section class=\"alert\" id=\"$anchor\">")
        appendLine("<h2>${riskBadge(risk = alert.risk)} ${escape(value = alert.name)}</h2>")
        appendLine("<dl class=\"facts\">")
        appendFact(term = "Rule", value = escape(value = alert.ruleId))
        appendFact(term = "Confidence", value = escape(value = alert.confidence))
        appendFact(term = "Site", value = escape(value = alert.site))
        appendFact(term = "CWE", value = cweLink(cweId = alert.cweId))
        appendFact(term = "WASC", value = escape(value = alert.wascId))
        appendLine("</dl>")
        appendParagraphs(heading = "Description", paragraphs = alert.description)
        appendParagraphs(heading = "Solution", paragraphs = alert.solution)
        appendParagraphs(heading = "Other information", paragraphs = alert.otherInfo)
        appendLinks(heading = "References", links = alert.references.map { reference -> reference to reference })
        appendLinks(heading = "Tags", links = alert.tags.map { (tag, target) -> target to tag })
        appendLine("<h3>Instances <span class=\"badge-count\">${alert.instances.size}</span></h3>")
        alert.instances.forEach { instance -> appendInstance(instance = instance) }
        appendLine("</section>")
    }

    private fun StringBuilder.appendInstance(instance: ZapReportInstance) {
        appendLine("<details>")
        appendLine(
            "<summary class=\"mono\">${escape(value = instance.method)} ${escape(value = instance.uri)}</summary>"
        )
        appendLine("<dl class=\"facts\">")
        appendFact(term = "Parameter", value = escape(value = instance.parameter))
        appendFact(term = "Attack", value = escape(value = instance.attack))
        appendFact(term = "Evidence", value = escape(value = instance.evidence))
        appendFact(term = "Other information", value = escape(value = instance.otherInfo))
        appendLine("</dl>")
        appendMessage(heading = "Request", header = instance.requestHeader, body = instance.requestBody)
        appendMessage(heading = "Response", header = instance.responseHeader, body = instance.responseBody)
        appendLine("</details>")
    }

    private fun StringBuilder.appendMessage(heading: String, header: String, body: String) {
        val message = listOf(header.trimEnd(), body).filter(String::isNotBlank).joinToString(separator = "\n\n")
        if (message.isEmpty()) {
            return
        }
        appendLine("<h4>$heading</h4>")
        appendLine("<pre>${escape(value = truncate(value = message))}</pre>")
    }

    /** Leaves out facts ZAP left empty, so an instance without an attack has no empty row. */
    private fun StringBuilder.appendFact(term: String, value: String) {
        if (value.isBlank()) {
            return
        }
        appendLine("<dt>$term</dt><dd>$value</dd>")
    }

    private fun StringBuilder.appendParagraphs(heading: String, paragraphs: List<String>) {
        if (paragraphs.isEmpty()) {
            return
        }
        appendLine("<h3>$heading</h3>")
        paragraphs.forEach { paragraph -> appendLine("<p>${escape(value = paragraph)}</p>") }
    }

    /** [links] are pairs of target and the text shown. */
    private fun StringBuilder.appendLinks(heading: String, links: List<Pair<String, String>>) {
        if (links.isEmpty()) {
            return
        }
        appendLine("<h3>$heading</h3>")
        appendLine("<ul>")
        links.forEach { (target, text) -> appendLine("<li>${link(target = target, text = text)}</li>") }
        appendLine("</ul>")
    }

    private fun truncate(value: String): String {
        if (value.length <= MAX_MESSAGE_LENGTH) {
            return value
        }
        return value.take(MAX_MESSAGE_LENGTH) + "\n\n[truncated, ${value.length} characters in total]"
    }
}
