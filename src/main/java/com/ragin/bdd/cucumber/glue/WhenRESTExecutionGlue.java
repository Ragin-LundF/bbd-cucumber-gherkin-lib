package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.java.en.When;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * This class contains common <code>When</code> execution of REST related steps.
 */
public class WhenRESTExecutionGlue extends BaseRESTExecutionGlue {
    /**
     * set the value of the previously given body property to a new value
     *
     * @param propertyPath  path to the JSON property
     * @param value         new value this element
     */
    @When("I set the value of the previously given body property {string} to {string}")
    public void whenISetTheValueOfTheBodyPropertyTo(final String propertyPath, final String value) {
        // fetch new value from state context if possible. Else use the given value
        String newValue = ScenarioStateContext.current().getScenarioContextMap().get(value);
        if (newValue == null) {
            newValue = value;
        }

        if ("null".equals(newValue)) {
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.removeJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            propertyPath
                    )
            );
        } else if (newValue.matches("\\d* bdd_lib_numbers")) {
            final int numOfChars = Integer.parseInt(newValue.split(" ")[0]);
            newValue = StringUtils.rightPad("", numOfChars, "1234567890");
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.editJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            propertyPath,
                            newValue
                    )
            );
        } else if (newValue.matches("bdd_lib_uuid")) {
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.editJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            propertyPath,
                            UUID.randomUUID().toString()
                    )
            );
        } else {
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.editJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            propertyPath,
                            newValue
                    )
            );
        }
    }

    /**
     * Set a header to a specific value.
     *
     * @param header        Header name
     * @param headerValue   Header value
     */
    @When("I set the header {string} to {string}")
    public void whenISetTheHeaderValueTo(final String header, final String headerValue) {
        ScenarioStateContext.current().getHeaderValues().put(header, headerValue);
    }
}
