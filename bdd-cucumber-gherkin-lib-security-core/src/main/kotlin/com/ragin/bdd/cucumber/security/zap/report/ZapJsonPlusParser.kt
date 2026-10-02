package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.models.SecurityRisk
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

/**
 * Reads the ZAP `traditional-json-plus` report into a [ZapReport].
 *
 * Lenient about missing fields, because the template grows with ZAP versions and an instance
 * carries the HTTP message only while ZAP still has it.
 */
object ZapJsonPlusParser {
    private val mapper = JsonMapper.builder().build()

    /** The labels ZAP uses for its confidence codes 0 to 4. */
    private val CONFIDENCES = listOf("False Positive", "Low", "Medium", "High", "Confirmed")

    @JvmStatic
    fun parse(json: String): ZapReport {
        val root = mapper.readTree(json)
        val sites = root.path("site").toList()
        return ZapReport(
            zapVersion = root.text(field = "@version"),
            generated = root.text(field = "@generated"),
            sites = sites.map { site -> site.text(field = "@name") },
            alerts = sites.flatMap { site ->
                site.path("alerts").toList().map { alert -> toAlert(site = site.text(field = "@name"), alert = alert) }
            }
        )
    }

    private fun toAlert(site: String, alert: JsonNode): ZapReportAlert {
        return ZapReportAlert(
            site = site,
            ruleId = alert.text(field = "pluginid"),
            name = alert.text(field = "name").ifBlank { alert.text(field = "alert") },
            risk = riskOf(code = alert.text(field = "riskcode")),
            confidence = CONFIDENCES.getOrElse(alert.text(field = "confidence").toIntOrNull() ?: -1) { "" },
            description = paragraphs(value = alert.text(field = "desc")),
            solution = paragraphs(value = alert.text(field = "solution")),
            otherInfo = paragraphs(value = alert.text(field = "otherinfo")),
            references = paragraphs(value = alert.text(field = "reference")),
            cweId = alert.text(field = "cweid"),
            wascId = alert.text(field = "wascid"),
            tags = alert.path("tags").toList().associate { tag -> tag.text(field = "tag") to tag.text(field = "link") },
            instances = alert.path("instances").toList().map(::toInstance)
        )
    }

    private fun toInstance(instance: JsonNode): ZapReportInstance {
        return ZapReportInstance(
            uri = instance.text(field = "uri"),
            method = instance.text(field = "method"),
            parameter = instance.text(field = "param"),
            attack = instance.text(field = "attack"),
            evidence = instance.text(field = "evidence"),
            otherInfo = instance.text(field = "otherinfo"),
            requestHeader = instance.text(field = "request-header"),
            requestBody = instance.text(field = "request-body"),
            responseHeader = instance.text(field = "response-header"),
            responseBody = instance.text(field = "response-body")
        )
    }

    /** ZAP risk codes: 0 informational, 1 low, 2 medium, 3 high. */
    private fun riskOf(code: String): SecurityRisk {
        return SecurityRisk.entries.firstOrNull { risk -> risk.level.toString() == code } ?: SecurityRisk.INFORMATIONAL
    }

    /**
     * Splits ZAP's `<p>first</p><p>second</p>` into its paragraphs. ZAP adds only these tags; the
     * text between them is raw, so nothing else is treated as markup.
     */
    private fun paragraphs(value: String): List<String> {
        return value.removePrefix("<p>")
            .removeSuffix("</p>")
            .split("</p><p>")
            .map(String::trim)
            .filter(String::isNotEmpty)
    }

    private fun JsonNode.text(field: String): String {
        return path(field).asString("")
    }
}
