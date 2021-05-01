package com.ragin.bdd.cucumbertests.library.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomDateTime {
    @GetMapping("/api/v1/customDateTime")
    public ResponseEntity<String> stubCustomDateTime() {
        return ResponseEntity.status(HttpStatus.OK).body(createCustomDateTime());
    }

    @GetMapping("/api/v1/date/{pastFuture}/{dateSelector}/{amount}")
    public ResponseEntity<String> stubDateDaysInPastFuture(
            @PathVariable("pastFuture") final String pastFuture,
            @PathVariable("dateSelector") final String dateSelector,
            @PathVariable("amount") final Integer amount
    ) {
        final LocalDate now = LocalDate.now();
        String result = null;
        if (pastFuture.equalsIgnoreCase("past")) {
            if (dateSelector.equalsIgnoreCase("days")) {
                result = createDateJson(now.minusDays(amount).format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else if (dateSelector.equalsIgnoreCase("months")) {
                result = createDateJson(now.minusMonths(amount).format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else if (dateSelector.equalsIgnoreCase("years")) {
                result = createDateJson(now.minusYears(amount).format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        } else if (pastFuture.equalsIgnoreCase("future")) {
            if (dateSelector.equalsIgnoreCase("days")) {
                result = createDateJson(now.plusDays(amount).format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else if (dateSelector.equalsIgnoreCase("months")) {
                result = createDateJson(now.plusMonths(amount).format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else if (dateSelector.equalsIgnoreCase("years")) {
                result = createDateJson(now.plusYears(amount).format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/v1/datetime/{pastFuture}/{dateSelector}/{amount}")
    public ResponseEntity<String> stubDateTimeDaysInPastFuture(
            @PathVariable("pastFuture") final String pastFuture,
            @PathVariable("dateSelector") final String dateSelector,
            @PathVariable("amount") final Integer amount
    ) {
        final LocalDateTime now = LocalDateTime.now();
        String result = null;
        if (pastFuture.equalsIgnoreCase("past")) {
            if (dateSelector.equalsIgnoreCase("days")) {
                result = createDateJson(now.minusDays(amount).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else if (dateSelector.equalsIgnoreCase("months")) {
                result = createDateJson(now.minusMonths(amount).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else if (dateSelector.equalsIgnoreCase("years")) {
                result = createDateJson(now.minusYears(amount).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        } else if (pastFuture.equalsIgnoreCase("future")) {
            if (dateSelector.equalsIgnoreCase("days")) {
                result = createDateJson(now.plusDays(amount).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else if (dateSelector.equalsIgnoreCase("months")) {
                result = createDateJson(now.plusMonths(amount).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else if (dateSelector.equalsIgnoreCase("years")) {
                result = createDateJson(now.plusYears(amount).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }
        return ResponseEntity.ok(result);
    }

    private String createDateJson(final String date) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date", date);
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }

    private String createCustomDateTime() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("validDate", "2020.04.30");
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
