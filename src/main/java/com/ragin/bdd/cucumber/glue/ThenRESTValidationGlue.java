package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import io.cucumber.java.en.Then;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.validation.constraints.NotNull;
import org.junit.Assert;

/**
 * This class contains HTTP response validations.
 */
public class ThenRESTValidationGlue extends BaseCucumberCore {
    /**
     * Ensure, that the response code is valid
     */
    @Then("I ensure that the status code of the response is {int}")
    public void thenEnsureThatTheStatusCodeOfTheResponseIs(@NotNull final Integer expectedStatusCode) {
        Assert.assertEquals(Integer.valueOf(latestResponse.getStatusCode().value()), expectedStatusCode);
    }

    /**
     * Ensure, that the response is equal to a file.
     * <p>This evaluates also JSON unit statements</p>
     * <p>see https://github.com/lukas-krecan/JsonUnit</p>
     */
    @Then("I ensure that the body of the response is equal to the file {string}")
    public void thenEnsureTheBodyOfTheResponseIsEqualToTheFile(@NotNull final String pathToFile) throws Exception {
        String expectedBody = readFile(pathToFile);
        compareJson(latestResponse.getBody(), expectedBody);
    }

    /**
     * Ensure, that the response is equal to a file.
     * <p>This evaluates also JSON unit statements</p>
     * <p>see https://github.com/lukas-krecan/JsonUnit</p>
     */
    @Then("^I ensure that the body of the response is equal to$")
    public void thenEnsureTheBodyOfTheResponseIsEqualTo(@NotNull final String expectedBody) throws Exception {
        compareJson(latestResponse.getBody(), expectedBody);
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
    public void storeStringOfFieldInContextForLaterUsage(@NotNull final String fieldName, @NotNull final String contextName) throws Exception {
        Assert.assertNotNull(fieldName);
        Assert.assertNotNull(contextName);
        Assert.assertNotNull("response was null!", latestResponse);
        Assert.assertNotNull("body of response was null", latestResponse.getBody());

        JsonReader jsonReader = Json.createReader(new StringReader(latestResponse.getBody()));
        JsonObject json = jsonReader.readObject();
        jsonReader.close();

        testContextMap.put(replaceTrailingAndLeadingQuotes(contextName), json.getJsonString(fieldName).getString());
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
