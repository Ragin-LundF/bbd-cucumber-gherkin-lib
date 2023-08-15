package com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.core.ScenarioStateContext.editableBody
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.ParameterType
import io.cucumber.java.Scenario
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.apache.commons.logging.LogFactory
import org.junit.Assert
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * This class contains common `When` execution of REST related steps.
 */
@Suppress("TooManyFunctions")
class WhenRESTExecutionGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties,
    restTemplate: TestRestTemplate
) : BaseRESTExecutionGlue(
    jsonUtils = jsonUtils,
    bddProperties = bddProperties,
    restTemplate = restTemplate
) {
    lateinit var scenarioState: Scenario

    @Before
    fun injectScenario(scenario: Scenario) {
        scenarioState = scenario
    }

    /**
     * Execute a {httpMethod} call with previously given URI and body
     *
     * @param httpMethod HTTP Method
     */
    @When("executing a {httpMethod} call with previously given URI and body")
    fun whenExecutingCallWithPreviouslyGivenUriAndBody(httpMethod: HttpMethod) {
        executeRequest(
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI and body
     *
     * @param httpMethod HTTP Method
     */
    @When("executing an authorized {httpMethod} call with previously given URI and body")
    fun whenExecutingAuthorizedCallWithPreviouslyGivenUriAndBody(httpMethod: HttpMethod) {
        executeRequest(
            httpMethod = httpMethod,
            authorized = true,
            scenario = scenarioState
        )
    }

    /**
     * Execute a {httpMethod} call to an URL with previously given body
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     */
    @When("executing a {httpMethod} call to {string} with previously given body")
    fun whenExecutingCallToUriAndPreviouslyGivenBody(httpMethod: HttpMethod, uri: String) {
        uriPath = uri
        executeRequest(
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call to an URL with previously given body
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     */
    @When("executing an authorized {httpMethod} call to {string} with previously given body")
    fun whenExecutingAuthorizedCallToUriWithPreviouslyGivenBody(httpMethod: HttpMethod, uri: String) {
        whenExecutingAuthorizedCallToUri(httpMethod, uri)
    }

    /**
     * Execute a {httpMethod} call to the defined URL with body from file
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     * @param pathToFile path to file to read
     * @throws java.io.IOException error while reading file
     */
    @When("executing a {httpMethod} call to {string} with the body from file {string}")
    @Throws(IOException::class)
    fun whenExecutingCallToUriAndBodyFromFile(httpMethod: HttpMethod, uri: String, pathToFile: String) {
        uriPath = uri
        val body = readFileAsString(pathToFile)
        editableBody = body
        executeRequest(
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL with body from file
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     * @param pathToFile path to file to read
     * @throws java.io.IOException error while reading file
     */
    @When("executing an authorized {httpMethod} call to {string} with the body from file {string}")
    @Throws(IOException::class)
    fun whenExecutingAuthorizedCallToUriWithBodyFromFile(httpMethod: HttpMethod, uri: String, pathToFile: String) {
        uriPath = uri
        val body = readFileAsString(pathToFile)
        editableBody = body
        executeRequest(
            httpMethod = httpMethod,
            authorized = true,
            scenario = scenarioState
        )
    }

    /**
     * Execute a {httpMethod} call without authentication to the defined URL with body from file and dynamic URL elements.
     *
     * For better separation use the database initializer and use static values instead of transporting them between scenarios!
     * <br></br>
     *
     *
     * DataTable looks like:
     * <pre>
     * | URI Elements  | URI Values |
     * | resourceId    | abc-def-gh |
     * | subResourceId | zyx-wvu-ts |
     * </pre>
     *
     * Structure:
     *
     *  * First line is the header.
     *  * 'URI Elements' is the element name of the dynamic URI path without {}.
     *  * 'URI Values' will be evaluated first from the scenario context.
     * If context var was not found use value directly
     *
     *
     * !!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing a {httpMethod} call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    fun whenExecutingCallToUriWithBodyAndDynamicURLElement(httpMethod: HttpMethod, dataTable: DataTable) {
        executeRequest(
            dataTable = dataTable,
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL with body from file and dynamic URL elements.
     *
     * For better separation use the database initializer and use static values instead of transporting them between scenarios!
     * <br></br>
     *
     *
     * DataTable looks like:
     * <pre>
     * | URI Elements  | URI Values |
     * | resourceId    | abc-def-gh |
     * | subResourceId | zyx-wvu-ts |
     * </pre>
     *
     * Structure:
     *
     *  * First line is the header.
     *  * 'URI Elements' is the element name of the dynamic URI path without {}.
     *  * 'URI Values' will be evaluated first from the scenario context.
     * If context var was not found use value directly
     *
     *
     * !!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing an authorized {httpMethod} call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    fun whenExecutingAuthorizedCallToUriWithBodyAndDynamicURLElement(httpMethod: HttpMethod, dataTable: DataTable) {
        executeRequest(
            dataTable = dataTable,
            httpMethod = httpMethod,
            authorized = true,
            scenario = scenarioState
        )
    }

    /**
     * Execute a {httpMethod} call with previously given URI (short sentence)
     *
     * @param httpMethod HTTP Method
     */
    @When("executing a {httpMethod} call with previously given URI")
    fun whenExecutingCall(httpMethod: HttpMethod) {
        executeRequest(
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI (short sentence)
     *
     * @param httpMethod HTTP Method
     */
    @When("executing an authorized {httpMethod} call with previously given URI")
    fun whenExecutingAuthorizedCall(httpMethod: HttpMethod) {
        executeRequest(
            httpMethod = httpMethod,
            authorized = true,
            scenario = scenarioState
        )
    }

    /**
     * Execute a {httpMethod} call to the defined REST URL
     *
     * @param httpMethod HTTP Method
     * @param uri   URI path to call.
     */
    @When("executing a {httpMethod} call to {string}")
    fun whenExecutingCallToUri(httpMethod: HttpMethod, uri: String) {
        uriPath = uri
        executeRequest(
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     */
    @When("executing an authorized {httpMethod} call to {string}")
    fun whenExecutingAuthorizedCallToUri(httpMethod: HttpMethod, uri: String) {
        uriPath = uri
        executeRequest(
            httpMethod = httpMethod,
            authorized = true,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL and dynamic URL elements.
     *
     * For better separation use the database initializer and use static values instead of transporting them between scenarios!
     * <br></br>
     *
     *
     * DataTable looks like:
     * <pre>
     * | URI Elements  | URI Values |
     * | resourceId    | abc-def-gh |
     * | subResourceId | zyx-wvu-ts |
     * </pre>
     *
     * Structure:
     *
     *  * First line is the header.
     *  * 'URI Elements' is the element name of the dynamic URI path without {}.
     *  * 'URI Values' will be evaluated first from the scenario context.
     * If context var was not found use value directly
     *
     *
     * !!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing an authorized {httpMethod} call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'")
    fun whenExecutingAuthorizedCallToUriWithDynamicURLElement(httpMethod: HttpMethod, dataTable: DataTable) {
        executeRequest(
            dataTable = dataTable,
            httpMethod = httpMethod,
            authorized = true,
            scenario = scenarioState
        )
    }

    /**
     * Execute a {httpMethod} call without authentication to the defined URL with dynamic URL elements.
     *
     * For better separation use the database initializer and use static values instead of transporting them between scenarios!
     * <br></br>
     *
     *
     * DataTable looks like:
     * <pre>
     * | URI Elements  | URI Values |
     * | resourceId    | abc-def-gh |
     * | subResourceId | zyx-wvu-ts |
     * </pre>
     *
     * Structure:
     *
     *  * First line is the header.
     *  * 'URI Elements' is the element name of the dynamic URI path without {}.
     *  * 'URI Values' will be evaluated first from the scenario context.
     * If context var was not found use value directly
     *
     *
     * !!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing a {httpMethod} call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'")
    fun whenExecutingCallToUriWithDynamicURLElement(httpMethod: HttpMethod, dataTable: DataTable) {
        executeRequest(
            dataTable = dataTable,
            httpMethod = httpMethod,
            authorized = false,
            scenario = scenarioState
        )
    }

    /**
     * Execute an authorized poll request, which has to be preconfigured until the response contains
     * the status and response message from a file.
     *
     * @param httpMethod HTTP Method
     * @param expectedStatusCode the expected HTTP status code
     * @param pathToFile describes the path to the expected JSON response file
     */
    @Then("executing an authorized {httpMethod} poll request until the response code is {int} and the body is equal to file {string}")
    @Throws(IOException::class)
    fun whenExecutingAuthorizedPollingUntilResponseIsEqualToFile(
        httpMethod: HttpMethod,
        expectedStatusCode: Int,
        pathToFile: String
    ) {
        val expectedBody = readFileAsString(pathToFile)
        executePollRequestUntilResponseIsEqual(httpMethod, expectedStatusCode, expectedBody, true)
    }

    /**
     * Execute a poll request, which has to be preconfigured until the response contains
     * the status and response message from a file.
     *
     * @param httpMethod HTTP Method
     * @param expectedStatusCode the expected HTTP status code
     * @param pathToFile describes the path to the expected JSON response file
     */
    @Then("executing a {httpMethod} poll request until the response code is {int} and the body is equal to file {string}")
    @Throws(IOException::class)
    fun whenExecutingPollingUntilResponseIsEqualToFile(
        httpMethod: HttpMethod,
        expectedStatusCode: Int,
        pathToFile: String
    ) {
        val expectedBody = readFileAsString(
            path = pathToFile
        )
        executePollRequestUntilResponseIsEqual(
            httpMethod = httpMethod,
            expectedStatusCode = expectedStatusCode,
            expectedBody = expectedBody,
            authorized = false
        )
    }

    /**
     * Execute an authorized poll request, which has to be preconfigured until the response contains
     * the status.
     *
     * @param httpMethod HTTP Method
     * @param expectedStatusCode the expected HTTP status code
     */
    @Then("executing an authorized {httpMethod} poll request until the response code is {int}")
    @Throws(IOException::class)
    fun whenExecutingAuthorizedPollingUntilResponseCodeIsEqual(httpMethod: HttpMethod,  expectedStatusCode: Int) {
        executePollRequestUntilResponseIsEqual(
            httpMethod = httpMethod,
            expectedStatusCode = expectedStatusCode,
            authorized = true
        )
    }

    /**
     * Execute a poll request, which has to be preconfigured until the response contains
     * the status.
     *
     * @param httpMethod HTTP Method
     * @param expectedStatusCode the expected HTTP status code
     */
    @Then("executing a {httpMethod} poll request until the response code is {int}")
    @Throws(IOException::class)
    fun whenExecutingPollingUntilResponseCodeIsEqual(httpMethod: HttpMethod, expectedStatusCode: Int) {
        executePollRequestUntilResponseIsEqual(
            httpMethod = httpMethod,
            expectedStatusCode = expectedStatusCode,
            authorized = false
        )
    }

    /**
     * Execute an authorized poll request, which has to be preconfigured until the response contains
     * the status and JSON response message.
     *
     * @param httpMethod HTTP Method
     * @param expectedStatusCode the expected HTTP status code
     * @param expectedBody describes the expected JSON body
     */
    @Then("executing an authorized {httpMethod} poll request until the response code is {int} and the body is equal to")
    fun whenExecutingAuthorizedPollingUntilResponseIsEqual(
        httpMethod: HttpMethod,
        expectedStatusCode: Int,
        expectedBody: String
    ) {
        executePollRequestUntilResponseIsEqual(
            httpMethod = httpMethod,
            expectedStatusCode = expectedStatusCode,
            expectedBody = expectedBody,
            authorized = true
        )
    }

    /**
     * Execute a poll request, which has to be preconfigured until the response contains
     * the status and JSON response message.
     *
     * @param httpMethod HTTP Method
     * @param expectedStatusCode the expected HTTP status code
     * @param expectedBody describes the expected JSON body
     */
    @Then("executing a {httpMethod} poll request until the response code is {int} and the body is equal to")
    fun whenExecutingPollingUntilResponseIsEqual(
        httpMethod: HttpMethod,
        expectedStatusCode: Int,
        expectedBody: String
    ) {
        executePollRequestUntilResponseIsEqual(
            httpMethod = httpMethod,
            expectedStatusCode = expectedStatusCode,
            expectedBody = expectedBody,
            authorized = false
        )
    }

    /**
     * Executes a form-data multipart POST request.
     *
     * The datatable contains the fields of the request as key/value pair.
     */
    @When("executing a form-data POST call to {string} with the fields")
    fun whenExecutingFormDataRequest(uri: String, dataTable: DataTable) {
        uriPath = uri
        executeFormDataRequest(
            dataTable = dataTable,
            authorized = false
        )
    }

    /**
     * Executes an authorized form-data multipart POST request.
     *
     * The datatable contains the fields of the request as key/value pair.
     */
    @When("executing an authorized form-data POST call to {string} with the fields")
    fun whenExecutingAuthorizedFormDataRequest(uri: String, dataTable: DataTable) {
        uriPath = uri
        executeFormDataRequest(
            dataTable = dataTable,
            authorized = true
        )
    }

    private fun executePollRequestUntilResponseIsEqual(
        httpMethod: HttpMethod,
        expectedStatusCode: Int,
        expectedBody: String? = null,
        authorized: Boolean
    ) {
        Assert.assertNotEquals("Please configure max number of polls!",
            -1,
            ScenarioStateContext.polling.numberOfPolls
        )

        var repeatLoop = 0
        for (i in 1..ScenarioStateContext.polling.numberOfPolls) {
            executeRequest(
                httpMethod = httpMethod,
                authorized = authorized,
                scenario = scenarioState
            )

            try {
                evaluateBody(expectedBody)
                Assert.assertEquals(expectedStatusCode, ScenarioStateContext.latestResponse!!.statusCode.value())
                repeatLoop = i
                break
            } catch (_: AssertionError) {
                TimeUnit.SECONDS.sleep(ScenarioStateContext.polling.pollEverySeconds)
            }
        }

        evaluateBody(expectedBody)
        Assert.assertEquals(expectedStatusCode, ScenarioStateContext.latestResponse!!.statusCode.value())
        log.info("Polling finished after $repeatLoop repeats")
    }

    /**
     * Compare message body with an expected result if the body is present.
     *
     * @param expectedBody body
     */
    private fun evaluateBody(expectedBody: String?) {
        if (!expectedBody.isNullOrEmpty()) {
            assertJSONisEqual(
                expectedBody,
                ScenarioStateContext.latestResponse!!.body
            )
        }
    }

    /**
     * Definition of {httpMethod} to offer concrete but dynamic parameter type
     *
     * @param httpMethod  String value of HttpMethod
     * @return            HttpMethod enum
     */
    @ParameterType("GET|POST|PUT|PATCH|DELETE")
    fun httpMethod(httpMethod: String): HttpMethod {
        return HttpMethod.valueOf(httpMethod)
    }

    companion object {
        private val log = LogFactory.getLog(ThenRESTValidationGlue::class.java)
    }
}
