package com.ragin.bdd.cucumber.datetimeformat

import java.time.format.DateTimeFormatter

fun interface BddCucumberDateTimeFormat {
    /**
     * Pattern to add to the Date(Time)Parser.
     *
     * @return List of patterns to add
     */
    fun formatters(): List<DateTimeFormatter>?
}
