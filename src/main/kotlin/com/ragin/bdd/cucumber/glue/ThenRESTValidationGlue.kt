package com.ragin.bdd.cucumber.glue

import com.jayway.jsonpath.JsonPath
import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.ScenarioStateContext.executionTime
import com.ragin.bdd.cucumber.core.ScenarioStateContext.latestResponse
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Then
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import java.io.IOException

/**
 * This class contains HTTP response validations.
 */
class ThenRESTValidationGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties
) : BaseCucumberCore(
    jsonUtils = jsonUtils,
    bddProperties = bddProperties
) {
    /**
     * Ensure, that the response code is valid
     * @param expectedStatusCode HTTP status code that is expected
     */
    @Then("I ensure that the status code of the response is {int}")
    fun thenEnsureThatTheStatusCodeOfTheResponseIs(expectedStatusCode: Int) {
        assertThat(Integer.valueOf(latestResponse!!.statusCode.value())).isEqualTo(expectedStatusCode)
    }

    /**
     * Ensure, that the response is equal to a file.
     *
     * This evaluates also JSON unit statements
     *
     * see https://github.com/lukas-krecan/JsonUnit
     *
     * @param pathToFile path to file to read
     * @throws java.io.IOException error while reading file
     */
    @Then("I ensure that the body of the response is equal to the file {string}")
    @Throws(IOException::class)
    fun thenEnsureTheBodyOfTheResponseIsEqualToTheFile(pathToFile: String) {
        val expectedBody = readFileAsString(path = pathToFile)
        assertJSONisEqual(
            expected = expectedBody,
            actual = latestResponse!!.body
        )
    }

    @Then("I ensure, that the header {string} is equal to {string}")
    fun thenEnsureHeaderIsEqualTo(headerName: String, expectedValue: String) {
        val header = latestResponse!!.headers.getOrEmpty(headerName).joinToString(",")
        assertThat(header).isEqualTo(expectedValue)
            .describedAs("Header [$headerName] does not have the value [$expectedValue], but [$header]")
    }

    /**
     * Ensure, that the response is equal to a directly given body.
     *
     * This evaluates also JSON unit statements
     *
     * see https://github.com/lukas-krecan/JsonUnit
     *
     * @param expectedBody expected JSON body
     */
    @Then("^I ensure that the body of the response is equal to$")
    fun thenEnsureTheBodyOfTheResponseIsEqualTo(expectedBody: String) {
        assertJSONisEqual(
            expected = expectedBody,
            actual = latestResponse!!.body
        )
    }

    @Then("I ensure that the body of the response contains a field {string} with the value {string}")
    fun thenEnsureTheBodyOfTheResponseContainsFieldWithValue(fieldName: String, value: String) {
        jsonUtils.validateJsonField(
            originalJson = latestResponse!!.body,
            fieldPath = fieldName,
            expectedValue = value
        )
    }

    @Then("I ensure that the body of the response contains the following fields and values")
    fun thenEnsureTheBodyOfTheResponseContainsFieldWithValues(dataTable: DataTable) {
        val contextDataTableMap = dataTable.asMap(String::class.java, String::class.java)
        val keySet: Set<String> = contextDataTableMap.keys
        for (key in keySet) {
            jsonUtils.validateJsonField(
                originalJson = latestResponse!!.body,
                fieldPath = key,
                expectedValue = contextDataTableMap[key]!!
            )
        }
    }

    /**
     * Ensure, that the response code and body is equal to directly given status code and body
     *
     * This evaluates also JSON unit statements
     *
     * see https://github.com/lukas-krecan/JsonUnit
     *
     * @param expectedStatusCode expected response status code
     * @param expectedBody expected JSON body
     */
    @Then("I ensure that the response code is {int} and the body is equal to")
    fun thenEnsureTheResponseCodeAndBodyIsEqualTo(expectedStatusCode: Int, expectedBody: String) {
        assertThat(Integer.valueOf(latestResponse!!.statusCode.value())).isEqualTo(expectedStatusCode)
        assertJSONisEqual(
            expected = expectedBody,
            actual = latestResponse!!.body
        )
    }

    /**
     * Ensure, that the response code and body is equal to directly given status code and body as file
     *
     * This evaluates also JSON unit statements
     *
     * see https://github.com/lukas-krecan/JsonUnit
     *
     * @param expectedStatusCode expected response status code
     * @param pathToFile expected file that contains the JSON body
     * @throws java.io.IOException if the file can not be read
     */
    @Then("I ensure that the response code is {int} and the body is equal to the file {string}")
    @Throws(IOException::class)
    fun thenEnsureTheResponseCodeAndBodyAsFileIsEqualTo(
        expectedStatusCode: Int,
        pathToFile: String
    ) {
        val expectedBody = readFileAsString(path = pathToFile)
        assertThat(Integer.valueOf(latestResponse!!.statusCode.value())).isEqualTo(expectedStatusCode)
        assertJSONisEqual(
            expected = expectedBody,
            actual = latestResponse!!.body
        )
    }

    /**
     * Store a string of the response to the context to reuse them later.
     *
     * !!!ATTENTION!!! This is an Anti-Pattern, but sometimes it can be necessary if Cucumber should work as Test-Suite.
     *
     * For better separation use the database initializer and use static values instead
     * of transporting them between scenarios!
     *
     * @param fieldName     name of the field in the response
     * @param contextName   name of the field as which it should be stored
     *                      (should be unique, else it will be overwritten)
     */
    @Then("I store the string of the field {string} in the context {string} for later usage")
    fun storeStringOfFieldInContextForLaterUsage(fieldName: String, contextName: String) {
        assertThat(fieldName).isNotNull
        assertThat(contextName).isNotNull
        assertThat(latestResponse).isNotNull.describedAs("response was null!")
        assertThat(latestResponse!!.body).isNotNull.describedAs("body of response was null!")

        var jsonPath = fieldName
        if (!jsonPath.startsWith("$.")) {
            jsonPath = "$.$jsonPath"
        }
        val documentContext = JsonPath.parse(latestResponse!!.body)
        val field = documentContext.read(jsonPath, Any::class.java)
        scenarioContextMap[replaceTrailingAndLeadingQuotes(value = contextName)] = field.toString()
    }

    /**
     * Ensure that the execution time is less than a value in ms.
     *
     * @param expectedExecutionTime     expected time in ms
     */
    @Then("I ensure that the execution time is less than {long} ms")
    fun ensureThatExecutionTimeIsLessThan(expectedExecutionTime: Long) {
        val executionTime = System.currentTimeMillis() - executionTime
        val executionTimeValid = executionTime <= expectedExecutionTime
        assertThat(executionTimeValid).isTrue.describedAs("The performance was poor with [$executionTime ms]")
    }

    /**
     * Wait for a specified amount of milliseconds
     *
     * @param milliseconds  amount of milliseconds to wait
     */
    @Then("I wait for {long} ms")
    fun waitForSeconds(milliseconds: Long) {
        runCatching {
            Thread.sleep(milliseconds)
        }.onFailure { error ->
            log.error(throwable = error) { "Wait has detected a problem" }
        }
    }

    /**
     * Replace trailing and leading quotes
     *
     * @param value     String value
     * @return          argument without trailing and leading quotes
     */
    private fun replaceTrailingAndLeadingQuotes(value: String): String {
        var result = value
        if (result.startsWith(prefix = "\"")) {
            result = result.replaceFirst(regex = REGEX_QUOTE, replacement = "")
        }
        if (result.endsWith("\"")) {
            result = result.replace(regex = REGEX_QUOTE_DOLLAR, replacement = "")
        }
        return result
    }

    companion object {
        private val log = KotlinLogging.logger { }
        private val REGEX_QUOTE = "\"".toRegex()
        private val REGEX_QUOTE_DOLLAR = "\"$".toRegex()
    }
}
