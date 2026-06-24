package com.ragin.bdd.cucumber.utils

import com.jayway.jsonpath.JsonPath
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class BddJsonUtilsTests {

    private val utils = BddJsonUtils(
        jsonMatcher = null,
        bddCucumberDateTimeFormatter = emptyList()
    )
    private val sampleJson = """{"name":"Alice","age":30}"""

    @BeforeTest
    private fun setUp() {
        ScenarioStateContext.defaultBearerToken = ""
        ScenarioStateContext.reset()
    }

    @Test
    internal fun `editJsonField with dollar-dot prefix sets field value`() {
        val result = utils.editJsonField(
            originalJson = sampleJson,
            fieldPath = "$.name",
            newValue = "Bob"
        )
        val name: String = JsonPath.read(result, "$.name")
        assertEquals(expected = "Bob", actual = name)
    }

    @Test
    internal fun `editJsonField without dollar-dot prefix auto-adds prefix`() {
        val result = utils.editJsonField(
            originalJson = sampleJson,
            fieldPath = "name",
            newValue = "Charlie"
        )
        val name: String = JsonPath.read(result, "$.name")
        assertEquals(expected = "Charlie", actual = name)
    }

    @Test
    internal fun `removeJsonField sets field value to null`() {
        val result = utils.removeJsonField(
            originalJson = sampleJson,
            fieldPath = "$.name"
        )
        val name: Any? = JsonPath.read(result, "$.name")
        assertNull(actual = name)
    }

    @Test
    internal fun `assertJsonEquals does not throw for equal JSON objects`() {
        utils.assertJsonEquals(
            expectedJSON = """{"name":"Alice","age":30}""",
            actualJSON = """{"name":"Alice","age":30}"""
        )
    }

    @Test
    internal fun `assertJsonEquals does not throw for equal JSON with different key order`() {
        utils.assertJsonEquals(
            expectedJSON = """{"age":30,"name":"Alice"}""",
            actualJSON = """{"name":"Alice","age":30}"""
        )
    }

    @Test
    internal fun `assertJsonEquals throws AssertionError for different JSON values`() {
        assertFailsWith<AssertionError> {
            utils.assertJsonEquals(
                expectedJSON = """{"name":"Alice"}""",
                actualJSON = """{"name":"Bob"}"""
            )
        }
    }
}
