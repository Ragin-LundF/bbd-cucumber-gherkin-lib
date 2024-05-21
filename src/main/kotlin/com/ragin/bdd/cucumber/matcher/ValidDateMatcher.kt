package com.ragin.bdd.cucumber.matcher

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import com.ragin.bdd.cucumber.utils.DateUtils
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * Matcher for valid date.
 *
 * ${json-unit.matches:isValidDate}
 */
@Component
class ValidDateMatcher(
    private val dateTimeFormatCollection: Collection<BddCucumberDateTimeFormat>
) : BaseMatcher<Any>() {
    override fun matches(item: Any): Boolean {
        return DateUtils.isValidMandatoryDate(item, dateTimeFormatCollection)
    }

    override fun describeMismatch(item: Any, description: Description) {
        description.appendText("Is not a valid date: ").appendValue(item)
    }

    override fun describeTo(description: Description) {
        // nothing to describe here
    }
}
