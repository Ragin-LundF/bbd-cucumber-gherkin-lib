package com.ragin.bdd.cucumber.security.zap.report

import com.ragin.bdd.cucumber.security.models.SecurityRisk

/**
 * One alert type on one site, with every instance ZAP found of it.
 *
 * The text fields are lists of paragraphs: ZAP stores them as `<p>..</p>` blocks around raw,
 * unescaped text, so they are split here and escaped only when rendered.
 */
data class ZapReportAlert(
    val site: String,
    val ruleId: String,
    val name: String,
    val risk: SecurityRisk,
    val confidence: String,
    val description: List<String>,
    val solution: List<String>,
    val otherInfo: List<String>,
    val references: List<String>,
    val cweId: String,
    val wascId: String,
    /** Tag name to link, e.g. `OWASP_2021_A05` to the OWASP Top 10 page. */
    val tags: Map<String, String>,
    val instances: List<ZapReportInstance>
)
