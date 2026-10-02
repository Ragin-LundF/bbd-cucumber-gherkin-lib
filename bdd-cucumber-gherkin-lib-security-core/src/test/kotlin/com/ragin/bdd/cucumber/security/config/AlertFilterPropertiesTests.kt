package com.ragin.bdd.cucumber.security.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

internal class AlertFilterPropertiesTests {
    @Test
    internal fun `an alert filter without rule id is rejected`() {
        assertFailsWith<IllegalArgumentException> { AlertFilterProperties(ruleId = " ") }
    }

    @Test
    internal fun `an alert filter with only a rule id marks the whole rule as false positive`() {
        val filter = AlertFilterProperties(ruleId = "40042")

        assertEquals(expected = AlertFilterRisk.FALSE_POSITIVE, actual = filter.newRisk)
        assertEquals(expected = null, actual = filter.url)
        assertFalse(actual = filter.urlRegex)
        assertFalse(actual = filter.parameterRegex)
        assertFalse(actual = filter.attackRegex)
        assertFalse(actual = filter.evidenceRegex)
        assertEquals(expected = emptyList(), actual = filter.methods)
    }
}
