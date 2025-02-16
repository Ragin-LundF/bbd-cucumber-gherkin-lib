package com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext.editableBody
import com.ragin.bdd.cucumber.core.ScenarioStateContext.headerValues
import com.ragin.bdd.cucumber.core.ScenarioStateContext.resolveEntry
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.java.en.When
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.test.web.client.TestRestTemplate
import java.util.*

class WhenRESTManipulationGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties,
    restTemplate: TestRestTemplate
) : BaseRESTExecutionGlue(
    jsonUtils = jsonUtils,
    bddProperties = bddProperties,
    restTemplate = restTemplate
) {
    /**
     * set the value of the previously given body property to a new value
     *
     * @param propertyPath  path to the JSON property
     * @param value         new value this element
     */
    @When("I set the value of the previously given body property {string} to {string}")
    fun whenISetTheValueOfTheBodyPropertyTo(propertyPath: String, value: String) {
        // fetch new value from state context if possible. Else use the given value
        var newValue = resolveEntry(key = value)

        when {
            "null" == newValue -> {
                editableBody = jsonUtils.removeJsonField(
                    originalJson = editableBody,
                    fieldPath = propertyPath
                )
            }
            newValue.matches("\\d* bdd_lib_numbers".toRegex()) -> {
                val numOfChars = newValue.split(" ").toTypedArray()[0].toInt()
                newValue = StringUtils.rightPad("", numOfChars, "1234567890")
                editableBody = jsonUtils.editJsonField(
                    originalJson = editableBody,
                    fieldPath = propertyPath,
                    newValue = newValue
                )
            }
            newValue.matches("bdd_lib_uuid".toRegex()) -> {
                editableBody = jsonUtils.editJsonField(
                    originalJson = editableBody,
                    fieldPath = propertyPath,
                    newValue = UUID.randomUUID().toString()
                )
            }
            else -> {
                editableBody = jsonUtils.editJsonField(
                    originalJson = editableBody,
                    fieldPath = propertyPath,
                    newValue = newValue
                )
            }
        }
    }

    /**
     * Set a header to a specific value.
     *
     * @param header        Header name
     * @param headerValue   Header value
     */
    @When("I set the header {string} to {string}")
    fun whenISetTheHeaderValueTo(header: String, headerValue: String) {
        headerValues[header] = resolveEntry(key = headerValue)
    }

    /**
     * Set a header to a specific prefixed value.
     *
     * @param header                Header name
     * @param headerValue           Header value
     * @param headerValuePrefix     Prefix for header value
     */
    @When("I set the header {string} to {string} prefixed by {string}")
    fun whenISetTheHeaderValueTo(header: String, headerValue: String, headerValuePrefix: String) {
        headerValues[header] = "${resolveEntry(key = headerValuePrefix)}${resolveEntry(key = headerValue)}"
    }
}
