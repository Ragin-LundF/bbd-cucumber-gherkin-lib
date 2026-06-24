package com.ragin.bdd.cucumber.matcher

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NeStringMatcherTests {

    private val neMatcher = NeStringMatcher()

    @Test
    internal fun `actual different from parameter matches`() {
        neMatcher.setParameter("expected")
        assertTrue(actual = neMatcher.matches(actual = "other"))
    }

    @Test
    internal fun `actual equal to parameter does not match`() {
        neMatcher.setParameter("expected")
        assertFalse(actual = neMatcher.matches(actual = "expected"))
    }

    @Test
    internal fun `non-string actual is converted to string for comparison`() {
        neMatcher.setParameter("42")
        assertFalse(actual = neMatcher.matches(actual = 42))
    }

    @Test
    internal fun `null parameter with any non-null actual always matches`() {
        // parameter not set, defaults to null — any actual.toString() != null
        assertTrue(actual = neMatcher.matches(actual = "anything"))
    }
}
