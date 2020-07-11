package com.ragin.bdd.cucumber.core;

import com.ragin.bdd.cucumber.utils.JsonUtils;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

public class BaseCucumberCore extends Loggable {
    public static ResponseEntity<String> latestResponse = null;
    protected static String fileBasePath = "";
    protected static String urlBasePath = "";
    protected static String bearerToken = "";
    protected static String editableBody = "";
    protected static String uriPath = "";
    private static String defaultBearerToken = "";
    private static Map<String, Runnable> rollBackConfigurations = new HashMap<>();
    public static Map<String, String> testContextMap = new HashMap<>();

    public static void addRollBackStep(String operation, Runnable rollback) {
        rollBackConfigurations.put(operation, rollback);
    }

    @Value("${cucumberTest.authorization.bearerToken.default}")
    public void setDefaultBearerToken(String defaultBearerToken) {
        BaseCucumberCore.defaultBearerToken = defaultBearerToken;
        if (StringUtils.isEmpty(bearerToken)) {
            bearerToken = defaultBearerToken;
        }
    }

    protected String readFile(String path) throws Exception {
        String name = fileBasePath + path;
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(name);
        if (resourceAsStream == null) {
            throw new FileNotFoundException("Could not find the file " + name + " in the class path.");
        }

        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
    }

    protected void compareJson(String expected, String actual) {
        JsonUtils.assertJsonEquals(expected, actual);
    }

    // Needs to be NON-static in order to ensure that "setDefaultBearerToken" is called after "@Value" has initialized the code.
    // If a better solution is found, this method can, and should be, static.
    public void resetState() {
        latestResponse = null;
        fileBasePath = "";
        urlBasePath = "";
        editableBody = "";
        bearerToken = defaultBearerToken;

        for (Map.Entry<String, Runnable> rollbackStep : rollBackConfigurations.entrySet()) {
            try {
                rollbackStep.getValue().run();
            } catch (Throwable e) {
                LOG.error("Rollback of configurations " + rollbackStep.getKey() + " failed. Next tests will probably report a strange result: STOP execution.", e);
                System.exit(-2);
            }
        }
        rollBackConfigurations = new HashMap<>();
    }
}
