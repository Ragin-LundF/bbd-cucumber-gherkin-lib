package com.ragin.bdd.cucumber.matcher

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * Is equal to scenario context.
 *
 * ${json-unit.matches:isEqualToScenarioContext}newUserNameInContext
 */
@Component
class ScenarioStateEqContextMatcher : BaseMatcher<Any>(), ParametrizedMatcher {
    private var parameter: String? = null

    override fun matches(actual: Any): Boolean {
        val parameterFromContext = ScenarioStateContext.scenarioContextMap[parameter]
        val actualAsString = actual.toString()
        return actualAsString == parameterFromContext
    }

    override fun describeTo(description: Description) {
        description.appendText("The actual value is not equal to the parameter [$parameter]")
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

    override fun setParameter(parameter: String) {
        this.parameter = parameter
    }
}
