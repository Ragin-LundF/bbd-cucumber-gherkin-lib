package com.ragin.bdd.cucumber.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Matcher for valid date
 */
public class ValidDateMatcher extends BaseMatcher<Object> {
    public boolean matches(Object item) {
        return DateUtils.isValidMandatoryDate(item);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText("Is not a valid date: ").appendValue(item);
    }

    @Override
    public void describeTo(Description description) {
        // nothing to describe here
    }
}
