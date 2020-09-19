package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils;
import io.cucumber.datatable.DataTable;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
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

public abstract class BaseRESTExecutionGlue extends BaseCucumberCore {
    protected static final String SERVER_URL = "http://localhost:";
    @LocalServerPort
    protected int port;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    protected TestRestTemplate restTemplate;

    protected void setLatestResponse(final ResponseEntity<String> latestResponse) {
        ScenarioStateContext.current().setLatestResponse(latestResponse);
    }

    @PostConstruct
    public void init() {
        // https://stackoverflow.com/questions/16748969/java-net-httpretryexception-cannot-retry-due-to-server-authentication-in-strea
        // https://github.com/spring-projects/spring-framework/issues/14004
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = response.getStatusCode();
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR;
            }
        });
    }
    /**
     * Executes a request with given httpMethod
     *
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request executed authorized or unauthorized (true = authorized)
     */
    protected void executeRequest(
            @NotNull final HttpMethod httpMethod,
            final boolean authorized
    ) {
        executeRequest(DataTable.emptyDataTable(), httpMethod, authorized);
    }

    /**
     * Executes a call with dynamic URL and replaces dynamic values with data from DataTable
     *
     * @param dataTable     DataTable which contains dynamic values mapping. If null, no URI parameter will be mapped.
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request executed authorized or unauthorized (true = authorized)
     */
    protected void executeRequest(
            @NotNull final DataTable dataTable,
            @NotNull final HttpMethod httpMethod,
            final boolean authorized
    ) {
        // Prepare path with dynamic URLs from datatable
        String path;
        if (! dataTable.isEmpty()) {
            path = RESTCommunicationUtils.prepareDynamicURLWithDataTable(dataTable);
        } else {
            path = ScenarioStateContext.current().getUriPath();
        }
        path = replacePathPlaceholders(path);

        // Prepare headers
        final HttpHeaders headers = RESTCommunicationUtils.createHTTPHeader(authorized);

        // create HttpEntity
        final String body = ScenarioStateContext.current().getEditableBody();
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        if (! StringUtils.isEmpty(body)) {
            // there was a body...replace with new entity with body
            httpEntity = new HttpEntity<>(body, headers);
        }

        setLatestResponse(
                restTemplate.exchange(
                        fullURLFor(path),
                        httpMethod,
                        httpEntity,
                        String.class
                )
        );
    }

    /**
     * Replace placeholder like ${myContextItem} with the available items from the context
     *
     * @param path  Path which can contain the placeholder
     * @return      replaced path
     */
    protected String replacePathPlaceholders(final String path) {
        // Build StringSubstitutor
        final StringSubstitutor sub = new StringSubstitutor(ScenarioStateContext.current().getScenarioContextMap());

        // Replace
        return sub.replace(path);
    }

    /**
     * Full URL for URI path
     *
     * @param path Path of URI
     * @return  full URL as protocol:server_host:port/basePath/path
     */
    protected String fullURLFor(final String path) {
        return SERVER_URL + port + ScenarioStateContext.current().getUrlBasePath() + path;
    }
}
