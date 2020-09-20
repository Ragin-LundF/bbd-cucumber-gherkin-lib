package com.ragin.bdd.cucumber.matcher;

import com.ragin.bdd.cucumber.utils.DateUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

/**
 * Matcher for valid date
 */
@Component
public class UUIDMatcher extends BaseMatcher<Object> {
    public boolean matches(Object item) {
        if (item instanceof String) {
            final String actualString = String.valueOf(item);
            return actualString.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
        }
        return false;
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
