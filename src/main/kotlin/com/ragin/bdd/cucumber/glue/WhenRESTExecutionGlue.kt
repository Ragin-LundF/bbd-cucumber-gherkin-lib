package com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.core.ScenarioStateContext.editableBody
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.datatable.DataTable
import io.cucumber.java.ParameterType
import io.cucumber.java.en.When
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import java.io.IOException

/**
 * This class contains common `When` execution of REST related steps.
 */
class WhenRESTExecutionGlue(jsonUtils: JsonUtils, restTemplate: TestRestTemplate) : BaseRESTExecutionGlue(jsonUtils, restTemplate) {
    /**
     * Execute a {httpMethod} call with previously given URI and body
     *
     * @param httpMethod HTTP Method
     */
    @When("executing a {httpMethod} call with previously given URI and body")
    fun whenExecutingCallWithPreviouslyGivenUriAndBody(httpMethod: HttpMethod) {
        executeRequest(httpMethod, false)
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI and body
     *
     * @param httpMethod HTTP Method
     */
    @When("executing an authorized {httpMethod} call with previously given URI and body")
    fun whenExecutingAuthorizedCallWithPreviouslyGivenUriAndBody(httpMethod: HttpMethod) {
        executeRequest(httpMethod, true)
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
        executeRequest(httpMethod, false)
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
        val body = readFile(pathToFile)
        editableBody = body
        executeRequest(httpMethod, false)
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
        val body = readFile(pathToFile)
        editableBody = body
        executeRequest(httpMethod, true)
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
        executeRequest(dataTable, httpMethod, false)
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
        executeRequest(dataTable, httpMethod, true)
    }

    /**
     * Execute a {httpMethod} call with previously given URI (short sentence)
     *
     * @param httpMethod HTTP Method
     */
    @When("executing a {httpMethod} call with previously given URI")
    fun whenExecutingCall(httpMethod: HttpMethod) {
        executeRequest(httpMethod, false)
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI (short sentence)
     *
     * @param httpMethod HTTP Method
     */
    @When("executing an authorized {httpMethod} call with previously given URI")
    fun whenExecutingAuthorizedCall(httpMethod: HttpMethod) {
        executeRequest(httpMethod, true)
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
        executeRequest(httpMethod, false)
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
        executeRequest(httpMethod, true)
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
        executeRequest(dataTable, httpMethod, true)
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
        executeRequest(dataTable, httpMethod, false)
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
}