package com.ragin.bdd.cucumber.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ValueObfuscatorTests {

    @Test
    internal fun `a value divisible by three keeps the outer thirds and masks the middle third`() {
        assertEquals(expected = "abc***ghi", actual = ValueObfuscator.obfuscate(value = "abcdefghi"))
    }

    @Test
    internal fun `the masked part absorbs the remainder when the length is not divisible by three`() {
        assertEquals(expected = "abc****hij", actual = ValueObfuscator.obfuscate(value = "abcdefghij"))
        assertEquals(expected = "abc*****ijk", actual = ValueObfuscator.obfuscate(value = "abcdefghijk"))
    }

    @Test
    internal fun `a value shorter than three characters is masked completely`() {
        assertEquals(expected = "*", actual = ValueObfuscator.obfuscate(value = "a"))
        assertEquals(expected = "**", actual = ValueObfuscator.obfuscate(value = "ab"))
    }

    @Test
    internal fun `an empty value stays empty`() {
        assertEquals(expected = "", actual = ValueObfuscator.obfuscate(value = ""))
    }

    @Test
    internal fun `the obfuscated value always has the same length as the input`() {
        for (length in 0..64) {
            val value = "x".repeat(n = length)
            assertEquals(
                expected = length,
                actual = ValueObfuscator.obfuscate(value = value).length,
                message = "length $length"
            )
        }
    }

    @Test
    internal fun `at least a third of every value is masked`() {
        for (length in 1..64) {
            val obfuscated = ValueObfuscator.obfuscate(value = "x".repeat(n = length))
            val masked = obfuscated.count { character -> character == '*' }
            assertTrue(
                actual = masked >= length / 3,
                message = "length $length masked only $masked characters"
            )
        }
    }

    @Test
    internal fun `a bearer token inside a JSON payload is obfuscated`() {
        val payload = """{"Authorization":"Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiZGVtbyJ9.c2lnbmF0dXJl"}"""

        val obfuscated = ValueObfuscator.obfuscateSecretsIn(text = payload)

        assertFalse(actual = obfuscated.contains(other = "eyJ1c2VyIjoiZGVtbyJ9"))
        assertTrue(actual = obfuscated.contains(other = "*"))
        assertEquals(expected = payload.length, actual = obfuscated.length)
        assertTrue(actual = obfuscated.startsWith(prefix = """{"Authorization":"Bearer """))
        assertTrue(actual = obfuscated.endsWith(suffix = """"}"""))
    }

    @Test
    internal fun `a basic authentication header is obfuscated`() {
        val obfuscated = ValueObfuscator.obfuscateSecretsIn(text = "Authorization: Basic dXNlcjpwYXNzd29yZA==")

        assertFalse(actual = obfuscated.contains(other = "dXNlcjpwYXNzd29yZA=="))
        assertTrue(actual = obfuscated.startsWith(prefix = "Authorization: Basic "))
    }

    @Test
    internal fun `text without a credential is returned unchanged`() {
        val payload = """{"name":"John Doe","id":"abc-def-ghi"}"""

        assertEquals(expected = payload, actual = ValueObfuscator.obfuscateSecretsIn(text = payload))
    }

    @Test
    internal fun `every credential in a payload is obfuscated, not only the first`() {
        val payload = "Bearer aaaaaaaaaaaaaaaaaaaa and Bearer bbbbbbbbbbbbbbbbbbbb"

        val obfuscated = ValueObfuscator.obfuscateSecretsIn(text = payload)

        assertFalse(actual = obfuscated.contains(other = "aaaaaaaaaaaaaaaaaaaa"))
        assertFalse(actual = obfuscated.contains(other = "bbbbbbbbbbbbbbbbbbbb"))
    }

    @Test
    internal fun `a bearer token is no longer usable but stays recognisable`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoiZGVtbyJ9.mFmI0-1jcQib1P9XJK6W1yIu7bQ"

        val obfuscated = ValueObfuscator.obfuscate(value = token)

        assertFalse(actual = obfuscated.contains(other = token))
        assertTrue(actual = obfuscated.startsWith(prefix = "eyJhbGciOiJIUzI1NiIsI"))
        assertTrue(actual = obfuscated.contains(other = "*"))
    }
}
