package com.ragin.bdd.cucumber.security.config

import com.ragin.bdd.cucumber.security.models.SecurityRisk

/**
 * Which findings are relevant at all.
 *
 * The risk that fails the build is not configured here: it is part of the Gherkin sentence that
 * runs the gate, so a feature file states its own threshold and nothing can silently lower it.
 */
data class AlertProperties @JvmOverloads constructor(
    /** Scanner rule ids to ignore, e.g. ZAP 40042 = Spring Actuator Information Leak. */
    val ignoredRuleIds: Set<String> = emptySet(),
    /** Findings below this confidence are ignored. */
    val minConfidence: SecurityRisk = SecurityRisk.LOW
)
