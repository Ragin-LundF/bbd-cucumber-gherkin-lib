package com.ragin.bdd.cucumber.matcher;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

@Component
public class ScenarioStateContextMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
    private String parameter;

    @Override
    public boolean matches(final Object actual) {
        final String parameterFromContext = ScenarioStateContext.current().getScenarioContextMap().get(parameter);
        final String actualAsString = String.valueOf(actual);
        return (actualAsString.equals(parameterFromContext));
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("The actual value is not equal to the parameter [" + parameter + "]");
    }

    @Override
    public void describeMismatch(final Object item, final Description description) {
        description
                .appendText("BDD Context value was [")
                .appendValue(ScenarioStateContext.current().getScenarioContextMap().get(parameter))
                .appendText("].")
                .appendText(" JSON Value was [")
                .appendValue(item)
                .appendText("]");
        super.describeMismatch(item, description);
    }

    @Override
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
