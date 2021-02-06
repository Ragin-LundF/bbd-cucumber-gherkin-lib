package com.ragin.bdd.cucumber.matcher

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * Matcher for valid date
 */
@Component
class UUIDMatcher : BaseMatcher<Any>() {
    override fun matches(item: Any): Boolean {
        if (item is String) {
            val actualString = item.toString()
            return actualString.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".toRegex())
        }
        return false
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("Is not a valid UUID: ").appendValue(item)
    }

    override fun describeTo(description: Description) {
        // nothing to describe here
    }
}