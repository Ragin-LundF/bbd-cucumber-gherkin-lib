package com.ragin.bdd.cucumber.matcher

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * Is not equal to string.
 *
 * ${json-unit.matches:isNotEqualTo}MY_CONTEXT_VALUE
 */
@Component
class NeStringMatcher : BaseMatcher<Any>(), ParametrizedMatcher {
    private var parameter: String? = null

    override fun matches(actual: Any): Boolean {
        val actualAsString = actual.toString()
        return actualAsString != parameter
    }

    override fun describeTo(description: Description) {
        description.appendText("The actual value is equal to the parameter [$parameter]")
    }

    override fun describeMismatch(item: Any, description: Description) {
        description
                .appendText("The value was [")
                .appendValue(ScenarioStateContext.scenarioContextMap[parameter])
                .appendText("].")
                .appendText(" JSON Value was [")
                .appendValue(item)
                .appendText("].")
    }

    override fun setParameter(parameter: String) {
        this.parameter = parameter
    }
}
