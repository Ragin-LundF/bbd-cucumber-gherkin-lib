package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.java.en.Given;
import java.io.IOException;
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
     */
    @Given("that all file paths are relative to {string}")
    public void givenThatAllFilePathsAreRelativeTo(@NotNull String basePath) {
        ScenarioStateContext.current().setFileBasePath(basePath);
    }

    /**
     * Defines the base URL path to which other URLs are relative
     */
    @Given("that all URLs are relative to {string}")
    public void givenThatAllURLsAreRelativeTo(@NotNull String basePath) {
        ScenarioStateContext.current().setUrlBasePath(basePath);
    }

    /**
     * Defines to use a bearer token without scopes
     */
    @Given("that a bearer token without scopes is used")
    public void givenThatBearerTokenWithoutScopesIsUsed() {
        ScenarioStateContext.current().setBearerToken(noScopeBearerToken);
    }

    @Given("that the API path is {string}")
    public void givenThatAPIPathIs(@NotNull String apiPath) {
        ScenarioStateContext.current().setUriPath(apiPath);
    }

    /**
     * Define to use a file as the body
     */
    @Given("that the file {string} is used as the body")
    public void givenThatTheFileIsUsedAsTheBody(@NotNull String pathToFile) throws IOException {
        ScenarioStateContext.current().setEditableBody(readFile(pathToFile));
    }

    /**
     * Define to use a body directly
     */
    @Given("^that the body of the response is$")
    public void givenThatTheBodyOfRequestIs(@NotNull final String body) {
        ScenarioStateContext.current().setEditableBody(body);
    }
}
