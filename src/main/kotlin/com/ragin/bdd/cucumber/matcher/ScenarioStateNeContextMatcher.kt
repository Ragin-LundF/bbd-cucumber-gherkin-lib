package com.ragin.bdd.cucumber.matcher

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * Is not equal to scenario context.
 *
 * ${json-unit.matches:isNotEqualToScenarioContext}MY_CONTEXT_VALUE
 */
@Component
class ScenarioStateNeContextMatcher : BaseMatcher<Any>(), ParametrizedMatcher {
    private var parameter: String? = null

    override fun matches(actual: Any): Boolean {
        val parameterFromContext = ScenarioStateContext.scenarioContextMap[parameter]
        val actualAsString = actual.toString()
        return actualAsString != parameterFromContext
    }

    override fun describeTo(description: Description) {
        description.appendText("The actual value is equal to the parameter [$parameter]")
    }

    override fun describeMismatch(item: Any, description: Description) {
        description
                .appendText("BDD Context value was [")
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
