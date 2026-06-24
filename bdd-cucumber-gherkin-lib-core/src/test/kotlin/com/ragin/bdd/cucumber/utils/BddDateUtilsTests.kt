package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.datetimeformat.BddCucumberDateTimeFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BddDateUtilsTests {

    private val noFormats: Collection<BddCucumberDateTimeFormat> = emptyList()

    @Test
    internal fun `ISO date string is parsed to LocalDateTime at midnight`() {
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = "2024-03-15",
            bddDateTimeFormats = noFormats
        )
        assertEquals(
            expected = LocalDateTime.of(2024, 3, 15, 0, 0),
            actual = result
        )
    }

    @Test
    internal fun `ISO datetime string date portion is extracted and time is normalized to midnight`() {
        // parseDate with ISO_DATE_TIME extracts LocalDate from the full string; time is discarded to LocalTime.MIN
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = "2024-03-15T10:30:00",
            bddDateTimeFormats = noFormats
        )
        assertEquals(
            expected = LocalDateTime.of(2024, 3, 15, 0, 0),
            actual = result
        )
    }

    @Test
    internal fun `space-separated datetime string date portion is extracted and time is normalized to midnight`() {
        // parseDate with the custom space formatter extracts LocalDate; time is discarded to LocalTime.MIN
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = "2024-03-15 10:30:00",
            bddDateTimeFormats = noFormats
        )
        assertEquals(
            expected = LocalDateTime.of(2024, 3, 15, 0, 0),
            actual = result
        )
    }

    @Test
    internal fun `null string returns null`() {
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = "null",
            bddDateTimeFormats = noFormats
        )
        assertNull(actual = result)
    }

    @Test
    internal fun `NULL string is case-insensitively treated as null and returns null`() {
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = "NULL",
            bddDateTimeFormats = noFormats
        )
        assertNull(actual = result)
    }

    @Test
    internal fun `unparseable string throws DateTimeParseException`() {
        assertFailsWith<DateTimeParseException> {
            BddDateUtils.transformToLocalDateTime(
                dateObject = "2025-20-20",
                bddDateTimeFormats = noFormats
            )
        }
    }

    @Test
    internal fun `Long epoch zero is converted to 1970-01-01 in Europe-Berlin timezone`() {
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = 0L,
            bddDateTimeFormats = noFormats
        )
        assertEquals(
            expected = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIN),
            actual = result
        )
    }

    @Test
    internal fun `BigDecimal epoch zero is converted to 1970-01-01 in Europe-Berlin timezone`() {
        val result = BddDateUtils.transformToLocalDateTime(
            dateObject = BigDecimal.ZERO,
            bddDateTimeFormats = noFormats
        )
        assertEquals(
            expected = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIN),
            actual = result
        )
    }

    @Test
    internal fun `isValidMandatoryDate returns true for valid ISO date string`() {
        assertTrue(
            actual = BddDateUtils.isValidMandatoryDate(
                dateObject = "2024-03-15",
                bddDateTimeFormats = noFormats
            )
        )
    }

    @Test
    internal fun `isValidMandatoryDate returns false for null string`() {
        assertFalse(
            actual = BddDateUtils.isValidMandatoryDate(
                dateObject = "null",
                bddDateTimeFormats = noFormats
            )
        )
    }

    @Test
    internal fun `isValidMandatoryDate returns true for valid Long timestamp`() {
        assertTrue(
            actual = BddDateUtils.isValidMandatoryDate(
                dateObject = 0L,
                bddDateTimeFormats = noFormats
            )
        )
    }
}
