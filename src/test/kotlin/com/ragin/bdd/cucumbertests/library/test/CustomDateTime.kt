package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class CustomDateTime {
    @GetMapping("/api/v1/customDateTime")
    fun stubCustomDateTime(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(createCustomDateTime())
    }

    @GetMapping("/api/v1/date/{pastFuture}/{dateSelector}/{amount}")
    fun stubDateDaysInPastFuture(
        @PathVariable("pastFuture") pastFuture: String,
        @PathVariable("dateSelector") dateSelector: String,
        @PathVariable("amount") amount: Int
    ): ResponseEntity<String> {
        val now = LocalDate.now()
        var result: String? = null
        if (pastFuture.equals("past", ignoreCase = true)) {
            if (dateSelector.equals("days", ignoreCase = true)) {
                result = createDateJson(now.minusDays(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
            } else if (dateSelector.equals("months", ignoreCase = true)) {
                result = createDateJson(now.minusMonths(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
            } else if (dateSelector.equals("years", ignoreCase = true)) {
                result = createDateJson(now.minusYears(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
        } else if (pastFuture.equals("future", ignoreCase = true)) {
            if (dateSelector.equals("days", ignoreCase = true)) {
                result = createDateJson(now.plusDays(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
            } else if (dateSelector.equals("months", ignoreCase = true)) {
                result = createDateJson(now.plusMonths(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
            } else if (dateSelector.equals("years", ignoreCase = true)) {
                result = createDateJson(now.plusYears(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/api/v1/datetime/{pastFuture}/{dateSelector}/{amount}")
    fun stubDateTimeDaysInPastFuture(
        @PathVariable("pastFuture") pastFuture: String,
        @PathVariable("dateSelector") dateSelector: String,
        @PathVariable("amount") amount: Int
    ): ResponseEntity<String> {
        val now = LocalDateTime.now()
        var result: String? = null
        if (pastFuture.equals("past", ignoreCase = true)) {
            if (dateSelector.equals("days", ignoreCase = true)) {
                result = createDateJson(now.minusDays(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            } else if (dateSelector.equals("months", ignoreCase = true)) {
                result = createDateJson(now.minusMonths(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            } else if (dateSelector.equals("years", ignoreCase = true)) {
                result = createDateJson(now.minusYears(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }
        } else if (pastFuture.equals("future", ignoreCase = true)) {
            if (dateSelector.equals("days", ignoreCase = true)) {
                result = createDateJson(now.plusDays(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            } else if (dateSelector.equals("months", ignoreCase = true)) {
                result = createDateJson(now.plusMonths(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            } else if (dateSelector.equals("years", ignoreCase = true)) {
                result = createDateJson(now.plusYears(amount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }
        }
        return ResponseEntity.ok(result)
    }

    private fun createDateJson(date: String): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("date", date)
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }

    private fun createCustomDateTime(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("validDate", "2020.04.30")
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }
}
