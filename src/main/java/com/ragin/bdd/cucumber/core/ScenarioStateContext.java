package com.ragin.bdd.cucumber.core;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public class ScenarioStateContext extends Loggable {
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

    public ResponseEntity<String> getLatestResponse() {
        return latestResponse;
    }

    public void setLatestResponse(ResponseEntity<String> latestResponse) {
        this.latestResponse = latestResponse;
    }

    public String getFileBasePath() {
        return fileBasePath;
    }

    public void setFileBasePath(String fileBasePath) {
        this.fileBasePath = fileBasePath;
    }

    public String getUrlBasePath() {
        return urlBasePath;
    }

    public void setUrlBasePath(String urlBasePath) {
        this.urlBasePath = urlBasePath;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getEditableBody() {
        return editableBody;
    }

    public void setEditableBody(String editableBody) {
        this.editableBody = editableBody;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public String getDefaultBearerToken() {
        return defaultBearerToken;
    }

    public void setDefaultBearerToken(String defaultBearerToken) {
        this.defaultBearerToken = defaultBearerToken;
    }

    public Map<String, String> getHeaderValues() {
        return headerValues;
    }

    public void setHeaderValues(Map<String, String> headerValues) {
        this.headerValues = headerValues;
    }

    public Map<String, String> getScenarioContextMap() {
        return scenarioContextMap;
    }

    public void setScenarioContextMap(Map<String, String> scenarioContextMap) {
        this.scenarioContextMap = scenarioContextMap;
    }
}
