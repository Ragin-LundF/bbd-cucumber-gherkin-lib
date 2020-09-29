package com.ragin.bdd.cucumber.glue;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.junit.Assert;

/**
 * This class contains HTTP response validations.
 */
@SuppressWarnings("squid:S5960")
public class ThenRESTValidationGlue extends BaseCucumberCore {
    /**
     * Ensure, that the response code is valid
     * @param expectedStatusCode HTTP status code that is expected
     */
    @Then("I ensure that the status code of the response is {int}")
    public void thenEnsureThatTheStatusCodeOfTheResponseIs(final @NotNull Integer expectedStatusCode) {
        Assert.assertEquals(
                expectedStatusCode,
                Integer.valueOf(ScenarioStateContext.current().getLatestResponse().getStatusCode().value())
        );
    }

    /**
     * Ensure, that the response is equal to a file.
     * <p>This evaluates also JSON unit statements</p>
     * <p>see https://github.com/lukas-krecan/JsonUnit</p>
     *
     * @param pathToFile path to file to read
     * @throws java.io.IOException error while reading file
     */
    @Then("I ensure that the body of the response is equal to the file {string}")
    public void thenEnsureTheBodyOfTheResponseIsEqualToTheFile(final @NotNull String pathToFile) throws IOException {
        final String expectedBody = readFile(pathToFile);
        assertJSONisEqual(
                expectedBody,
                ScenarioStateContext.current().getLatestResponse().getBody()
        );
    }

    /**
     * Ensure, that the response is equal to a directly given body.
     * <p>This evaluates also JSON unit statements</p>
     * <p>see https://github.com/lukas-krecan/JsonUnit</p>
     *
     * @param expectedBody expected JSON body
     */
    @Then("^I ensure that the body of the response is equal to$")
    public void thenEnsureTheBodyOfTheResponseIsEqualTo(final @NotNull String expectedBody) {
        assertJSONisEqual(
                expectedBody,
                ScenarioStateContext.current().getLatestResponse().getBody()
        );
    }

    @Then("I ensure that the body of the response contains a field {string} with the value {string}")
    public void thenEnsureTheBodyOfTheResponseContainsFieldWithValue(final @NotNull String fieldName, final String value) {
        jsonUtils.validateJsonField(ScenarioStateContext.current().getLatestResponse().getBody(), fieldName, value);
    }

    @Then("I ensure that the body of the response contains the following fields and values")
    public void thenEnsureTheBodyOfTheResponseContainsFieldWithValues(@NotNull final DataTable dataTable) {
        final Map<String, String> contextDataTableMap = dataTable.asMap(String.class, String.class);
        final Set<String> keySet = contextDataTableMap.keySet();
        for (String key : keySet) {
            jsonUtils.validateJsonField(ScenarioStateContext.current().getLatestResponse().getBody(), key, contextDataTableMap.get(key));
        }
    }

    /**
     * Ensure, that the response code and body is equal to directly given status code and body
     * <p>This evaluates also JSON unit statements</p>
     * <p>see https://github.com/lukas-krecan/JsonUnit</p>
     *
     * @param expectedStatusCode expected response status code
     * @param expectedBody expected JSON body
     */
    @Then("I ensure that the response code is {int} and the body is equal to")
    public void thenEnsureTheResponseCodeAndBodyIsEqualTo(final @NotNull Integer expectedStatusCode, final @NotNull String expectedBody) {
        Assert.assertEquals(
                expectedStatusCode,
                Integer.valueOf(ScenarioStateContext.current().getLatestResponse().getStatusCode().value())
        );
        assertJSONisEqual(
                expectedBody,
                ScenarioStateContext.current().getLatestResponse().getBody()
        );
    }

    /**
     * Ensure, that the response code and body is equal to directly given status code and body as file
     * <p>This evaluates also JSON unit statements</p>
     * <p>see https://github.com/lukas-krecan/JsonUnit</p>
     *
     * @param expectedStatusCode expected response status code
     * @param pathToFile expected file that contains the JSON body
     * @throws java.io.IOException if the file can not be read
     */
    @Then("I ensure that the response code is {int} and the body is equal to the file {string}")
    public void thenEnsureTheResponseCodeAndBodyAsFileIsEqualTo(
            final @NotNull Integer expectedStatusCode,
            final @NotNull String pathToFile
    ) throws IOException {
        final String expectedBody = readFile(pathToFile);
        Assert.assertEquals(
                expectedStatusCode,
                Integer.valueOf(ScenarioStateContext.current().getLatestResponse().getStatusCode().value())
        );
        assertJSONisEqual(
                expectedBody,
                ScenarioStateContext.current().getLatestResponse().getBody()
        );
    }

    /**
     * Store a string of the response to the context to reuse them later
     * <p>!!!ATTENTION!!! This is an Anti-Pattern, but sometimes it can be necessary if Cucumber should work as Test-Suite.</p>
     * <p>For better separation use the database initializer and use static values instead of transporting them between scenarios!</p>
     *
     * @param fieldName     name of the field in the response
     * @param contextName   name of the field as which it should be stored (should be unique, else it will be overwritten)
     */
    @Then("I store the string of the field {string} in the context {string} for later usage")
    public void storeStringOfFieldInContextForLaterUsage(
            final @NotNull String fieldName,
            final @NotNull String contextName
    ) {
        Assert.assertNotNull(fieldName);
        Assert.assertNotNull(contextName);
        Assert.assertNotNull(
                "response was null!",
                ScenarioStateContext.current().getLatestResponse()
        );
        Assert.assertNotNull(
                "body of response was null",
                ScenarioStateContext.current().getLatestResponse().getBody()
        );

        String jsonPath = fieldName;
        if (! jsonPath.startsWith("$.")) {
            jsonPath = "$." + jsonPath;
        }
        final DocumentContext documentContext = JsonPath.parse(ScenarioStateContext.current().getLatestResponse().getBody());
        final String field = documentContext.read(jsonPath, String.class);
        ScenarioStateContext.current().getScenarioContextMap().put(
                replaceTrailingAndLeadingQuotes(contextName),
                field
        );
    }

    /**
     * Replace trailing and leading quotes
     *
     * @param value     String value
     * @return          argument without trailing and leading quotes
     */
    private String replaceTrailingAndLeadingQuotes(final String value) {
        String result = value;
        if (result.startsWith("\"")) {
            result = result.replaceFirst("\"", "");
        }
        if (result.endsWith("\"")) {
            result = result.replaceAll("\"$", "");
        }
        return result;
    }
}
