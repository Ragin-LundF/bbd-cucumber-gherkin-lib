package com.ragin.bdd.cucumber.utils

import com.jayway.jsonpath.JsonPath
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
import org.hamcrest.Matcher
import org.junit.jupiter.api.assertNull
import org.springframework.stereotype.Component
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
        runCatching {
            val configuration = createJsonAssertConfiguration()
            JsonAssert.assertJsonEquals(
                expectedJSON,
                actualJSON,
                configuration
            )
        }.onFailure { error ->
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
        val validDateTimeContextMatcher = ValidDateMatcher(
            dateTimeFormatCollection = bddCucumberDateTimeFormatter
        )
        val validDateContextMatcher = ValidDateContextMatcher(
            dateTimeFormatCollection = bddCucumberDateTimeFormatter
        )
        var configuration = JsonAssert.withTolerance(0.0)
            .`when`(
                Option.TREATING_NULL_AS_ABSENT
            )
            .withMatcher(validDateContextMatcher.matcherName(), validDateContextMatcher)
            .withMatcher(validDateTimeContextMatcher.matcherName(), validDateTimeContextMatcher)
            .withMatcher(uuidMatcher.matcherName(), uuidMatcher)
            .withMatcher(ibanMatcher.matcherName(), ibanMatcher)
            .withMatcher(neStringMatcher.matcherName(), neStringMatcher)
            .withMatcher(scenarioStateEqContextMatcher.matcherName(), scenarioStateEqContextMatcher)
            .withMatcher(scenarioStateNeContextMatcher.matcherName(), scenarioStateNeContextMatcher)

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
        runCatching {
            configurationVar = configurationVar.withMatcher(
                matcher.matcherName(),
                matcher.matcherClass().getDeclaredConstructor().newInstance()
            )
        }.onFailure {
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
        runCatching {
            fieldValue = documentContext.read(fieldJSONPath, Any::class.java)
            assertFieldValidation(
                expectedValueToCompare = expectedValueToCompare,
                fieldValue = fieldValue.toString()
            )
        }.onFailure { error ->
            if (BddLibConstants.BDD_LIB_NOT_EXISTS.equals(other = expectedValueToCompare, ignoreCase = true)) {
                assertNull(actual = fieldValue)
            } else {
                throw error
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
                assertFalse(actual = fieldMatches)
            } else {
                assertNotEquals(
                    actual = fieldValue,
                    illegal = expectedValueToCompare.substring(BddLibConstants.BDD_LIB_NOT.length)
                )
            }
        } else {
            if (matcher.isPresent) {
                val fieldMatches = matcher.get().matches(fieldValue)
                assertTrue(actual = fieldMatches)
            } else {
                assertEquals(
                    expected = expectedValueToCompare,
                    actual = fieldValue
                )
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
            .map { it.replace(oldValue = CARRIAGE_RETURN, newValue = "") }
            .map { it.replace(oldValue = JSON_SPACING, newValue = JSON_COMPACT) }
            .joinToString(separator = "") { it.trim() }
    }

    companion object {
        private val log = KotlinLogging.logger { }
        const val EMPTY_JSON = "{}"
        const val NEW_LINE = "\n"
        const val CARRIAGE_RETURN = "\r"
        const val JSON_SPACING = "\": "
        const val JSON_COMPACT = "\":"

        private val uuidMatcher = UUIDMatcher()
        private val ibanMatcher = IBANMatcher()
        private val neStringMatcher = NeStringMatcher()
        private val scenarioStateEqContextMatcher = ScenarioStateEqContextMatcher()
        private val scenarioStateNeContextMatcher = ScenarioStateNeContextMatcher()
    }
}
