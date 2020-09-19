package com.ragin.bdd.cucumbertests.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.json.JSONObject;

public class ParameterizedCustomMultiParameterMatcher extends BaseMatcher<Object> implements BddCucumberJsonMatcher, ParametrizedMatcher {
    private JsonParameter jsonParameter;

    @Override
    public String matcherName() {
        return "containsOneOf";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }

    @Override
    public void setParameter(String parameter) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.jsonParameter = objectMapper.readValue(parameter, JsonParameter.class);
        } catch (Exception e) {}
    }

    @Override
    public boolean matches(final Object actual) {
        final String actualParameter = String.valueOf(actual);
        if ((isFirstValid(actualParameter) || isSecondValid(actualParameter)) &&
                ! (isFirstValid(actualParameter) && isSecondValid(actualParameter))
        ) {
            return true;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
    }

    private boolean isFirstValid(final String actualParameter) {
        return actualParameter.equalsIgnoreCase(jsonParameter.getFirstArgument());
    }

    private boolean isSecondValid(final String actualParameter) {
        return actualParameter.equalsIgnoreCase(jsonParameter.getSecondArgument());
    }

    /**
     * Inner class for showcase
     */
    static class JsonParameter {
        private String firstArgument;
        private String secondArgument;

        public String getFirstArgument() {
            return firstArgument;
        }

        public void setFirstArgument(String firstArgument) {
            this.firstArgument = firstArgument;
        }

        public String getSecondArgument() {
            return secondArgument;
        }

        public void setSecondArgument(String secondArgument) {
            this.secondArgument = secondArgument;
        }
    }
}
