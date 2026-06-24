package com.ragin.bdd.cucumber.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IBANMatcherTests {

    private val matcher = IBANMatcher()

    @Test
    internal fun `valid German IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "DE89370400440532013000"))
    }

    @Test
    internal fun `valid Austrian IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "AT611904300234573201"))
    }

    @Test
    internal fun `valid GB IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "GB29NWBK60161331926819"))
    }

    @Test
    internal fun `valid Dutch IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "NL91ABNA0417164300"))
    }

    @Test
    internal fun `random string does not match`() {
        assertFalse(actual = matcher.matches(actual = "NOTANIBAN"))
    }

    @Test
    internal fun `empty string does not match`() {
        assertFalse(actual = matcher.matches(actual = ""))
    }

    @Test
    internal fun `non-string type does not match`() {
        assertFalse(actual = matcher.matches(actual = 12345))
    }

    @Test
    internal fun `IBAN with wrong digit count does not match`() {
        assertFalse(actual = matcher.matches(actual = "DE8937040044053201300"))
    }

    @Test
    internal fun `matcherName returns isValidIBAN`() {
        assertEquals(expected = "isValidIBAN", actual = matcher.matcherName())
    }
}
