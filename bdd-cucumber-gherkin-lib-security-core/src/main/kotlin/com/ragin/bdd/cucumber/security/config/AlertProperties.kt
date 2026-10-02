package com.ragin.bdd.cucumber.security.config

import com.ragin.bdd.cucumber.security.models.SecurityRisk

/**
 * Which findings are relevant at all.
 *
 * The risk that fails the build is not configured here: it is part of the Gherkin sentence that
 * runs the gate, so a feature file states its own threshold and nothing can silently lower it.
 */
data class AlertProperties @JvmOverloads constructor(
    /**
     * Scanner rule ids to ignore, e.g. ZAP 40042 = Spring Actuator Information Leak.
     *
     * Applied to both the gate and the report: the gate drops the findings, and the scanner is
     * told to leave them out of the report it writes.
     */
    val ignoredRuleIds: Set<String> = emptySet(),
    /** Findings below this confidence are ignored. */
    val minConfidence: SecurityRisk = SecurityRisk.LOW,
    /**
     * Scanner alert filters, applied to every alert raised after the scanner started.
     *
     * Unlike [ignoredRuleIds] they act inside the scanner: the gate sees the changed risk because
     * it reads the alerts back. A 'False Positive' alert carries the lowest confidence, so the gate
     * drops it as long as [minConfidence] is above [SecurityRisk.INFORMATIONAL].
     */
    val alertFilter: List<AlertFilterProperties> = emptyList()
)
