package com.ragin.bdd.cucumber.security.models

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SecurityRiskTests {
    @Test
    internal fun `parses the severities a scanner reports`() {
        assertEquals(expected = SecurityRisk.INFORMATIONAL, actual = SecurityRisk.fromLabel(value = "Informational"))
        assertEquals(expected = SecurityRisk.LOW, actual = SecurityRisk.fromLabel(value = "Low"))
        assertEquals(expected = SecurityRisk.MEDIUM, actual = SecurityRisk.fromLabel(value = "Medium"))
        assertEquals(expected = SecurityRisk.HIGH, actual = SecurityRisk.fromLabel(value = "High"))
    }

    @Test
    internal fun `maps the confidence-only values onto the closest risk`() {
        assertEquals(expected = SecurityRisk.INFORMATIONAL, actual = SecurityRisk.fromLabel(value = "False Positive"))
        assertEquals(expected = SecurityRisk.HIGH, actual = SecurityRisk.fromLabel(value = "User Confirmed"))
    }

    @Test
    internal fun `falls back to informational for unknown or missing values`() {
        assertEquals(expected = SecurityRisk.INFORMATIONAL, actual = SecurityRisk.fromLabel(value = null))
        assertEquals(expected = SecurityRisk.INFORMATIONAL, actual = SecurityRisk.fromLabel(value = ""))
        assertEquals(expected = SecurityRisk.INFORMATIONAL, actual = SecurityRisk.fromLabel(value = "something new"))
    }

    @Test
    internal fun `orders the levels so that a threshold comparison works`() {
        assertEquals(
            expected = listOf(SecurityRisk.INFORMATIONAL, SecurityRisk.LOW, SecurityRisk.MEDIUM, SecurityRisk.HIGH),
            actual = SecurityRisk.entries.sortedBy { risk -> risk.level }
        )
    }
}
