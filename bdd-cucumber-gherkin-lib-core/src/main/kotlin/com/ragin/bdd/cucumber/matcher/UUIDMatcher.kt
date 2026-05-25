package com.ragin.bdd.cucumber.matcher

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Matcher for valid date.
 *
 * ${json-unit.matches:isValidUUID}
 */
@Component
class UUIDMatcher : BaseMatcher<Any>(), BddCucumberJsonMatcher {
    override fun matches(item: Any): Boolean {
        return runCatching {
            UUID.fromString(item.toString())
        }.fold(
            onSuccess = { true },
            onFailure = { false }
        )
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("Is not a valid UUID: ").appendValue(item)
    }

    override fun describeTo(description: Description) {
        // nothing to describe here
    }

    override fun matcherName(): String {
        return "isValidUUID"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>> {
        return this::class.java
    }
}
