package com.ragin.bdd.cucumber.core;

import com.ragin.bdd.cucumber.utils.JsonUtils;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

public class BaseCucumberCore extends Loggable {
    /**
     * Handle BearerToken
     *
     * @param defaultBearerToken default Bearer token
     */
    @Value("${cucumberTest.authorization.bearerToken.default}")
    public void setDefaultBearerToken(String defaultBearerToken) {
        ScenarioStateContext.current().setDefaultBearerToken(defaultBearerToken);
        if (StringUtils.isEmpty(ScenarioStateContext.current().getBearerToken())) {
            ScenarioStateContext.current().setBearerToken(defaultBearerToken);
        }
    }

    /**
     * Read file and return content as String
     *
     * @param path          Path to file
     * @return              Content of file as String
     * @throws Exception    Error while reading file
     */
    protected String readFile(String path) throws Exception {
        String name = ScenarioStateContext.current().getFileBasePath() + path;
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(name);
        if (resourceAsStream == null) {
            throw new FileNotFoundException("Could not find the file " + name + " in the class path.");
        }

        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
    }

    /**
     * Assert that JSONs are equal
     *
     * @param expected  expected JSON
     * @param actual    actual JSON
     */
    protected void assertJSONisEqual(String expected, String actual) {
        JsonUtils.assertJsonEquals(expected, actual);
    }
}
