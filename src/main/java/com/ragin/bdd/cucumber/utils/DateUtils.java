package com.ragin.bdd.cucumber.utils;

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;

@SuppressWarnings({"WeakerAccess", "squid:S1192"}) // Methods are public to be used by the project.
@CommonsLog
public final class DateUtils {
    /**
     * available dateFormats
     */
    private static final Map<String, DateTimeFormatter> dateFormats = createDateList();

    /**
     * Check, that mandatory date is valid.
     * <p>Transforms date also if it is in another timezone</p>
     *
     * @param dateObject    Object with date
     * @return              true = valid | false = invalid
     */
    public static boolean isValidMandatoryDate(final Object dateObject,
                                               final Collection<BddCucumberDateTimeFormat> bddDateTimeFormats) {
        return transformToLocalDateTime(dateObject, bddDateTimeFormats) != null;
    }

    private static Map<String, DateTimeFormatter> createDateList() {
        final Map<String, DateTimeFormatter> dateTimeFormatterMap = new HashMap<>();
        dateTimeFormatterMap.put("yyyy-MM-dd", DateTimeFormatter.ISO_LOCAL_DATE);
        dateTimeFormatterMap.put("yyyy-MM-dd HH:mm:ss.SSS000", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS000"));
        dateTimeFormatterMap.put("yyyy-MM-dd HH:mm:ss.SSS000+HH:mm", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        dateTimeFormatterMap.put("yyyy-MM-dd'T'HH:mm:ss.SSS", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        dateTimeFormatterMap.put("yyyy-MM-dd HH:mm:ss.SSS", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        dateTimeFormatterMap.put("yyyy-MM-dd HH:mm:ss.SS", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"));
        dateTimeFormatterMap.put("yyyy-MM-dd HH:mm:ss.S", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        dateTimeFormatterMap.put("yyyy-MM-dd HH:mm:ss.SSSSSS", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        return dateTimeFormatterMap;
    }

    /**
     * transform object to LocalDateTime
     *
     * @param dateObject    object with possible date
     * @param bddDateTimeFormats Datetime formatter
     * @return              LocalDateTime if valid date, else null
     * @throws DateTimeParseException   Exception, when date is not parseable
     */
    private static LocalDateTime transformToLocalDateTime(
            final Object dateObject,
            final Collection<BddCucumberDateTimeFormat> bddDateTimeFormats) {
        LocalDateTime localDateTime = null;

        if (dateObject instanceof Long) {
            // Read Long (timestamp) with LocalDateTime to ensure that it is a valid date
            return LocalDateTime.of(
                    Instant.ofEpochMilli((Long) dateObject).atZone(ZoneId.of("Europe/Berlin")).toLocalDate(),
                    LocalTime.MIN
            );
        } else if (dateObject instanceof BigDecimal) {
            // Read BigDecimal (e.g. yyyyddmm) with LocalDateTime to ensure that it is a valid date
            return LocalDateTime.of(
                    Instant.ofEpochMilli(((BigDecimal) dateObject).longValue()).atZone(ZoneId.of("Europe/Berlin")).toLocalDate(),
                    LocalTime.MIN);
        } else if (dateObject instanceof String) {
            // Parse string with date to ensure that it is a valid date
            // first, check, that it not contains sth. like "null"
            if ("null".equalsIgnoreCase(dateObject.toString())) {
                return null;
            }

            // check known date formats if one fits to the object
            for (Map.Entry<String, DateTimeFormatter> entry : createDateFormatters(bddDateTimeFormats).entrySet()) {
                log.debug("Try to parse " + dateObject.toString() + " with the format " + entry.getKey());
                localDateTime = parseDate(dateObject.toString(), entry);

                // if parsing date was null, parse String as dateTime
                if (null == localDateTime) {
                    localDateTime = parseDateTime(dateObject.toString(), entry);
                }

                // if it is not null the String was parsed and is valid!
                if (null != localDateTime) {
                    break;
                }
            }

            // seems not to be a valid date
            if (null == localDateTime) {
                throw new DateTimeParseException("No parser for " + dateObject.toString(), dateObject.toString(), 0);
            }
        }
        return localDateTime;
    }

    /**
     * Try to parse String as date to LocalDateTime
     *
     * @param date      date as string
     * @param entry     entry which contains format (for logging) and DateTimeFormatter for parsing
     * @return          LocalDateTime if valid, null if not parseable
     */
    private static LocalDateTime parseDate(final String date, final Map.Entry<String, DateTimeFormatter> entry) {
        try {
            return LocalDateTime.of(LocalDate.parse(date, entry.getValue()), LocalTime.MIN);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse as date " + date + " with the format " + entry.getKey());
            return null;
        }
    }

    /**
     * Try to parse String as date to LocalDateTime
     *
     * @param dateTime  datetime as string
     * @param entry     entry which contains format (for logging) and DateTimeFormatter for parsing
     * @return          LocalDateTime if valid, null if not parseable
     */
    private static LocalDateTime parseDateTime(final String dateTime, final Map.Entry<String, DateTimeFormatter> entry) {
        try {
            return LocalDateTime.parse(dateTime, entry.getValue());
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse as dateTime " + dateTime + " with the format " + entry.getKey());
            return null;
        }
    }

    private static Map<String, DateTimeFormatter> createDateFormatters(
            final Collection<BddCucumberDateTimeFormat> bddDateTimeFormats
    ) {
        final Map<String, DateTimeFormatter> dateTimeFormatsResult = dateFormats;

        if (bddDateTimeFormats != null && ! bddDateTimeFormats.isEmpty()) {
            bddDateTimeFormats.forEach(bddDateTimeFormat -> {
                // Pattern
                if (! bddDateTimeFormat.pattern().isEmpty()) {
                    bddDateTimeFormat.pattern().forEach(
                            pattern -> dateTimeFormatsResult.put(
                                    pattern, DateTimeFormatter.ofPattern(pattern))
                    );
                }
            });
        }
        return dateTimeFormatsResult;
    }

    private DateUtils() {}
}
