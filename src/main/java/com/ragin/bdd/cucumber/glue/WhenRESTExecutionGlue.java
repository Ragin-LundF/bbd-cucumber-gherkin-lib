package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import com.ragin.bdd.cucumber.utils.JsonUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * This class contains the <code>When</code> execution of REST related steps.
 */
public class WhenRESTExecutionGlue extends BaseCucumberCore {
    private static final String SERVER_URL = "http://localhost:";
    @LocalServerPort
    private int port;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    private TestRestTemplate restTemplate;

    private static void setLatestResponse(ResponseEntity<String> latestResponse) {
        BaseCucumberCore.latestResponse = latestResponse;
    }

    @PostConstruct
    public void init() {
        // https://stackoverflow.com/questions/16748969/java-net-httpretryexception-cannot-retry-due-to-server-authentication-in-strea
        // https://github.com/spring-projects/spring-framework/issues/14004
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR;
            }
        });
    }

    /**
     * Execute a GET call to the defined REST URL
     *
     * @param path   URI path to call.
     */
    @When("executing a GET call to {string}")
    public void whenExecutingAGETCallToPath(final String path) {
        final String url = fullURLFor(path);
        final HttpHeaders headers = createHTTPHeader(false);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));
    }

    /**
     * Execute an authorized GET call to the defined URL
     */
    @When("executing an authorized GET call to {string}")
    public void whenExecutingAnAuthorizedGETCallToUrl(final String path) {
        final String url = fullURLFor(path);
        final HttpHeaders headers = createHTTPHeader(true);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));
    }

    /**
     * Execute a POST call to the defined URL with body from file
     */
    @When("executing a POST call to {string} with the body from file {string}")
    public void whenExecutingAPOSTCallToPathWithBodyFromFile(String path, String pathToFile) throws Exception {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(false);

        String body = readFile(pathToFile);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class));
    }

    /**
     * Execute an authorized POST call to the defined URL with body from file
     */
    @When("executing an authorized POST call to {string} with the body from file {string}")
    public void whenExecutingAnAuthorizedPOSTCallToPathWithBodyFromFile(String path, String pathToFile) throws Exception {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(true);

        String body = readFile(pathToFile);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class));
    }

    /**
     * Execute an authorized POST call to the defined URL with body from file and dynamic URL elements.
     * <p>!!!ATTENTION!!! This is an Anti-Pattern, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     * <br />
     * <p>
     *     DataTable looks like:
     *     <pre>
     *     | URI Elements  | URI Values |
     *     | resourceId    | abc-def-gh |
     *     | subResourceId | zyx-wvu-ts |
     *     </pre>
     * </p>
     * <br />
     * <p>Structure:</p>
     * <ul>
     *     <li>First line is the header.</li>
     *     <li>'URI Elements' is the element name of the dynamic URI path without {}.</li>
     *     <li>'URI Values' will be evaluated first from the scenario context.
     *     If context var was not found use value directly</li>
     * </ul>
     */
    @When("executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'")
    public void whenExecutingAnAuthorizedPOSTCallToPathWithBodyAndDynamicURLElement(DataTable dataTable) throws Exception {
        if (dataTable == null) {
            throw new IllegalArgumentException("Dynamic URL parts are null");
        } else {
            // Prepare request
            String path = uriPath;
            String body = editableBody;

            // assert that given for path and body was previously done
            Assert.assertNotNull("No given path found", path);
            Assert.assertNotNull("No given body found", body);

            // Read datatable
            List<Map<String, String>> dataTableRowList = dataTable.asMaps(String.class, String.class);

            for (Map<String, String> stringStringMap : dataTableRowList) {
                // Try to resolve value from context map
                String uriValue = testContextMap.get(stringStringMap.get("URI Values"));
                // If context map knows nothing about the value, use value directly
                if (uriValue == null) {
                    uriValue = stringStringMap.get("URI Values");
                }
                // replace path with URI key and URI value
                path = path.replace(
                        "{" + stringStringMap.get("URI Elements") + "}",
                        uriValue
                );
            }
            HttpHeaders headers = createHTTPHeader(true);

            setLatestResponse(restTemplate.exchange(fullURLFor(path), HttpMethod.POST, new HttpEntity<>(body, headers), String.class));
        }
    }

    /**
     * Execute an authorized POST call to an URL with previously given body
     */
    @When("executing an authorized POST call to {string} with previously given body")
    public void whenExecutingAnAuthorizedPOSTCallToWithGivenBody(String path) {
        Assert.assertNotNull("No body was previously given!", editableBody);
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(true);

        setLatestResponse(restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(editableBody, headers), String.class));
    }

    /**
     * Execute a PUT call to a path with body from file
     */
    @When("executing a PUT call to {string} with the body from file {string}")
    public void whenExecuteAPUTCallToPathWithBodyFromFile(String path, String pathToFile) throws Exception {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(false);

        String body = readFile(pathToFile);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class));
    }

    /**
     * Execute an authorized PUT call to a path with body from file
     */
    @When("executing an authorized PUT call to {string} with the body from file {string}")
    public void whenExecuteAnAuthorizedPUTCallToPathWithBodyFromFile(String path, String pathToFile) throws Exception {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(true);

        String body = readFile(pathToFile);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class));
    }

    /**
     * Execute a PUT call to a path with previously given body
     */
    @When("executing a PUT call to {string} with previously given body")
    public void whenExecutingAPUTCallToPathWithGivenBody(String path) {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(false);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(editableBody, headers), String.class));
    }

    /**
     * Execute a PUT call to a path with previously given body
     */
    @When("executing an authorized PUT call to {string} with previously given body")
    public void whenExecutingAnAuthorizedPUTCallToPathWithGivenBody(String path) {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(true);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(editableBody, headers), String.class));
    }

    /**
     * Execute a DELETE call to a path
     */
    @When("executing a DELETE call to {string}")
    public void whenExecuteADeleteCallTo(String path) {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(false);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class));
    }

    /**
     * Execute an authorized DELETE call to a path
     */
    @When("executing an authorized DELETE call to {string}")
    public void whenExecutingAnAuthorizedDELETECallTo(String path) {
        String url = fullURLFor(path);
        HttpHeaders headers = createHTTPHeader(true);
        setLatestResponse(restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class));
    }

    /**
     * set the value of the previously given body property to a new value
     */
    @When("I set the value of the previously given body property {string} to {string}")
    public void whenISetTheValueOfTheBodyPropertyTo(String propertyPath, String value) {
        String safePropertyPath = "/" + propertyPath.replace(".", "/");

        if ("null".equals(value)) {
            editableBody = JsonUtils.removeJsonField(editableBody, safePropertyPath);
        } else if (value.matches("\\d* characters")) {
            Integer numOfChars = Integer.parseInt(value.split(" ")[0]);
            String newValue = StringUtils.rightPad("", numOfChars, "1234567890");
            editableBody = JsonUtils.editJsonField(editableBody, safePropertyPath, newValue);
        } else {
            editableBody = JsonUtils.editJsonField(editableBody, safePropertyPath, value);
        }
    }

    /**
     * Create HTTP header
     *
     * @param addAuthorisation  true = add BearerToken | false = no Authorization header
     * @return  default Headers
     */
    private HttpHeaders createHTTPHeader(boolean addAuthorisation) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Accept-Language", "en");
        headers.add("Content-Type", "application/json");

        if (addAuthorisation) {
            headers.add("Authorization", "Bearer " + bearerToken);
        }

        return headers;
    }

    /**
     * Full URL for URI path
     *
     * @param path Path of URI
     * @return  full URL as protocol:server_host:port/basePath/path
     */
    private String fullURLFor(String path) {
        return SERVER_URL + port + urlBasePath + path;
    }
}
