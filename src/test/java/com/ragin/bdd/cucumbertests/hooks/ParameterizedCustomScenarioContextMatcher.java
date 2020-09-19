package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ParameterizedCustomScenarioContextMatcher extends BaseMatcher<Object> implements BddCucumberJsonMatcher, ParametrizedMatcher {
    private String jsonParameter;

    @Override
    public String matcherName() {
        return "isInContextAvailable";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }

    @Override
    public void setParameter(String parameter) {
        this.jsonParameter = parameter;
    }

    @Override
    public boolean matches(final Object actual) {
        final String actualParameter = String.valueOf(actual);
        final String contextParameter = ScenarioStateContext
                .current()
                .getScenarioContextMap()
                .get(jsonParameter);
        return actualParameter.equals(contextParameter);
    }

    @Override
    public void describeTo(Description description) {
    }
}
