package com.ragin.bdd.cucumbertests.hooks

import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class SimpleCustomUUIDMatcher : BaseMatcher<String>(), BddCucumberJsonMatcher {
    override fun matcherName(): String {
        return "isUUID"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>?> {
        return this.javaClass
    }

    override fun matches(actual: Any): Boolean {
        if (actual is String) {
            return actual
                .matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".toRegex())
        }
        return false
    }

    override fun describeTo(description: Description) = Unit
}
