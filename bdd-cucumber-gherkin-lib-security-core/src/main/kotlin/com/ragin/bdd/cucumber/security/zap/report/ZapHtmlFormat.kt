package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.models.SecurityRisk

/** The small HTML pieces both the page and the alert sections of the report are built from. */
internal object ZapHtmlFormat {
    /** Everything in the report comes from scanned requests and responses, so every value is escaped. */
    fun escape(value: String): String {
        return buildString(value.length) {
            value.forEach { char ->
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(char)
                }
            }
        }
    }

    /** Only web links become clickable: the values come from scan rules, not from this library. */
    fun link(target: String, text: String): String {
        if (!target.startsWith("https://") && !target.startsWith("http://")) {
            return escape(value = text)
        }
        return "<a href=\"${escape(value = target)}\" rel=\"noopener noreferrer\">${escape(value = text)}</a>"
    }

    /** ZAP reports `-1` or `0` when a rule has no CWE. */
    fun cweLink(cweId: String): String {
        val id = cweId.toIntOrNull()?.takeIf { it > 0 } ?: return ""
        return link(target = "https://cwe.mitre.org/data/definitions/$id.html", text = "CWE-$id")
    }

    fun riskBadge(risk: SecurityRisk): String {
        return "<span class=\"sev sev-${cssClass(risk = risk)}\">${label(risk = risk)}</span>"
    }

    fun cssClass(risk: SecurityRisk): String {
        return risk.name.lowercase()
    }

    fun label(risk: SecurityRisk): String {
        return risk.name.lowercase().replaceFirstChar(Char::uppercase)
    }
}
