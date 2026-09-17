package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ScenarioReporterTests {

    @BeforeTest
    fun setUp() {
        ScenarioStateContext.scenario = null
    }

    @AfterTest
    fun tearDown() {
        ScenarioStateContext.scenario = null
    }

    @Test
    internal fun `reporting without a running scenario does nothing instead of failing`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging())

        reporter.summary(line = "some line")
        reporter.attachText(name = "text", text = "content")
        reporter.attachJson(name = "json", json = """{"a":1}""")
        reporter.attachHeaders(name = "headers", headers = mapOf("Accept" to listOf("application/json")))
        reporter.attachBody(name = "body", body = "content", contentSubtype = "json")
        reporter.attachComparison(expected = """{"a":1}""", actual = """{"a":2}""")
    }

    @Test
    internal fun `a null or empty body is not attached`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging())

        reporter.attachBody(name = "body", body = null, contentSubtype = "json")
        reporter.attachBody(name = "body", body = "", contentSubtype = "json")
    }

    // --- truncation: maxBodyLength is a documented guarantee ---

    @Test
    internal fun `a payload at the limit is attached whole`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(maxBodyLength = 10))
        val content = "0123456789"

        assertEquals(expected = content, actual = reporter.truncate(content = content))
    }

    @Test
    internal fun `a payload one character over the limit is cut and says how much is missing`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(maxBodyLength = 10))

        val truncated = reporter.truncate(content = "0123456789X")

        assertEquals(expected = "0123456789\n... truncated, 1 more characters", actual = truncated)
    }

    @Test
    internal fun `a long payload keeps exactly the configured number of characters`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(maxBodyLength = 100))

        val truncated = reporter.truncate(content = "x".repeat(n = 500))

        assertEquals(expected = 100, actual = truncated.substringBefore(delimiter = "\n").length)
        assertTrue(actual = truncated.endsWith(suffix = "truncated, 400 more characters"))
    }

    @Test
    internal fun `a limit of zero or less switches truncation off`() {
        val content = "x".repeat(n = 500)

        assertEquals(
            expected = content,
            actual = ScenarioReporter(options = BddProperties.Logging(maxBodyLength = 0)).truncate(content = content)
        )
        assertEquals(
            expected = content,
            actual = ScenarioReporter(options = BddProperties.Logging(maxBodyLength = -1)).truncate(content = content)
        )
    }

    // --- header obfuscation: getting this wrong leaks a credential ---

    @Test
    internal fun `the value of a sensitive header is obfuscated`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(headers = true))
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiZGVtbyJ9.c2ln"

        val rendered = reporter.obfuscate(headerName = "Authorization", values = listOf(token))

        assertFalse(actual = rendered.contains(other = token))
        assertTrue(actual = rendered.contains(other = "*"))
        assertEquals(expected = token.length, actual = rendered.length)
    }

    @Test
    internal fun `a sensitive header is recognised regardless of its casing`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(headers = true))

        for (name in listOf("authorization", "AUTHORIZATION", "Set-Cookie", "set-cookie", "X-Api-Key")) {
            val rendered = reporter.obfuscate(headerName = name, values = listOf("supersecretvalue"))
            assertTrue(actual = rendered.contains(other = "*"), message = name)
            assertFalse(actual = rendered.contains(other = "supersecretvalue"), message = name)
        }
    }

    @Test
    internal fun `an ordinary header keeps its value so the report stays useful`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(headers = true))

        assertEquals(
            expected = "application/json",
            actual = reporter.obfuscate(headerName = "Content-Type", values = listOf("application/json"))
        )
    }

    @Test
    internal fun `every value of a repeated sensitive header is obfuscated`() {
        val reporter = ScenarioReporter(options = BddProperties.Logging(headers = true))

        val rendered = reporter.obfuscate(
            headerName = "Set-Cookie",
            values = listOf("firstcookievalue", "secondcookievalue")
        )

        assertFalse(actual = rendered.contains(other = "firstcookievalue"))
        assertFalse(actual = rendered.contains(other = "secondcookievalue"))
    }

    // --- the formatting decisions, checked through the public pretty printer ---

    @Test
    internal fun `json is indented so it stays readable in a report and on a console`() {
        val formatted = BddJacksonUtils.prettyPrintOrRaw(json = """{"name":"John","ids":["a","b"]}""")

        assertTrue(actual = formatted.contains(other = "\n"))
        assertTrue(actual = formatted.contains(other = "\"name\" : \"John\""))
    }

    @Test
    internal fun `a payload that is not json is kept as it is`() {
        val broken = """{"name": "John"""

        assertEquals(expected = broken, actual = BddJacksonUtils.prettyPrintOrRaw(json = broken))
    }

    @Test
    internal fun `the default body limit is large enough for a realistic response`() {
        assertEquals(expected = 8192, actual = BddProperties.Logging().maxBodyLength)
    }

    @Test
    internal fun `headers are only reported when they were switched on`() {
        assertEquals(expected = false, actual = BddProperties.Logging().headers)
        assertEquals(expected = false, actual = BddProperties.Logging().sql)
        assertEquals(expected = true, actual = BddProperties.Logging().requestBody)
        assertEquals(expected = true, actual = BddProperties.Logging().responseBody)
        assertEquals(expected = true, actual = BddProperties.Logging().prettyJson)
    }
}
