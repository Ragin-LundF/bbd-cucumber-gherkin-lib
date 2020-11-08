package com.ragin.bdd.cucumber.matcher;

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat;
import com.ragin.bdd.cucumber.utils.DateUtils;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

/**
 * Matcher for valid date
 */
@Component
@RequiredArgsConstructor
public class ValidDateMatcher extends BaseMatcher<Object> {
    private final Collection<BddCucumberDateTimeFormat> dateTimeFormatCollection;

    public boolean matches(Object item) {
        return DateUtils.isValidMandatoryDate(item, dateTimeFormatCollection);
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
