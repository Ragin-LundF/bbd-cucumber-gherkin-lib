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
        final String safePropertyPath = "/" + propertyPath.replace(".", "/");

        if ("null".equals(value)) {
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.removeJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            safePropertyPath
                    )
            );
        } else if (value.matches("\\d* characters")) {
            int numOfChars = Integer.parseInt(value.split(" ")[0]);
            String newValue = StringUtils.rightPad("", numOfChars, "1234567890");
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.editJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            safePropertyPath,
                            newValue
                    )
            );
        } else {
            ScenarioStateContext.current().setEditableBody(
                    jsonUtils.editJsonField(
                            ScenarioStateContext.current().getEditableBody(),
                            safePropertyPath,
                            value
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
