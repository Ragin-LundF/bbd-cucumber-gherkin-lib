package com.ragin.bdd.cucumber.utils;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat;

import lombok.extern.apachecommons.CommonsLog;

@SuppressWarnings({"WeakerAccess", "squid:S1192"}) // Methods are public to be used by the project.
@CommonsLog
public final class DateUtils {
    /**
     * available dateFormats
     */
    private static final List<DateTimeFormatter> dateFormats = createDateList();

    /**
     * Check, that mandatory date is valid.
     * <p>Transforms date also if it is in another timezone</p>
     *
     * @param dateObject    Object with date
     * @param bddDateTimeFormats Collection of BddCucumberDateFormat classes
     * @return              true = valid | false = invalid
     */
    public static boolean isValidMandatoryDate(final Object dateObject,
                                               final Collection<BddCucumberDateTimeFormat> bddDateTimeFormats) {
        return transformToLocalDateTime(dateObject, bddDateTimeFormats) != null;
    }

    /**
     * create a list of default date and datetime formats.
     *
     * @return  List with default date/time formatters
     */
    private static List<DateTimeFormatter> createDateList() {
        return Arrays.asList(
            ISO_DATE,
            ISO_DATE_TIME,
            new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .toFormatter());
    }

    /**
     * Transform object to LocalDateTime.
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
            for (DateTimeFormatter formatter : createDateFormatters(bddDateTimeFormats)) {
                log.debug("Try to parse " + dateObject.toString() + " with the format " + formatter);
                localDateTime = parseDate(dateObject.toString(), formatter);

                // if parsing date was null, parse String as dateTime
                if (null == localDateTime) {
                    localDateTime = parseDateTime(dateObject.toString(), formatter);
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
     * @param date          date as string
     * @param formatter     entry which contains format (for logging) and DateTimeFormatter for parsing
     * @return              LocalDateTime if valid, null if not parseable
     */
    private static LocalDateTime parseDate(final String date, final DateTimeFormatter formatter) {
        try {
            return LocalDateTime.of(LocalDate.parse(date, formatter), LocalTime.MIN);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse as date " + date + " with the format " + formatter);
            return null;
        }
    }

    /**
     * Try to parse String as date to LocalDateTime
     *
     * @param dateTime      datetime as string
     * @param formatter     entry which contains format (for logging) and DateTimeFormatter for parsing
     * @return              LocalDateTime if valid, null if not parseable
     */
    private static LocalDateTime parseDateTime(final String dateTime, final DateTimeFormatter formatter) {
        try {
            return LocalDateTime.parse(dateTime, formatter);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse as dateTime " + dateTime + " with the format " + formatter);
            return null;
        }
    }

    /**
     * Create a set of all DateTimeFormatters (default + custom).
     *
     * @param bddDateTimeFormats    Collection of custom DateTimeFormatters
     * @return                      Set which contains all DateTimeFormatters
     */
    private static Set<DateTimeFormatter> createDateFormatters(
            final Collection<BddCucumberDateTimeFormat> bddDateTimeFormats
    ) {
        final Set<DateTimeFormatter> dateTimeFormatsResult = new LinkedHashSet<>(dateFormats);
        ofNullable(bddDateTimeFormats).ifPresent(formats -> formats.stream()
                .map(BddCucumberDateTimeFormat::formatters)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(dateTimeFormatsResult::add));
        return dateTimeFormatsResult;
    }

    private DateUtils() {}
}
