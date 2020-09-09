package com.ragin.bdd.cucumber.matcher;

import org.hamcrest.BaseMatcher;

public interface BddCucumberJsonMatcher {
    String matcherName();
    Class<? extends BaseMatcher<?>> matcherClass();
}
