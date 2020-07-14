package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

/**
 * This class contains the <code>When</code> execution of REST GET request related steps.
 */
public class WhenRESTExecutionGETGlue extends BaseRESTExecutionGlue {
    /**
     * Execute a GET call with previously given URI
     */
    @When("executing a GET call with previously given URI")
    public void whenExecutingAGETCall() {
        executeRequest(HttpMethod.GET, false);
    }

    /**
     * Execute an authorized GET call with previously given URI
     */
    @When("executing an authorized GET call with previously given URI")
    public void whenExecutingAnAuthorizedGETCall() {
        executeRequest(HttpMethod.GET, true);
    }

    /**
     * Execute a GET call to the defined REST URL
     *
     * @param path   URI path to call.
     */
    @When("executing a GET call to {string}")
    public void whenExecutingAGETCallToPath(final String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.GET, false);
    }

    /**
     * Execute an authorized GET call to the defined URL
     *
     * @param path to the endpoint URI
     */
    @When("executing an authorized GET call to {string}")
    public void whenExecutingAnAuthorizedGETCallToUrl(final String path) {
        ScenarioStateContext.current().setUriPath(path);

        executeRequest(HttpMethod.GET, true);
    }

    /**
     * Execute an authorized GET call to the defined URL and dynamic URL elements.
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
    @When("executing an authorized GET call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingAnAuthorizedGETCallToPathWithDynamicURLElement(DataTable dataTable) {
        executeRequest(dataTable, HttpMethod.GET, true);
    }

    /**
     * Execute a GET call without authentication to the defined URL with dynamic URL elements.
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
    @When("executing a GET call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingAGETCallToPathWithDynamicURLElement(DataTable dataTable) {
        executeRequest(dataTable, HttpMethod.GET, false);
    }
}
