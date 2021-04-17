package com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
import java.util.*

/**
 * This class contains steps to edit the test state.
 */
class GivenRESTStateGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties
) : BaseCucumberCore(jsonUtils, bddProperties) {
    @Value("\${cucumberTest.authorization.bearerToken.noscope:none}")
    private val noScopeBearerToken: String? = null

    /**
     * Defines the base path to which other paths are relative
     *
     * @param basePath of files
     */
    @Given("that all file paths are relative to {string}")
    fun givenThatAllFilePathsAreRelativeTo(basePath: String) {
        ScenarioStateContext.fileBasePath = basePath
    }

    /**
     * Defines the base URL path to which other URLs are relative
     *
     * @param basePath  of the URLs
     */
    @Given("that all URLs are relative to {string}")
    fun givenThatAllURLsAreRelativeTo(basePath: String) {
        ScenarioStateContext.urlBasePath = basePath
    }

    /**
     * Set a list of users and tokens.
     *
     * @param userDataTable     data table which contains the user in the first row and token in the second
     */
    @Given("that the following users and tokens are existing")
    fun givenThatUsersAndTokensExisting(userDataTable: DataTable) {
        val userDataMap = userDataTable.asMap<String, String>(String::class.java, String::class.java)
        val keySet: Set<String> = userDataMap.keys
        for (key in keySet) {
            ScenarioStateContext.userTokenMap[key] = userDataMap[key]!!
        }
    }

    /**
     * Defines to use a bearer token without scopes
     *
     * @param user name of the user
     */
    @Given("that the user is {string}")
    fun givenThatTheUserIs(user: String?) {
        ScenarioStateContext.bearerToken = ScenarioStateContext.userTokenMap[user]
    }

    /**
     * Defines to use a bearer token without scopes
     */
    @Given("that a bearer token without scopes is used")
    fun givenThatBearerTokenWithoutScopesIsUsed() {
        ScenarioStateContext.bearerToken = noScopeBearerToken
    }

    /**
     * Define to use a API path for a concrete endpoint
     *
     * @param apiPath   path of the endpoint
     */
    @Given("that the API path is {string}")
    fun givenThatAPIPathIs(apiPath: String) {
        ScenarioStateContext.uriPath = apiPath
    }

    /**
     * Define to use a file as the body
     *
     * @param pathToFile path to file for body
     * @throws java.io.IOException error while reading file
     */
    @Given("that the file {string} is used as the body")
    @Throws(IOException::class)
    fun givenThatTheFileIsUsedAsTheBody(pathToFile: String) {
        ScenarioStateContext.editableBody = readFile(pathToFile)
    }

    /**
     * Define to use a body directly
     *
     * @param body of the next request
     */
    @Given("^that the body of the request is$")
    fun givenThatTheBodyOfRequestIs(body: String) {
        ScenarioStateContext.editableBody = body
    }

    /**
     * Offers the possibility to set static values to the context
     *
     * @param key       Key in the context map
     * @param value     Value that should be used
     */
    @Given("that the context contains the key {string} with the value {string}")
    fun givenThatContextContainsKeyValue(key: String, value: String) {
        ScenarioStateContext.scenarioContextMap[key] = value
    }

    /**
     * With this sentence it is possible to add static values to the context map.
     *
     *
     *
     * DataTable looks like:
     * <pre>
     * | key | value |
     * | resourceId    | abc-def-gh |
     * | subResourceId | zyx-wvu-ts |
    </pre> *
     *
     * @param dataTable  DataTable with the key "key" and value "value"
     */
    @Given("that the context contains the following 'key' and 'value' pairs")
    fun givenThatContextContainsKeyValuePairFromDataTable(dataTable: DataTable) {
        val contextDataTableMap = dataTable.asMap<String, String>(String::class.java, String::class.java)
        val keySet: Set<String> = contextDataTableMap.keys
        for (key in keySet) {
            ScenarioStateContext.scenarioContextMap[key] = contextDataTableMap[key]!!
        }
    }

    /**
     * Configure the JSON assertion, that arrays can contain more elements than the expected JSON has
     */
    @Given("that the response JSON can contain arrays with extra elements")
    fun givenThatJsonCanContainExtraArrayElements() {
        ScenarioStateContext.addJsonIgnoringExtraArrayElements()
    }

    /**
     * Configure the JSON assertion, that arrays can contain more fields than the expected JSON has
     */
    @Given("that the response JSON can contain extra fields")
    fun givenThatJsonCanContainExtraFields() {
        ScenarioStateContext.addJsonIgnoringExtraFields()
    }

    /**
     * Configure the JSON assertion, that arrays have a different order than the expected JSON has
     */
    @Given("that the response JSON can contain arrays in different order")
    fun givenThatJsonHasDifferentArrayOrders() {
        ScenarioStateContext.addJsonIgnoringArrayOrder()
    }

    @Given("that the stored data in the scenario context map has been reset")
    fun givenThatStoredDataInContextMapHasBeenReset() {
        ScenarioStateContext.scenarioContextMap = HashMap()
    }

    @Given("that the Bearer token is {string}")
    fun givenThatBearerTokenIsUsed(bearerToken: String) {
        val bearerFromContext: String? = ScenarioStateContext.scenarioContextMap[bearerToken]
        if (Objects.isNull(bearerFromContext)) {
            ScenarioStateContext.bearerToken = bearerToken
        } else {
            ScenarioStateContext.bearerToken = bearerFromContext
        }
    }

    @Given("that a requests polls every {int} seconds")
    fun givenPollingSeconds(seconds: Long) {
        ScenarioStateContext.polling.pollEverySeconds = seconds
    }

    @Given("that a requests polls for {int} times")
    fun givenPollingNumberOfPolls(numberOfPolls: Int) {
        ScenarioStateContext.polling.numberOfPolls = numberOfPolls
    }

    @Given("that a request polls every {int} seconds for {int} times")
    fun givenPollingSecondsAndNumberOfPolls(seconds: Long, numberOfPolls: Int) {
        ScenarioStateContext.polling.pollEverySeconds = seconds
        ScenarioStateContext.polling.numberOfPolls = numberOfPolls
    }
}