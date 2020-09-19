package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import java.io.IOException;
import org.springframework.http.HttpMethod;

/**
 * This class contains the <code>When</code> execution of REST of DELETE request related steps.
 */
public class WhenRESTExecutionDELETEGlue extends BaseRESTExecutionGlue {
    /**
     * Execute a DELETE call with previously given URI and body
     */
    @When("executing a DELETE call with previously given URI and body")
    public void whenExecutingDELETECall() {
        executeRequest(HttpMethod.DELETE, false);
    }

    /**
     * Execute an authorized DELETE call with previously given URI and body
     */
    @When("executing an authorized DELETE call with previously given URI and body")
    public void whenExecutingDELETECallAuthorized() {
        executeRequest(HttpMethod.DELETE, true);
    }

    /**
     * Execute a DELETE call to an URL with previously given body
     *
     * @param path to the endpoint URI
     */
    @When("executing a DELETE call to {string} with previously given body")
    public void whenExecutingDELETECallToWithGivenBody(final String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.DELETE, false);
    }

    /**
     * Execute an authorized DELETE call to an URL with previously given body
     *
     * @param path to the endpoint URI
     */
    @When("executing an authorized DELETE call to {string} with previously given body")
    public void whenExecutingDELETECallToWithGivenBodyAuthorized(final String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.DELETE, true);
    }

    /**
     * Execute a DELETE call to the defined URL with body from file
     *
     * @param path to the endpoint URI
     * @param pathToFile path to file to read
     * @throws java.io.IOException error while reading file
     */
    @When("executing a DELETE call to {string} with the body from file {string}")
    public void whenExecutingDELETECallToPathWithBodyFromFile(final String path, final String pathToFile) throws IOException {
        ScenarioStateContext.current().setUriPath(path);
        final String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(HttpMethod.DELETE, false);
    }

    /**
     * Execute an authorized DELETE call to the defined URL with body from file
     *
     * @param path to the endpoint URI
     * @param pathToFile path to file to read
     * @throws java.io.IOException error while reading file
     */
    @When("executing an authorized DELETE call to {string} with the body from file {string}")
    public void whenExecutingDELETECallToPathWithBodyFromFileAuthorized(final String path, final String pathToFile) throws IOException {
        ScenarioStateContext.current().setUriPath(path);
        final String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(HttpMethod.DELETE, true);
    }

    /**
     * Execute a DELETE call without authentication to the defined URL with body from file and dynamic URL elements.
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
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing a DELETE call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingDELETECallToPathWithBodyAndDynamicURLElement(final DataTable dataTable) {
        executeRequest(dataTable, HttpMethod.DELETE, false);
    }

    /**
     * Execute an authorized DELETE call to the defined URL with body from file and dynamic URL elements.
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
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing an authorized DELETE call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingDELETECallToPathWithBodyAndDynamicURLElementAuthorized(final DataTable dataTable) {
        executeRequest(dataTable, HttpMethod.DELETE, true);
    }
}
