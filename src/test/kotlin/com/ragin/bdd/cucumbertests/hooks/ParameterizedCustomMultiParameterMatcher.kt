package com.ragin.bdd.cucumbertests.hooks

import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher
import com.ragin.bdd.cucumber.utils.JsonUtils
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class ParameterizedCustomMultiParameterMatcher : BaseMatcher<Any>(), BddCucumberJsonMatcher, ParametrizedMatcher {
    private var jsonParameter: JsonParameter? = null

    override fun matcherName(): String {
        return "containsOneOf"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>?> {
        return this.javaClass
    }

    override fun setParameter(parameter: String) {
        try {
            val objectMapper = JsonUtils.mapper
            this.jsonParameter = objectMapper.readValue(parameter, JsonParameter::class.java)
        } catch (_: Exception) {
        }
    }

    override fun matches(actual: Any): Boolean {
        val actualParameter = actual.toString()
        return (isFirstValid(actualParameter) || isSecondValid(actualParameter)) &&
                !(isFirstValid(actualParameter) && isSecondValid(actualParameter))
    }

    override fun describeTo(description: Description) = Unit

    private fun isFirstValid(actualParameter: String): Boolean {
        return actualParameter.equals(jsonParameter!!.firstArgument, ignoreCase = true)
    }

    private fun isSecondValid(actualParameter: String): Boolean {
        return actualParameter.equals(jsonParameter!!.secondArgument, ignoreCase = true)
    }

    /**
     * Inner class for showcase
     */
    internal class JsonParameter {
        var firstArgument: String? = null
        var secondArgument: String? = null
    }
}
