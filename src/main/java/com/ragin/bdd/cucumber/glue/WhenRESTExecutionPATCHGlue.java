package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

/**
 * This class contains the <code>When</code> execution of REST PATCH request related steps.
 */
public class WhenRESTExecutionPATCHGlue extends BaseRESTExecutionGlue {
    /**
     * Execute a PATCH call with previously given URI and body
     */
    @When("executing a PATCH call with previously given URI and body")
    public void whenExecutingPATCHCall() {
        executeRequest(HttpMethod.PATCH, false);
    }

    /**
     * Execute an authorized PATCH call with previously given URI and body
     */
    @When("executing an authorized PATCH call with previously given URI and body")
    public void whenExecutingPATCHCallAuthorized() {
        executeRequest(HttpMethod.PATCH, true);
    }

    /**
     * Execute a PATCH call to an URL with previously given body
     */
    @When("executing a PATCH call to {string} with previously given body")
    public void whenExecutingPATCHCallToWithGivenBody(String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.PATCH, false);
    }

    /**
     * Execute an authorized PATCH call to an URL with previously given body
     */
    @When("executing an authorized PATCH call to {string} with previously given body")
    public void whenExecutingPATCHCallToWithGivenBodyAuthorized(String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.PATCH, true);
    }

    /**
     * Execute a PATCH call to the defined URL with body from file
     */
    @When("executing a PATCH call to {string} with the body from file {string}")
    public void whenExecutingPATCHCallToPathWithBodyFromFile(String path, String pathToFile) throws Exception {
        ScenarioStateContext.current().setUriPath(path);
        String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(HttpMethod.PATCH, false);
    }

    /**
     * Execute an authorized PATCH call to the defined URL with body from file
     */
    @When("executing an authorized PATCH call to {string} with the body from file {string}")
    public void whenExecutingPATCHCallToPathWithBodyFromFileAuthorized(String path, String pathToFile) throws Exception {
        ScenarioStateContext.current().setUriPath(path);
        String body = readFile(pathToFile);
        ScenarioStateContext.current().setEditableBody(body);

        executeRequest(HttpMethod.PATCH, true);
    }

    /**
     * Execute a PATCH call without authentication to the defined URL with body from file and dynamic URL elements.
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
    @When("executing a PATCH call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingPATCHCallToPathWithBodyAndDynamicURLElement(DataTable dataTable) throws Exception {
        executeRequest(dataTable, HttpMethod.PATCH, false);
    }

    /**
     * Execute an authorized PATCH call to the defined URL with body from file and dynamic URL elements.
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
    @When("executing an authorized PATCH call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingPATCHCallToPathWithBodyAndDynamicURLElementAuthorized(DataTable dataTable) throws Exception {
        executeRequest(dataTable, HttpMethod.PATCH, true);
    }
}
