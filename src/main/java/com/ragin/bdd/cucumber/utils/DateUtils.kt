package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import org.apache.commons.logging.LogFactory
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.util.*

// Methods are public to be used by the project.
object DateUtils {
    /**
     * available dateFormats
     */
    private val dateFormats = createDateList()
    private val log = LogFactory.getLog(DateUtils::class.java)

    /**
     * Check, that mandatory date is valid.
     *
     * Transforms date also if it is in another timezone
     *
     * @param dateObject    Object with date
     * @param bddDateTimeFormats Collection of BddCucumberDateFormat classes
     * @return              true = valid | false = invalid
     */
    @JvmStatic
    fun isValidMandatoryDate(dateObject: Any,
                             bddDateTimeFormats: Collection<BddCucumberDateTimeFormat>): Boolean {
        return transformToLocalDateTime(dateObject, bddDateTimeFormats) != null
    }

    /**
     * create a list of default date and datetime formats.
     *
     * @return  List with default date/time formatters
     */
    private fun createDateList(): List<DateTimeFormatter> {
        return listOf(
                DateTimeFormatter.ISO_DATE,
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatterBuilder()
                        .append(DateTimeFormatter.ISO_LOCAL_DATE)
                        .appendLiteral(' ')
                        .append(DateTimeFormatter.ISO_LOCAL_TIME)
                        .toFormatter())
    }

    /**
     * Transform object to LocalDateTime.
     *
     * @param dateObject    object with possible date
     * @param bddDateTimeFormats Datetime formatter
     * @return              LocalDateTime if valid date, else null
     * @throws DateTimeParseException   Exception, when date is not parseable
     */
    private fun transformToLocalDateTime(
            dateObject: Any,
            bddDateTimeFormats: Collection<BddCucumberDateTimeFormat>): LocalDateTime? {
        var localDateTime: LocalDateTime? = null
        if (dateObject is Long) {
            // Read Long (timestamp) with LocalDateTime to ensure that it is a valid date
            return LocalDateTime.of(
                    Instant.ofEpochMilli(dateObject).atZone(ZoneId.of("Europe/Berlin")).toLocalDate(),
                    LocalTime.MIN
            )
        } else if (dateObject is BigDecimal) {
            // Read BigDecimal (e.g. yyyyddmm) with LocalDateTime to ensure that it is a valid date
            return LocalDateTime.of(
                    Instant.ofEpochMilli(dateObject.toLong()).atZone(ZoneId.of("Europe/Berlin")).toLocalDate(),
                    LocalTime.MIN)
        } else if (dateObject is String) {
            // Parse string with date to ensure that it is a valid date
            // first, check, that it not contains sth. like "null"
            if ("null".equals(dateObject.toString(), ignoreCase = true)) {
                return null
            }

            // check known date formats if one fits to the object
            for (formatter in createDateFormatters(bddDateTimeFormats)) {
                log.debug("Try to parse $dateObject with the format $formatter")
                localDateTime = parseDate(dateObject.toString(), formatter)

                // if parsing date was null, parse String as dateTime
                if (null == localDateTime) {
                    localDateTime = parseDateTime(dateObject.toString(), formatter)
                }

                // if it is not null the String was parsed and is valid!
                if (null != localDateTime) {
                    break
                }
            }

            // seems not to be a valid date
            if (null == localDateTime) {
                throw DateTimeParseException("No parser for $dateObject", dateObject.toString(), 0)
            }
        }
        return localDateTime
    }

    /**
     * Try to parse String as date to LocalDateTime
     *
     * @param date          date as string
     * @param formatter     entry which contains format (for logging) and DateTimeFormatter for parsing
     * @return              LocalDateTime if valid, null if not parseable
     */
    private fun parseDate(date: String, formatter: DateTimeFormatter): LocalDateTime? {
        return try {
            LocalDateTime.of(LocalDate.parse(date, formatter), LocalTime.MIN)
        } catch (e: DateTimeParseException) {
            log.debug("Failed to parse as date $date with the format $formatter")
            null
        }
    }

    /**
     * Try to parse String as date to LocalDateTime
     *
     * @param dateTime      datetime as string
     * @param formatter     entry which contains format (for logging) and DateTimeFormatter for parsing
     * @return              LocalDateTime if valid, null if not parseable
     */
    private fun parseDateTime(dateTime: String, formatter: DateTimeFormatter): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateTime, formatter)
        } catch (e: DateTimeParseException) {
            log.debug("Failed to parse as dateTime $dateTime with the format $formatter")
            null
        }
    }

    /**
     * Create a set of all DateTimeFormatters (default + custom).
     *
     * @param bddDateTimeFormats    Collection of custom DateTimeFormatters
     * @return                      Set which contains all DateTimeFormatters
     */
    private fun createDateFormatters(bddDateTimeFormats: Collection<BddCucumberDateTimeFormat>): Set<DateTimeFormatter> {
        val dateTimeFormatsResult: MutableSet<DateTimeFormatter> = LinkedHashSet(dateFormats)
        Optional.ofNullable(bddDateTimeFormats).ifPresent {
            formats: Collection<BddCucumberDateTimeFormat> ->
            formats.stream()
                    .map { obj: BddCucumberDateTimeFormat -> obj.formatters() }
                    .filter { obj: List<DateTimeFormatter?>? -> Objects.nonNull(obj) }
                    .flatMap { obj: List<DateTimeFormatter?>? -> obj?.stream() }
                    .forEach { e: DateTimeFormatter? -> dateTimeFormatsResult.add(e!!) }
        }
        return dateTimeFormatsResult
    }
}