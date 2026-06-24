package com.ragin.bdd.cucumber.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class UUIDMatcherTests {

    private val uuidMatcher = UUIDMatcher()

    @Test
    internal fun `valid UUID matches`() {
        assertTrue(actual = uuidMatcher.matches(item = Uuid.random().toString()))
    }

    @Test
    internal fun `nil UUID matches`() {
        assertTrue(actual = uuidMatcher.matches(item = Uuid.NIL))
    }

    @Test
    internal fun `non-UUID string does not match`() {
        assertFalse(actual = uuidMatcher.matches(item = "not-a-uuid"))
    }

    @Test
    internal fun `empty string does not match`() {
        assertFalse(actual = uuidMatcher.matches(item = ""))
    }

    @Test
    internal fun `UUID without hyphens does not match`() {
        assertFalse(actual = uuidMatcher.matches(
            item = Uuid.random().toString().replace(oldValue = "-", newValue = ""))
        )
    }

    @Test
    internal fun `matcherName returns isValidUUID`() {
        assertEquals(expected = "isValidUUID", actual = uuidMatcher.matcherName())
    }
}
