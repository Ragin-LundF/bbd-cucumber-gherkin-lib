package com.ragin.bdd.cucumber.datetimeformat;

import java.time.format.DateTimeFormatter;
import java.util.List;

@FunctionalInterface
public interface BddCucumberDateTimeFormat {
    /**
     * Pattern to add to the Date(Time)Parser.
     *
     * @return List of patterns to add
     */
    List<DateTimeFormatter> formatters();
}
