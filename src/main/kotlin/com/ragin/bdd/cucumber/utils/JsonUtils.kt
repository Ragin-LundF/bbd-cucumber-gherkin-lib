package com.ragin.bdd.cucumber.utils

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.ragin.bdd.cucumber.BddLibConstants
import com.ragin.bdd.cucumber.core.ScenarioStateContext.getJsonPathOptions
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import com.ragin.bdd.cucumber.matcher.*
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import org.apache.commons.logging.LogFactory
import org.hamcrest.Matcher
import org.junit.Assert
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

/**
 * Utility class used to work with JSON objects.
 */
@Component
class JsonUtils(private val jsonMatcher: Collection<BddCucumberJsonMatcher>?, private val bddCucumberDateTimeFormatter: Collection<BddCucumberDateTimeFormat>) {
    companion object {
        private val log = LogFactory.getLog(JsonUtils::class.java)
    }

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
            val minimizedExpected = minimizeJSON(expectedJSON)
            val minimizedActual = minimizeJSON(actualJSON)
            log.error("JSON comparison failed.\nExpected:\n\t$minimizedExpected\nActual:\n\t$minimizedActual\n")
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
                .`when`(Option.TREATING_NULL_AS_ABSENT)
                .withMatcher("isValidDate", ValidDateMatcher(bddCucumberDateTimeFormatter))
                .withMatcher("isDateOfContext", ValidDateContextMatcher(bddCucumberDateTimeFormatter))
                .withMatcher("isValidUUID", UUIDMatcher())
                .withMatcher("isValidIBAN", IBANMatcher())
                .withMatcher("isEqualToScenarioContext", ScenarioStateContextMatcher())

        // add additional options
        for (jsonOption in getJsonPathOptions()) {
            configuration = configuration.`when`(jsonOption)
        }

        // add additional matcher
        if (jsonMatcher != null && !jsonMatcher.isEmpty()) {
            for (matcher in jsonMatcher) {
                configuration = addMatcherConfiguration(configuration, matcher)
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
        } catch (e: Exception) {
            log.error("""Unable to instantiate the matcher [${matcher.matcherName()}]""")
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
        return editJsonField(originalJson, fieldPath, null)
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
            assertFieldValidation(expectedValueToCompare, fieldValue.toString())
        } catch (pnfe: PathNotFoundException) {
            if (BddLibConstants.BDD_LIB_NOT_EXISTS.equals(expectedValueToCompare, ignoreCase = true)) {
                Assert.assertNull(fieldValue)
            } else {
                throw pnfe
            }
        }
    }

    private fun assertFieldValidation(expectedValueToCompare: String, fieldValue: String) {
        val assertConfig = createJsonAssertConfiguration()
        var matcher: Optional<Matcher<*>> = Optional.empty()
        val matcherName = findMatcherName(expectedValueToCompare)
        if (matcherName.isPresent) {
            matcher = Optional.ofNullable(assertConfig.getMatcher(matcherName.get()))
        }
        if (expectedValueToCompare.startsWith(BddLibConstants.BDD_LIB_NOT)) {
            if (matcher.isPresent) {
                val fieldMatches = matcher.get().matches(fieldValue)
                Assert.assertFalse(fieldMatches)
            } else {
                Assert.assertNotEquals(expectedValueToCompare.substring(BddLibConstants.BDD_LIB_NOT.length), fieldValue)
            }
        } else {
            if (matcher.isPresent) {
                val fieldMatches = matcher.get().matches(fieldValue)
                Assert.assertTrue(fieldMatches)
            } else {
                Assert.assertEquals(expectedValueToCompare, fieldValue)
            }
        }
    }

    /**
     * Find custom matcher
     *
     * @param possibleMatcherName   string which possibly contains a matcher name
     * @return  if matcher name was found the matcher name
     */
    private fun findMatcherName(possibleMatcherName: String): Optional<String> {
        val matcher = BddLibConstants.BDD_LIB_MATCHER_PATTERN.matcher(possibleMatcherName)
        return if (matcher.find() && matcher.groupCount() == 3) {
            Optional.of(matcher.group(2))
        } else Optional.empty()
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
        val nullSafeJson = json ?: "{}"
        return Arrays.stream(nullSafeJson.split("\n").toTypedArray())
                .map { line: String -> line.replace("\r", "") }
                .map { line: String -> line.replace("\": ", "\":") }
                .map { obj: String -> obj.trim { it <= ' ' } }
                .collect(Collectors.joining(""))
    }
}
