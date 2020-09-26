package com.ragin.bdd.cucumber.matcher;

import org.hamcrest.BaseMatcher;

public interface BddCucumberJsonMatcher {
    String matcherName();

    @SuppressWarnings("squid:S1452")
    Class<? extends BaseMatcher<?>> matcherClass();
}
