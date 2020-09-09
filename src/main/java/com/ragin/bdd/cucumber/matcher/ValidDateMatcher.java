package com.ragin.bdd.cucumber.matcher;

import com.ragin.bdd.cucumber.utils.DateUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

/**
 * Matcher for valid date
 */
@Component
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
