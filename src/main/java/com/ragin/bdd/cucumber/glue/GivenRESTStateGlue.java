package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.BaseCucumberCore;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
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
    public void givenThatAllFilePathsAreRelativeTo(String basePath) {
        fileBasePath = basePath;
    }

    /**
     * Defines the base URL path to which other URLs are relative
     */
    @Given("that all URL's are relative to {string}")
    public void givenThatAllURLSAreRelativeTo(String basePath) {
        urlBasePath = basePath;
    }

    /**
     * Defines to use a bearer token without scopes
     */
    @Given("that a bearer token without scopes is used")
    public void givenThatBearerTokenWithoutScopesIsUsed() {
        bearerToken = noScopeBearerToken;
    }

    @Given("that the API path is {string}")
    public void givenThatAPIPathIs(String apiPath) {
        uriPath = apiPath;
    }

    /**
     * Define to use a file as the body
     */
    @Given("that the file {string} is used as the body")
    public void givenThatTheFileIsUsedAsTheBody(String pathToFile) throws Exception {
        editableBody = readFile(pathToFile);
    }
}
