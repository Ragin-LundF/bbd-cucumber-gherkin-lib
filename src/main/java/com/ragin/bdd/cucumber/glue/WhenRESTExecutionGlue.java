package com.ragin.bdd.cucumber.glue;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import com.ragin.bdd.cucumber.utils.JsonUtils;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class contains common <code>When</code> execution of REST related steps.
 */
public class WhenRESTExecutionGlue extends BaseRESTExecutionGlue {
    @Autowired
    protected JsonUtils jsonUtils;

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
        } else if (newValue.matches("\\d* characters")) {
            final int numOfChars = Integer.parseInt(newValue.split(" ")[0]);
            newValue = StringUtils.rightPad("", numOfChars, "1234567890");
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.editJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            propertyPath,
                            newValue
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
