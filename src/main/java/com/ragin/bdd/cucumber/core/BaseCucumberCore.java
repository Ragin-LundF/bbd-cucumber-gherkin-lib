package com.ragin.bdd.cucumber.core;

import com.ragin.bdd.cucumber.utils.JsonUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class BaseCucumberCore {
    private static final String KEYWORD_ABSOLUTE_PATH = "absolutePath:";
    @Autowired
    protected JsonUtils jsonUtils;

    /**
     * Handle BearerToken
     *
     * @param defaultBearerToken default Bearer token
     */
    @Value("${cucumberTest.authorization.bearerToken.default:none}")
    public void setDefaultBearerToken(final String defaultBearerToken) {
        ScenarioStateContext.current().setDefaultBearerToken(defaultBearerToken);
        if (StringUtils.isEmpty(ScenarioStateContext.current().getBearerToken())) {
            ScenarioStateContext.current().setBearerToken(defaultBearerToken);
        }
    }

    /**
     * Read file and return content as String.
     * If the path contains the reserved word "absolutePath:" it tries to resolve the file from the classpath root.
     *
     * @param path              Path to file
     * @return                  Content of file as String
     * @throws IOException      Error while reading file
     */
    protected String readFile(final String path) throws IOException {
        final String name;
        if (path != null && path.startsWith(KEYWORD_ABSOLUTE_PATH)) {
            int prefixLength = KEYWORD_ABSOLUTE_PATH.length();
            if (path.startsWith(KEYWORD_ABSOLUTE_PATH + "/")) {
                prefixLength = prefixLength+1;
            }
            name = path.substring(prefixLength);
        } else {
            name = ScenarioStateContext.current().getFileBasePath() + path;
        }
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(name);
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
    protected void assertJSONisEqual(final String expected, final String actual) {
        jsonUtils.assertJsonEquals(expected, actual);
    }
}
