package com.ragin.bdd.cucumber.core;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
public class ScenarioStateContext {
    private static ScenarioStateContext scenarioStateContext;
    private ResponseEntity<String> latestResponse = null;
    private String fileBasePath = "";
    private String urlBasePath = "";
    private String bearerToken = "";
    private String editableBody = "";
    private String uriPath = "";
    private String defaultBearerToken = "";
    private Map<String, String> headerValues = new HashMap<>();
    private Map<String, String> scenarioContextMap = new HashMap<>();

    private ScenarioStateContext() {}

    public static ScenarioStateContext current() {
        if (scenarioStateContext == null) {
            scenarioStateContext = new ScenarioStateContext();
        }
        return scenarioStateContext;
    }

    /**
     * Reset states
     */
    public void reset() {
        latestResponse = null;
        fileBasePath = "";
        urlBasePath = "";
        editableBody = "";
        headerValues = new HashMap<>();
        bearerToken = defaultBearerToken;
    }
}
