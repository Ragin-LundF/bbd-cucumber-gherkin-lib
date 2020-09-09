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
    public boolean matches(Object actual) {
        String parameterFromContext = ScenarioStateContext.current().getScenarioContextMap().get(parameter);
        String actualAsString = String.valueOf(actual);
        return (actualAsString.equals(parameterFromContext));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("The actual value is not equal to the parameter [" + parameter + "]");
    }

    @Override
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
