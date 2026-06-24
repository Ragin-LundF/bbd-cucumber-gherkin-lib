package com.ragin.bdd.cucumber.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IBANMatcherTest {

    private val matcher = IBANMatcher()

    @Test
    fun `valid German IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "DE89370400440532013000"))
    }

    @Test
    fun `valid Austrian IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "AT611904300234573201"))
    }

    @Test
    fun `valid GB IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "GB29NWBK60161331926819"))
    }

    @Test
    fun `valid Dutch IBAN matches`() {
        assertTrue(actual = matcher.matches(actual = "NL91ABNA0417164300"))
    }

    @Test
    fun `random string does not match`() {
        assertFalse(actual = matcher.matches(actual = "NOTANIBAN"))
    }

    @Test
    fun `empty string does not match`() {
        assertFalse(actual = matcher.matches(actual = ""))
    }

    @Test
    fun `non-string type does not match`() {
        assertFalse(actual = matcher.matches(actual = 12345))
    }

    @Test
    fun `IBAN with wrong country code length does not match`() {
        assertFalse(actual = matcher.matches(actual = "DE8937040044053201300"))
    }

    @Test
    fun `matcherName returns isValidIBAN`() {
        assertEquals(expected = "isValidIBAN", actual = matcher.matcherName())
    }
}
