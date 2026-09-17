package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
