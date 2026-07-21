package com.ragin.bdd.cucumber.matcher

import org.hamcrest.StringDescription
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class StringContainsNotMatcherTests {

    private val matcher = StringContainsNotMatcher()

    @Test
    internal fun `actual not containing parameter matches`() {
        matcher.setParameter("qux")
        assertTrue(actual = matcher.matches(actual = "foobarbaz"))
    }

    @Test
    internal fun `actual containing parameter does not match`() {
        matcher.setParameter("bar")
        assertFalse(actual = matcher.matches(actual = "foobarbaz"))
    }

    @Test
    internal fun `containment check is case insensitive`() {
        matcher.setParameter("BAR")
        assertFalse(actual = matcher.matches(actual = "foobarbaz"))
    }

    @Test
    internal fun `non-string actual is converted to string for comparison`() {
        matcher.setParameter("234")
        assertFalse(actual = matcher.matches(actual = 12345))
    }

    @Test
    internal fun `null parameter never matches`() {
        // parameter not set, defaults to null
        assertFalse(actual = matcher.matches(actual = "anything"))
    }

    @Test
    internal fun `matcherName returns string-contains-not`() {
        assertEquals(expected = "string-contains-not", actual = matcher.matcherName())
    }

    @Test
    internal fun `matcherClass returns the matcher type`() {
        assertEquals(expected = StringContainsNotMatcher::class.java, actual = matcher.matcherClass())
    }

    @Test
    internal fun `describeTo mentions the parameter`() {
        matcher.setParameter("bar")
        val description = StringDescription()
        matcher.describeTo(description)
        assertEquals(
            expected = "The actual value does not contain the parameter [bar]",
            actual = description.toString(),
        )
    }

    @Test
    internal fun `describeMismatch includes parameter and item`() {
        matcher.setParameter("bar")
        val description = StringDescription()
        matcher.describeMismatch("foobar", description)
        assertTrue(actual = description.toString().contains("Parameter was [\"bar\"]"))
        assertTrue(actual = description.toString().contains("JSON Value was [\"foobar\"]"))
    }
}
