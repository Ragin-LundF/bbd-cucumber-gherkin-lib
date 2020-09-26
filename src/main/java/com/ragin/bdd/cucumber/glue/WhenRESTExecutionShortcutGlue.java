package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

/**
 * This class contains the <code>When</code> execution of REST request steps for simplified executions.
 */
public class WhenRESTExecutionShortcutGlue extends BaseRESTExecutionGlue {
    /**
     * Execute a {httpMethod} call with previously given URI (short sentence)
     */
    @When("executing a {httpMethod} call with previously given URI")
    public void whenExecutingCall(final HttpMethod httpMethod) {
        executeRequest(httpMethod, false);
    }

    /**
     * Execute an authorized {httpMethod} call with previously given URI (short sentence)
     */
    @When("executing an authorized {httpMethod} call with previously given URI")
    public void whenExecutingAuthorizedCall(final HttpMethod httpMethod) {
        executeRequest(httpMethod, true);
    }

    /**
     * Execute a {httpMethod} call to the defined REST URL
     *
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
     * @param dataTable DataTable which contains the mapping of dynamic elements and values
     */
    @When("executing a {httpMethod} call with previously given API path and the dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingCallToUriWithDynamicURLElement(final HttpMethod httpMethod, final DataTable dataTable) {
        executeRequest(dataTable, httpMethod, false);
    }
}
