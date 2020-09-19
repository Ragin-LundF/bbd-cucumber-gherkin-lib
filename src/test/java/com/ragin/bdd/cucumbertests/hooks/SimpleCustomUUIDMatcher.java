package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.lang.NonNull;

public class SimpleCustomUUIDMatcher extends BaseMatcher<String> implements BddCucumberJsonMatcher {
    @Override
    public String matcherName() {
        return "isUUID";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }

    @Override
    public boolean matches(final @NonNull Object actual) {
        if (actual instanceof String) {
            String actualString = String.valueOf(actual);
            return actualString.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
    }
}
