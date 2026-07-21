package com.ragin.bdd.cucumber.matcher

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * String contains not.
 *
 * ${json-unit.matches:string-contains-not}MY_CONTEXT_VALUE
 */
@Component
class StringContainsNotMatcher : BaseMatcher<Any>(), ParametrizedMatcher, BddCucumberJsonMatcher {
    private var parameter: String? = null

    override fun matches(actual: Any): Boolean {
        val actualAsString = ScenarioStateContext.resolveEntry(key = actual.toString())
        return parameter?.let {
            !actualAsString.contains(other = it, ignoreCase = true)
        } ?: false
    }

    override fun describeTo(description: Description) {
        description.appendText("The actual value does not contain the parameter [$parameter]")
    }

    override fun describeMismatch(item: Any, description: Description) {
        description
            .appendText("Parameter was [")
            .appendValue(parameter)
            .appendText("].")
            .appendText("BDD Context value was [")
            .appendValue(ScenarioStateContext.scenarioContextMap[parameter])
            .appendText("].")
            .appendText(" JSON Value was [")
            .appendValue(item)
            .appendText("].")
    }

    override fun setParameter(parameter: String?) {
        this.parameter = parameter
    }

    override fun matcherName(): String {
        return "string-contains-not"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>> {
        return this::class.java
    }
}
