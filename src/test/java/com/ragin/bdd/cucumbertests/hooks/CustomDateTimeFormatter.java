package com.ragin.bdd.cucumbertests.hooks;

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat;
import java.util.Collections;
import java.util.List;

/**
 * Example to add a date with "yyyy.MM.dd" format.
 */
public class CustomDateTimeFormatter implements BddCucumberDateTimeFormat {
    @Override
    public List<String> pattern() {
        return Collections.singletonList(
                "yyyy.MM.dd"
        );
    }
}
