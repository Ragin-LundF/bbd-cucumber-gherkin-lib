package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.When;
import java.io.IOException;
import org.springframework.http.HttpMethod;

/**
 * This class contains common <code>When</code> execution of REST related steps.
 */
public class WhenRESTExecutionGlue extends BaseRESTExecutionGlue {
    /**
     * Execute a {httpMethod} call with previously given URI and body
     *
     * @param httpMethod HTTP Method
     */
    @When("executing a {httpMethod} call with previously given URI and body")
    public void whenExecutingCallWithPreviouslyGivenUriAndBody(final HttpMethod httpMethod) {
        executeRequest(httpMethod, false);
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI and body
     *
     * @param httpMethod HTTP Method
     */
    @When("executing an authorized {httpMethod} call with previously given URI and body")
    public void whenExecutingAuthorizedCallWithPreviouslyGivenUriAndBody(final HttpMethod httpMethod) {
        executeRequest(httpMethod, true);
    }

    /**
     * Execute a {httpMethod} call to an URL with previously given body
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     */
    @When("executing a {httpMethod} call to {string} with previously given body")
    public void whenExecutingCallToUriAndPreviouslyGivenBody(final HttpMethod httpMethod, final String uri) {
        ScenarioStateContext.current().setUriPath(uri);

        executeRequest(httpMethod, false);
    }

    /**
     * Execute an authorized {httpMethod} call to an URL with previously given body
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     */
    @When("executing an authorized {httpMethod} call to {string} with previously given body")
    public void whenExecutingAuthorizedCallToUriWithPreviouslyGivenBody(final HttpMethod httpMethod, final String uri) {
        ScenarioStateContext.current().setUriPath(uri);

        executeRequest(httpMethod, true);
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
    public void whenExecutingCallToUriAndBodyFromFile(final HttpMethod httpMethod, final String uri, final String pathToFile) throws IOException {
        ScenarioStateContext.current().setUriPath(uri);
        final String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(httpMethod, false);
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
    public void whenExecutingAuthorizedCallToUriWithBodyFromFile(final HttpMethod httpMethod, final String uri, final String pathToFile) throws IOException {
        ScenarioStateContext.current().setUriPath(uri);
        final String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(httpMethod, true);
    }

    /**
     * Execute a {httpMethod} call without authentication to the defined URL with body from file and dynamic URL elements.
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br>
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     * <p>!!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing a {httpMethod} call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingCallToUriWithBodyAndDynamicURLElement(final HttpMethod httpMethod, final DataTable dataTable) {
        executeRequest(dataTable, httpMethod, false);
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL with body from file and dynamic URL elements.
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br>
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     * <p>!!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing an authorized {httpMethod} call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingAuthorizedCallToUriWithBodyAndDynamicURLElement(final HttpMethod httpMethod, final DataTable dataTable) {
        executeRequest(dataTable, httpMethod, true);
    }
    /**
     * Execute a {httpMethod} call with previously given URI (short sentence)
     *
     * @param httpMethod HTTP Method
     */
    @When("executing a {httpMethod} call with previously given URI")
    public void whenExecutingCall(final HttpMethod httpMethod) {
        executeRequest(httpMethod, false);
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI (short sentence)
     *
     * @param httpMethod HTTP Method
     */
    @When("executing an authorized {httpMethod} call with previously given URI")
    public void whenExecutingAuthorizedCall(final HttpMethod httpMethod) {
        executeRequest(httpMethod, true);
    }

    /**
     * Execute a {httpMethod} call to the defined REST URL
     *
     * @param httpMethod HTTP Method
     * @param uri   URI path to call.
     */
    @When("executing a {httpMethod} call to {string}")
    public void whenExecutingCallToUri(final HttpMethod httpMethod, final String uri) {
        ScenarioStateContext.current().setUriPath(uri);

        executeRequest(httpMethod, false);
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL
     *
     * @param httpMethod HTTP Method
     * @param uri to the endpoint URI
     */
    @When("executing an authorized {httpMethod} call to {string}")
    public void whenExecutingAuthorizedCallToUri(final HttpMethod httpMethod, final String uri) {
        ScenarioStateContext.current().setUriPath(uri);

        executeRequest(httpMethod, true);
    }

    /**
     * Execute an authorized {httpMethod} call to the defined URL and dynamic URL elements.
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br>
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     * <p>!!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing an authorized {httpMethod} call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingAuthorizedCallToUriWithDynamicURLElement(final HttpMethod httpMethod, final DataTable dataTable) {
        executeRequest(dataTable, httpMethod, true);
    }

    /**
     * Execute a {httpMethod} call without authentication to the defined URL with dynamic URL elements.
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br>
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     * <p>!!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     *
     * @param httpMethod HTTP Method
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing a {httpMethod} call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingCallToUriWithDynamicURLElement(final HttpMethod httpMethod, final DataTable dataTable) {
        executeRequest(dataTable, httpMethod, false);
    }


    /**
     * Definition of {httpMethod} to offer concrete but dynamic parameter type
     *
     * @param httpMethod  String value of HttpMethod
     * @return            HttpMethod enum
     */
    @ParameterType("GET|POST|PUT|PATCH|DELETE")
    public HttpMethod httpMethod(String httpMethod) {
        return HttpMethod.valueOf(httpMethod);
    }
}
