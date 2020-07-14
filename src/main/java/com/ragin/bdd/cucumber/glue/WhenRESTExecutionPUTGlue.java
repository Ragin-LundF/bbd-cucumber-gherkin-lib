package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import java.io.IOException;
import org.springframework.http.HttpMethod;

/**
 * This class contains the <code>When</code> execution of REST PUT request related steps.
 */
public class WhenRESTExecutionPUTGlue extends BaseRESTExecutionGlue {
    /**
     * Execute a PUT call with previously given URI and body
     */
    @When("executing a PUT call with previously given URI and body")
    public void whenExecutingPUTCall() {
        executeRequest(HttpMethod.PUT, false);
    }

    /**
     * Execute an authorized PUT call with previously given URI and body
     */
    @When("executing an authorized PUT call with previously given URI and body")
    public void whenExecutingPUTCallAuthorized() {
        executeRequest(HttpMethod.PUT, true);
    }

    /**
     * Execute a PUT call to an URL with previously given body
     */
    @When("executing a PUT call to {string} with previously given body")
    public void whenExecutingPUTCallToWithGivenBody(String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.PUT, false);
    }

    /**
     * Execute an authorized PUT call to an URL with previously given body
     */
    @When("executing an authorized PUT call to {string} with previously given body")
    public void whenExecutingPUTCallToWithGivenBodyAuthorized(String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.PUT, true);
    }

    /**
     * Execute a PUT call to the defined URL with body from file
     */
    @When("executing a PUT call to {string} with the body from file {string}")
    public void whenExecutingPUTCallToPathWithBodyFromFile(String path, String pathToFile) throws IOException {
        ScenarioStateContext.current().setUriPath(path);
        String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(HttpMethod.PUT, false);
    }

    /**
     * Execute an authorized PUT call to the defined URL with body from file
     */
    @When("executing an authorized PUT call to {string} with the body from file {string}")
    public void whenExecutingPUTCallToPathWithBodyFromFileAuthorized(String path, String pathToFile) throws IOException {
        ScenarioStateContext.current().setUriPath(path);
        String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(HttpMethod.PUT, true);
    }

    /**
     * Execute a PUT call without authentication to the defined URL with body from file and dynamic URL elements.
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br>
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * <p>
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     * <p>!!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     */
    @When("executing a PUT call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingPUTCallToPathWithBodyAndDynamicURLElement(DataTable dataTable) {
        executeRequest(dataTable, HttpMethod.PUT, false);
    }

    /**
     * Execute an authorized PUT call to the defined URL with body from file and dynamic URL elements.
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br>
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * <p>
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     * <p>!!!ATTENTION!!! This is an Anti-Pattern if you reuse previously stored elements, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     */
    @When("executing an authorized PUT call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingPUTCallToPathWithBodyAndDynamicURLElementAuthorized(DataTable dataTable) {
        executeRequest(dataTable, HttpMethod.PUT, true);
    }
}
