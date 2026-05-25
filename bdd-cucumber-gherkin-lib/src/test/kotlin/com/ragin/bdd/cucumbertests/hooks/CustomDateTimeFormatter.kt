package com.ragin.bdd.cucumbertests.hooks

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import java.time.format.DateTimeFormatter

/**
 * Example to add a date with "yyyy.MM.dd" format.
 */
class CustomDateTimeFormatter : BddCucumberDateTimeFormat {
    override fun formatters(): List<DateTimeFormatter> {
        return listOf(
            DateTimeFormatter.ofPattern("yyyy.MM.dd")
        )
    }
}
