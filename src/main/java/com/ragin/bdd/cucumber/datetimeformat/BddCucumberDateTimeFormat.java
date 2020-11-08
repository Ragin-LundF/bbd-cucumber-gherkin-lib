package com.ragin.bdd.cucumber.datetimeformat;

import java.util.List;

public interface BddCucumberDateTimeFormat {
    /**
     * Pattern to add to the Date(Time)Parser.
     *
     * @return List of patterns to add
     */
    default List<String> pattern() {
        return null;
    }
}
