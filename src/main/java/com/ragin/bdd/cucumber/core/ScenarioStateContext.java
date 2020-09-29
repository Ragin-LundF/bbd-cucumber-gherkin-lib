package com.ragin.bdd.cucumber.core;

import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.javacrumbs.jsonunit.core.Option;
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
    private Map<String, String> userTokenMap = new HashMap<>();
    private List<Option> jsonPathOptions = new ArrayList<>(0);

    private ScenarioStateContext() {}

    public static ScenarioStateContext current() {
        if (scenarioStateContext == null) {
            scenarioStateContext = new ScenarioStateContext();
        }
        return scenarioStateContext;
    }

    /**
     * Add IGNORING_EXTRA_ARRAY_ITEMS option to the jsonPathOptions
     */
    public void addJsonIgnoringExtraArrayElements() {
        jsonPathOptions.add(IGNORING_EXTRA_ARRAY_ITEMS);
    }

    /**
     * Add IGNORING_EXTRA_FIELDS option to the jsonPathOptions
     */
    public void addJsonIgnoringExtraFields() {
        jsonPathOptions.add(IGNORING_EXTRA_FIELDS);
    }

    /**
     * Add IGNORING_ARRAY_ORDER option to the jsonPathOptions
     */
    public void addJsonIgnoringArrayOrder() {
        jsonPathOptions.add(IGNORING_ARRAY_ORDER);
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
        jsonPathOptions = new ArrayList<>(0);
        bearerToken = defaultBearerToken;
    }
}
