package com.ragin.bdd.cucumber.glue

import com.jayway.jsonpath.JsonPath
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.ScenarioStateContext.executionTime
import com.ragin.bdd.cucumber.core.ScenarioStateContext.latestResponse
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Then
import org.junit.Assert
import java.io.IOException

/**
 * This class contains HTTP response validations.
 */
class ThenRESTValidationGlue(jsonUtils: JsonUtils) : BaseCucumberCore(jsonUtils) {
    /**
     * Ensure, that the response code is valid
     * @param expectedStatusCode HTTP status code that is expected
     */
    @Then("I ensure that the status code of the response is {int}")
    fun thenEnsureThatTheStatusCodeOfTheResponseIs(expectedStatusCode: Int) {
        Assert.assertEquals(
                expectedStatusCode,
                Integer.valueOf(latestResponse!!.statusCode.value())
        )
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
        val expectedBody = readFile(pathToFile)
        assertJSONisEqual(
                expectedBody,
                latestResponse!!.body
        )
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
                expectedBody,
                latestResponse!!.body
        )
    }

    @Then("I ensure that the body of the response contains a field {string} with the value {string}")
    fun thenEnsureTheBodyOfTheResponseContainsFieldWithValue(fieldName: String, value: String) {
        jsonUtils.validateJsonField(latestResponse!!.body, fieldName, value)
    }

    @Then("I ensure that the body of the response contains the following fields and values")
    fun thenEnsureTheBodyOfTheResponseContainsFieldWithValues(dataTable: DataTable) {
        val contextDataTableMap = dataTable.asMap<String, String>(String::class.java, String::class.java)
        val keySet: Set<String> = contextDataTableMap.keys
        for (key in keySet) {
            jsonUtils.validateJsonField(latestResponse!!.body, key, contextDataTableMap[key]!!)
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
        Assert.assertEquals(
                expectedStatusCode,
                Integer.valueOf(latestResponse!!.statusCode.value())
        )
        assertJSONisEqual(
                expectedBody,
                latestResponse!!.body
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
        val expectedBody = readFile(pathToFile)
        Assert.assertEquals(
                expectedStatusCode,
                Integer.valueOf(latestResponse!!.statusCode.value())
        )
        assertJSONisEqual(
                expectedBody,
                latestResponse!!.body
        )
    }

    /**
     * Store a string of the response to the context to reuse them later.
     *
     * !!!ATTENTION!!! This is an Anti-Pattern, but sometimes it can be necessary if Cucumber should work as Test-Suite.
     *
     * For better separation use the database initializer and use static values instead of transporting them between scenarios!
     *
     * @param fieldName     name of the field in the response
     * @param contextName   name of the field as which it should be stored (should be unique, else it will be overwritten)
     */
    @Then("I store the string of the field {string} in the context {string} for later usage")
    fun storeStringOfFieldInContextForLaterUsage(
            fieldName: String,
            contextName: String
    ) {
        Assert.assertNotNull(fieldName)
        Assert.assertNotNull(contextName)
        Assert.assertNotNull(
                "response was null!",
                latestResponse
        )
        Assert.assertNotNull(
                "body of response was null",
                latestResponse!!.body
        )
        var jsonPath = fieldName
        if (!jsonPath.startsWith("$.")) {
            jsonPath = "$.$jsonPath"
        }
        val documentContext = JsonPath.parse(latestResponse!!.body)
        val field = documentContext.read(jsonPath, String::class.java)
        scenarioContextMap[replaceTrailingAndLeadingQuotes(contextName)] = field
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
        Assert.assertTrue("The performance was poor with [$executionTime ms]", executionTimeValid)
    }

    /**
     * Replace trailing and leading quotes
     *
     * @param value     String value
     * @return          argument without trailing and leading quotes
     */
    private fun replaceTrailingAndLeadingQuotes(value: String): String {
        var result = value
        if (result.startsWith("\"")) {
            result = result.replaceFirst("\"".toRegex(), "")
        }
        if (result.endsWith("\"")) {
            result = result.replace("\"$".toRegex(), "")
        }
        return result
    }
}