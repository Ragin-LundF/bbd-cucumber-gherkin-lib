package com.ragin.bdd.cucumbertests.hooks

import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher
import com.ragin.bdd.cucumber.utils.JacksonUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.jupiter.api.assertNotNull

class ParameterizedCustomMultiParameterMatcher : BaseMatcher<Any>(), BddCucumberJsonMatcher, ParametrizedMatcher {
    private var jsonParameter: JsonParameter? = null

    override fun matcherName(): String {
        return "containsOneOf"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>?> {
        return this.javaClass
    }

    override fun setParameter(parameter: String?) {
        assertNotNull(
            actual = parameter,
            message = "The parameter {$parameter} of 'containsOneOf' matcher cannot be null."
        )
        runCatching {
            this.jsonParameter = JacksonUtils.mapper.readValue(parameter, JsonParameter::class.java)
        }.onFailure {
            log.error { "Parameter not parsable: $parameter" }
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

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
