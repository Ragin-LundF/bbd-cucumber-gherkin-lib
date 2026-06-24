package com.ragin.bdd.cucumber.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UUIDMatcherTest {

    private val uuidMatcher = UUIDMatcher()

    @Test
    fun `valid UUID matches`() {
        assertTrue(actual = uuidMatcher.matches(item = Uuid.random().toString()))
    }

    @Test
    fun `nil UUID matches`() {
        assertTrue(actual = uuidMatcher.matches(item = Uuid.NIL))
    }

    @Test
    fun `non-UUID string does not match`() {
        assertFalse(actual = uuidMatcher.matches(item = "not-a-uuid"))
    }

    @Test
    fun `empty string does not match`() {
        assertFalse(actual = uuidMatcher.matches(item = ""))
    }

    @Test
    fun `UUID without hyphens does not match`() {
        assertFalse(actual = uuidMatcher.matches(
            item = Uuid.random().toString().replace(oldValue = "-", newValue = ""))
        )
    }

    @Test
    fun `matcherName returns isValidUUID`() {
        assertEquals(expected = "isValidUUID", actual = uuidMatcher.matcherName())
    }
}
