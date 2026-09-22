package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.config.AlertProperties
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Turns raw findings into a verdict: which of them are relevant, and do they fail the build.
 *
 * Kept apart from [SecurityScan] because this is the only part of the scan that decides whether
 * a build passes; it must stay readable and testable without any scanner around it.
 */
class SecurityAlertGate(private val properties: AlertProperties) {
    /** Findings that survive the ignore list and the confidence threshold. */
    fun relevant(alerts: List<SecurityAlert>): List<SecurityAlert> {
        return alerts
            .distinct()
            .filterNot { alert -> alert.ruleId in properties.ignoredRuleIds }
            .filter { alert -> alert.confidence.level >= properties.minConfidence.level }
    }

    /** Fails with a readable summary if any of the [alerts] reaches [failFrom]. */
    fun verify(alerts: List<SecurityAlert>, failFrom: SecurityRisk) {
        val blocking = alerts.filter { alert -> alert.risk.level >= failFrom.level }

        log.info { "${alerts.size} relevant finding(s), ${blocking.size} of risk $failFrom or higher" }
        alerts.forEach { alert -> log.info { "  ${alert.describe()}" } }

        check(blocking.isEmpty()) {
            buildString {
                append("The security scan found ${blocking.size} finding(s) of risk $failFrom or higher:")
                blocking.forEach { alert -> append("\n  - ${alert.describe()}") }
            }
        }
    }

    private companion object {
        val log = KotlinLogging.logger {}
    }
}
