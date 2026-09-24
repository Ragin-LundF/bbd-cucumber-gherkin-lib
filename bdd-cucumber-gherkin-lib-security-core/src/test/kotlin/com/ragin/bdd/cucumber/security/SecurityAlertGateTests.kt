package com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.config.AlertProperties
import com.ragin.bdd.cucumber.security.models.SecurityAlert
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The gate is the only part of the security scan that decides whether a build passes, so it is
 * covered here rather than only by observing a real scan.
 */
internal class SecurityAlertGateTests {
    @Test
    internal fun `passes when every finding stays below the threshold`() {
        val gate = SecurityAlertGate(properties = AlertProperties())
        val alerts = listOf(alert(risk = SecurityRisk.LOW), alert(risk = SecurityRisk.INFORMATIONAL))

        gate.verify(alerts = alerts, failFrom = SecurityRisk.MEDIUM)
    }

    @Test
    internal fun `fails on a finding of exactly the threshold risk`() {
        val gate = SecurityAlertGate(properties = AlertProperties())
        val alerts = listOf(alert(risk = SecurityRisk.MEDIUM, name = "Spring Actuator Information Leak"))

        val failure = assertFailsWith<IllegalStateException> {
            gate.verify(alerts = alerts, failFrom = SecurityRisk.MEDIUM)
        }

        assertTrue(actual = failure.message!!.contains(other = "Spring Actuator Information Leak"))
        assertTrue(actual = failure.message!!.contains(other = "risk MEDIUM or higher"))
    }

    @Test
    internal fun `fails on a finding above the threshold`() {
        val gate = SecurityAlertGate(properties = AlertProperties())
        val alerts = listOf(alert(risk = SecurityRisk.HIGH))

        assertFailsWith<IllegalStateException> { gate.verify(alerts = alerts, failFrom = SecurityRisk.MEDIUM) }
    }

    @Test
    internal fun `drops findings whose rule id is on the ignore list`() {
        val gate = SecurityAlertGate(properties = AlertProperties(ignoredRuleIds = setOf("40042")))
        val alerts = listOf(alert(risk = SecurityRisk.MEDIUM, ruleId = "40042"))

        assertEquals(expected = emptyList(), actual = gate.relevant(alerts = alerts))
    }

    @Test
    internal fun `drops findings below the configured confidence`() {
        val gate = SecurityAlertGate(properties = AlertProperties(minConfidence = SecurityRisk.MEDIUM))
        val alerts = listOf(alert(risk = SecurityRisk.HIGH, confidence = SecurityRisk.LOW))

        assertEquals(expected = emptyList(), actual = gate.relevant(alerts = alerts))
    }

    @Test
    internal fun `drops findings the scanner marked as false positive`() {
        val gate = SecurityAlertGate(properties = AlertProperties())
        val alerts = listOf(
            alert(risk = SecurityRisk.MEDIUM, confidence = SecurityRisk.fromLabel(value = "False Positive"))
        )

        assertEquals(expected = emptyList(), actual = gate.relevant(alerts = alerts))
    }

    @Test
    internal fun `reports an identical finding only once`() {
        val gate = SecurityAlertGate(properties = AlertProperties())
        val duplicate = alert(risk = SecurityRisk.LOW)

        assertEquals(expected = listOf(duplicate), actual = gate.relevant(alerts = listOf(duplicate, duplicate)))
    }

    private fun alert(
        risk: SecurityRisk,
        confidence: SecurityRisk = SecurityRisk.HIGH,
        ruleId: String = "10000",
        name: String = "some finding"
    ): SecurityAlert {
        return SecurityAlert(
            ruleId = ruleId,
            name = name,
            risk = risk,
            confidence = confidence,
            url = "http://host.testcontainers.internal:50000/api/v1/labelling",
            method = "POST",
            parameter = ""
        )
    }
}
