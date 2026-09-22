package com.ragin.bdd.cucumber.security.models

/**
 * A single finding, normalised across scanners.
 *
 * [ruleId] is whatever the scanner calls the rule that fired - a ZAP plugin id, for example -
 * and is what `cucumbertest.security.alerts.ignored-rule-ids` matches on.
 */
data class SecurityAlert(
    val ruleId: String,
    val name: String,
    val risk: SecurityRisk,
    val confidence: SecurityRisk,
    val url: String,
    val method: String,
    val parameter: String
) {
    /** One line for the scan log and for the failure message of the gate. */
    fun describe(): String {
        val finding = "[$risk risk, $confidence confidence] $name (rule $ruleId) - $method $url"
        if (parameter.isBlank()) {
            return finding
        }
        return "$finding (parameter '$parameter')"
    }
}
