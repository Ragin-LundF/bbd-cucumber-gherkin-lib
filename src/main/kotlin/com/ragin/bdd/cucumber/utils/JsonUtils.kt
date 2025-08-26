package com.ragin.bdd.cucumber.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.ragin.bdd.cucumber.BddLibConstants
import com.ragin.bdd.cucumber.core.ScenarioStateContext.getJsonPathOptions
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher
import com.ragin.bdd.cucumber.matcher.IBANMatcher
import com.ragin.bdd.cucumber.matcher.NeStringMatcher
import com.ragin.bdd.cucumber.matcher.ScenarioStateEqContextMatcher
import com.ragin.bdd.cucumber.matcher.ScenarioStateNeContextMatcher
import com.ragin.bdd.cucumber.matcher.UUIDMatcher
import com.ragin.bdd.cucumber.matcher.ValidDateContextMatcher
import com.ragin.bdd.cucumber.matcher.ValidDateMatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matcher
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * Utility class used to work with JSON objects.
 */
@Component
class JsonUtils(
    private val jsonMatcher: Collection<BddCucumberJsonMatcher>?,
    private val bddCucumberDateTimeFormatter: Collection<BddCucumberDateTimeFormat>
) {
    /**
     * Assert that two JSON Strings are equal
     *
     * @param expectedJSON expected JSON as String
     * @param actualJSON   actual JSON as String
     */
    fun assertJsonEquals(expectedJSON: String?, actualJSON: String?) {
        try {
            val configuration = createJsonAssertConfiguration()
            JsonAssert.assertJsonEquals(
                expectedJSON,
                actualJSON,
                configuration
            )
        } catch (error: AssertionError) {
            val minimizedExpected = minimizeJSON(json = expectedJSON)
            val minimizedActual = minimizeJSON(json = actualJSON)
            log.error {
            """
            JSON comparison failed.
            Expected:
                $minimizedExpected

            Actual:
                $minimizedActual
            """.trimIndent()
            }
            throw error
        }
    }

    /**
     * Configure JSON Assert
     *
     * @return Configuration class with default configuration
     */
    private fun createJsonAssertConfiguration(): Configuration {
        // base configuration
        var configuration = JsonAssert.withTolerance(0.0)
            .`when`(
                Option.TREATING_NULL_AS_ABSENT
            )
            .withMatcher(
                "isValidDate", ValidDateMatcher(
                    dateTimeFormatCollection = bddCucumberDateTimeFormatter
                )
            ).withMatcher(
                "isDateOfContext", ValidDateContextMatcher(
                    dateTimeFormatCollection = bddCucumberDateTimeFormatter
                )
            ).withMatcher("isValidUUID", UUIDMatcher())
            .withMatcher("isValidIBAN", IBANMatcher())
            .withMatcher("isNotEqualTo", NeStringMatcher())
            .withMatcher("isEqualToScenarioContext", ScenarioStateEqContextMatcher())
            .withMatcher("isNotEqualToScenarioContext", ScenarioStateNeContextMatcher())

        // add additional options
        for (jsonOption in getJsonPathOptions()) {
            configuration = configuration.`when`(jsonOption)
        }

        // add additional matcher
        if (!jsonMatcher.isNullOrEmpty()) {
            for (matcher in jsonMatcher) {
                configuration = addMatcherConfiguration(configuration = configuration, matcher = matcher)
            }
        }
        return configuration
    }

    /**
     * Add more matcher to existing configuration.
     *
     * @param configuration Configuration
     * @param matcher       Class implementation of BddCucumberJsonMatcher interface
     * @return              Configuration with added matcher
     */
    private fun addMatcherConfiguration(configuration: Configuration, matcher: BddCucumberJsonMatcher): Configuration {
        var configurationVar = configuration
        try {
            configurationVar = configurationVar.withMatcher(
                matcher.matcherName(),
                matcher.matcherClass().getDeclaredConstructor().newInstance()
            )
        } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") _: Exception) {
            log.error { "Unable to instantiate the matcher [${matcher.matcherName()}]" }
        }
        return configurationVar
    }

    /**
     * Remove an element from a JSON file.
     *
     * @param originalJson the JSON file in which the field should be removed
     * @param fieldPath    the JSON path to the field that should be removed
     * @return the JSON file as String without the field
     */
    fun removeJsonField(originalJson: String?, fieldPath: String): String {
        return editJsonField(
            originalJson = originalJson,
            fieldPath = fieldPath,
            newValue = null
        )
    }

    /**
     * Edit an element from a JSON file.
     *
     * @param originalJson the JSON file in which the field should be edited
     * @param fieldPath    the field path to the field that should be edited
     * @param newValue     the new value for the field
     * @return the JSON file with the edited field
     */
    fun editJsonField(originalJson: String?, fieldPath: String, newValue: String?): String {
        // set a JSON path
        var fieldJSONPath = fieldPath
        if (!fieldJSONPath.startsWith("$.")) {
            fieldJSONPath = "$.$fieldJSONPath"
        }

        // read document
        val documentContext = JsonPath.parse(originalJson)
        documentContext.set(fieldJSONPath, newValue)
        return documentContext.jsonString()
    }

    /**
     * Validate an element from a JSON file.
     *
     * @param originalJson  the JSON file in which the field should be edited
     * @param fieldPath     the field path to the field that should be edited
     * @param expectedValue the new value for the field
     */
    fun validateJsonField(originalJson: String?, fieldPath: String, expectedValue: String) {
        // set a JSON path
        var fieldJSONPath = fieldPath
        if (!fieldJSONPath.startsWith("$.")) {
            fieldJSONPath = "$.$fieldJSONPath"
        }

        // try to resolve value from context. If nothing was found, reset to original
        var expectedValueToCompare = scenarioContextMap[expectedValue]
        if (expectedValueToCompare == null) {
            expectedValueToCompare = expectedValue
        }

        // read document
        val documentContext = JsonPath.parse(originalJson)
        var fieldValue: Any? = null
        try {
            fieldValue = documentContext.read(fieldJSONPath, Any::class.java)
            assertFieldValidation(
                expectedValueToCompare = expectedValueToCompare,
                fieldValue = fieldValue.toString()
            )
        } catch (pnfe: PathNotFoundException) {
            if (BddLibConstants.BDD_LIB_NOT_EXISTS.equals(expectedValueToCompare, ignoreCase = true)) {
                assertThat(fieldValue).isNull()
            } else {
                throw pnfe
            }
        }
    }

    private fun assertFieldValidation(expectedValueToCompare: String, fieldValue: String) {
        val assertConfig = createJsonAssertConfiguration()
        var matcher: Optional<Matcher<*>> = Optional.empty()
        val matcherName = findMatcherName(
            possibleMatcherName = expectedValueToCompare
        )
        if (matcherName.isPresent) {
            matcher = Optional.ofNullable(assertConfig.getMatcher(matcherName.get()))
        }
        if (expectedValueToCompare.startsWith(BddLibConstants.BDD_LIB_NOT)) {
            if (matcher.isPresent) {
                val fieldMatches = matcher.get().matches(fieldValue)
                assertThat(fieldMatches).isFalse
            } else {
                assertThat(fieldValue)
                    .isNotEqualTo(expectedValueToCompare.substring(BddLibConstants.BDD_LIB_NOT.length))
            }
        } else {
            if (matcher.isPresent) {
                val fieldMatches = matcher.get().matches(fieldValue)
                assertThat(fieldMatches).isTrue
            } else {
                assertThat(fieldValue).isEqualTo(expectedValueToCompare)
            }
        }
    }

    /**
     * Find custom matcher
     *
     * @param possibleMatcherName   string which possibly contains a matcher name
     * @return  if matcher name was found the matcher name
     */
    @Suppress("MagicNumber")
    private fun findMatcherName(possibleMatcherName: String): Optional<String> {
        val matcher = BddLibConstants.BDD_LIB_MATCHER_PATTERN.matcher(possibleMatcherName)
        return if (matcher.find() && matcher.groupCount() == 3) {
            Optional.of(matcher.group(2))
        } else {
            Optional.empty()
        }
    }

    /**
     * Minimize JSON object to be better comparable.
     *
     * Actions:
     *
     *  * Replace '\r' and '\n' with ''
     *  * Replace '": ' with '":' (space between key/value)
     *  * Trim everything
     *
     *
     * @param json JSON string
     * @return minimized JSON string
     */
    private fun minimizeJSON(json: String?): String {
        val nullSafeJson = json ?: EMPTY_JSON

        return nullSafeJson
            .split(NEW_LINE)
            .asSequence()
            .map { it.replace(CARRIAGE_RETURN, "") }
            .map { it.replace(JSON_SPACING, JSON_COMPACT) }
            .map { it.trim() }
            .joinToString("")
    }

    companion object {
        private val log = KotlinLogging.logger { }
        const val EMPTY_JSON = "{}"
        const val NEW_LINE = "\n"
        const val CARRIAGE_RETURN = "\r"
        const val JSON_SPACING = "\": "
        const val JSON_COMPACT = "\":"

        val mapper: ObjectMapper = Jackson2ObjectMapperBuilder()
            .modules(JavaTimeModule(), KotlinModule.Builder().build())
            .featuresToDisable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
            )
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build()
    }
}
