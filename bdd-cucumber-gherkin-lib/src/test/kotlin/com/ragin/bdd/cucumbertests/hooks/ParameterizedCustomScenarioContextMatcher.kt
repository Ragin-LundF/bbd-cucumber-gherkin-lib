package com.ragin.bdd.cucumbertests.hooks

import com.ragin.bdd.cucumber.core.ScenarioStateContext.current
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import kotlin.test.assertNotNull

class ParameterizedCustomScenarioContextMatcher : BaseMatcher<Any>(), BddCucumberJsonMatcher, ParametrizedMatcher {
    private var jsonParameter: String? = null

    override fun matcherName(): String {
        return "isInContextAvailable"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>?> {
        return this.javaClass
    }

    override fun setParameter(parameter: String?) {
        assertNotNull(
            actual = parameter,
            message = "The parameter {$parameter} of 'isInContextAvailable' matcher cannot be null."
        )
        this.jsonParameter = parameter
    }

    override fun matches(actual: Any): Boolean {
        val actualParameter = actual.toString()
        val contextParameter = current()
            .scenarioContextMap[jsonParameter]
        return actualParameter == contextParameter
    }

    override fun describeTo(description: Description) = Unit
}
