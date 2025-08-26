package com.ragin.bdd.cucumber.matcher

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import com.ragin.bdd.cucumber.utils.DateUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.jsonunit.core.ParametrizedMatcher
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

/**
 * Valid date from context matcher.
 *
 * ${json-unit.matches:isDateOfContext}3_DAYS_IN_PAST
 */
@Component
class ValidDateContextMatcher(
    private val dateTimeFormatCollection: Collection<BddCucumberDateTimeFormat>
) : BaseMatcher<Any>(), ParametrizedMatcher {
    private var parameter: String? = null

    override fun matches(actual: Any): Boolean {
        val parameterFromContext = ScenarioStateContext.scenarioContextMap[parameter]
        val jsonDate = DateUtils.transformToLocalDateTime(
            dateObject = actual,
            bddDateTimeFormats = dateTimeFormatCollection
        )

        log.info { "Compare actual '$actual' with parameter 'parameter' as '$parameterFromContext'" }
        if (jsonDate != null) {
            return jsonDate.toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE) == parameterFromContext
        }
        return false
    }

    override fun describeTo(description: Description) {
        description.appendText("The actual date is not equal to the parameter [$parameter]")
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

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
