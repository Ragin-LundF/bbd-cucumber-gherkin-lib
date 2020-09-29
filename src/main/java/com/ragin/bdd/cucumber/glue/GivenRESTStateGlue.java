package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class contains steps to edit the test state.
 */
public class GivenRESTStateGlue extends BaseCucumberCore {

    @Value("${cucumberTest.authorization.bearerToken.noscope}")
    private String noScopeBearerToken;

    /**
     * Defines the base path to which other paths are relative
     *
     * @param basePath of files
     */
    @Given("that all file paths are relative to {string}")
    public void givenThatAllFilePathsAreRelativeTo(final @NotNull String basePath) {
        ScenarioStateContext.current().setFileBasePath(basePath);
    }

    /**
     * Defines the base URL path to which other URLs are relative
     *
     * @param basePath  of the URLs
     */
    @Given("that all URLs are relative to {string}")
    public void givenThatAllURLsAreRelativeTo(final @NotNull String basePath) {
        ScenarioStateContext.current().setUrlBasePath(basePath);
    }

    /**
     * Set a list of users and tokens.
     *
     * @param userDataTable     data table which contains the user in the first row and token in the second
     */
    @Given("that the following users and tokens are existing")
    public void givenThatUsersAndTokensExisting(final DataTable userDataTable) {
        final Map<String, String> userDataMap = userDataTable.asMap(String.class, String.class);
        final Set<String> keySet = userDataMap.keySet();
        for (String key : keySet) {
            ScenarioStateContext.current().getUserTokenMap().put(key, userDataMap.get(key));
        }
    }

    /**
     * Defines to use a bearer token without scopes
     */
    @Given("that the user is {string}")
    public void givenThatTheUserIs(final String user) {
        ScenarioStateContext.current().setBearerToken(ScenarioStateContext.current().getUserTokenMap().get(user));
    }

    /**
     * Defines to use a bearer token without scopes
     */
    @Given("that a bearer token without scopes is used")
    public void givenThatBearerTokenWithoutScopesIsUsed() {
        ScenarioStateContext.current().setBearerToken(noScopeBearerToken);
    }

    /**
     * Define to use a API path for a concrete endpoint
     *
     * @param apiPath   path of the endpoint
     */
    @Given("that the API path is {string}")
    public void givenThatAPIPathIs(final @NotNull String apiPath) {
        ScenarioStateContext.current().setUriPath(apiPath);
    }

    /**
     * Define to use a file as the body
     *
     * @param pathToFile path to file for body
     * @throws java.io.IOException error while reading file
     */
    @Given("that the file {string} is used as the body")
    public void givenThatTheFileIsUsedAsTheBody(final @NotNull String pathToFile) throws IOException {
        ScenarioStateContext.current().setEditableBody(readFile(pathToFile));
    }

    /**
     * Define to use a body directly
     *
     * @param body of the next request
     */
    @Given("^that the body of the request is$")
    public void givenThatTheBodyOfRequestIs(final @NotNull String body) {
        ScenarioStateContext.current().setEditableBody(body);
    }

    /**
     * Offers the possibility to set static values to the context
     *
     * @param key       Key in the context map
     * @param value     Value that should be used
     */
    @Given("that the context contains the key {string} with the value {string}")
    public void givenThatContextContainsKeyValue(final @NotNull String key, final @NotNull String value) {
        ScenarioStateContext.current().getScenarioContextMap().put(key, value);
    }

    /**
     * With this sentence it is possible to add static values to the context map.
     *
     * <p>
     * DataTable looks like:
     * <pre>
     * | key | value |
     * | resourceId    | abc-def-gh |
     * | subResourceId | zyx-wvu-ts |
     * </pre>
     *
     * @param dataTable  DataTable with the key "key" and value "value"
     */
    @Given("that the context contains the following 'key' and 'value' pairs")
    public void givenThatContextContainsKeyValuePairFromDataTable(final DataTable dataTable) {
        final Map<String, String> contextDataTableMap = dataTable.asMap(String.class, String.class);
        final Set<String> keySet = contextDataTableMap.keySet();
        for (String key : keySet) {
            ScenarioStateContext.current().getScenarioContextMap().put(key, contextDataTableMap.get(key));
        }
    }

    /**
     * Configure the JSON assertion, that arrays can contain more elements than the expected JSON has
     */
    @Given("that the response JSON can contain arrays with extra elements")
    public void givenThatJsonCanContainExtraArrayElements() {
        ScenarioStateContext.current().addJsonIgnoringExtraArrayElements();
    }

    /**
     * Configure the JSON assertion, that arrays can contain more fields than the expected JSON has
     */
    @Given("that the response JSON can contain extra fields")
    public void givenThatJsonCanContainExtraFields() {
        ScenarioStateContext.current().addJsonIgnoringExtraFields();
    }

    /**
     * Configure the JSON assertion, that arrays have a different order than the expected JSON has
     */
    @Given("that the response JSON can contain arrays in different order")
    public void givenThatJsonHasDifferentArrayOrders() {
        ScenarioStateContext.current().addJsonIgnoringArrayOrder();
    }

    @Given("that the stored data in the scenario context map has been reset")
    public void givenThatStoredDataInContextMapHasBeenReset() {
        ScenarioStateContext.current().setScenarioContextMap(new HashMap<>());
    }
}
