package com.ragin.bdd.cucumber.security.config

/**
 * One alert filter that changes the risk of matching alerts, e.g. to accept a finding on a
 * single URL instead of ignoring the whole rule.
 *
 * The fields mirror an entry of `alertFilters` in the ZAP Automation Framework `alertFilter` job
 * (https://www.zaproxy.org/docs/desktop/addons/alert-filters/automation/), so a filter from a ZAP
 * plan can be copied over unchanged. `context` is not supported: the scan creates no context, so
 * every filter is global.
 */
data class AlertFilterProperties @JvmOverloads constructor(
    /** Mandatory, the scan rule id or the alert reference. */
    val ruleId: String,
    /** Optional, the name of the rule. Only used in the log. */
    val ruleName: String? = null,
    /** The new risk of matching alerts. Mandatory in ZAP; defaults to 'False Positive' here. */
    val newRisk: AlertFilterRisk = AlertFilterRisk.FALSE_POSITIVE,
    /** Optional string to match against the alert url. */
    val url: String? = null,
    /** If true then [url] is a regex. */
    val urlRegex: Boolean = false,
    /** Optional string to match against the alert parameter field. */
    val parameter: String? = null,
    /** If true then [parameter] is a regex. */
    val parameterRegex: Boolean = false,
    /** Optional string to match against the alert attack field. */
    val attack: String? = null,
    /** If true then [attack] is a regex. */
    val attackRegex: Boolean = false,
    /** Optional string to match against the alert evidence field. */
    val evidence: String? = null,
    /** If true then [evidence] is a regex. */
    val evidenceRegex: Boolean = false,
    /** Optional, the HTTP methods the filter applies to. Empty means all. */
    val methods: List<String> = emptyList()
) {
    init {
        require(ruleId.isNotBlank()) { "An alert filter needs a ruleId." }
    }
}
