package com.ragin.bdd.cucumber.core

import net.javacrumbs.jsonunit.core.Option
import org.springframework.http.ResponseEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScenarioStateContextTest {

    @BeforeTest
    fun setUp() {
        ScenarioStateContext.defaultBearerToken = ""
        ScenarioStateContext.reset()
        ScenarioStateContext.scenarioContextMap.clear()
    }

    @Test
    fun `reset clears latest response`() {
        ScenarioStateContext.latestResponse = ResponseEntity.ok("body")
        ScenarioStateContext.reset()
        assertNull(actual = ScenarioStateContext.latestResponse)
    }

    @Test
    fun `reset clears header values`() {
        ScenarioStateContext.headerValues["X-Custom"] = "value"
        ScenarioStateContext.reset()
        assertTrue(actual = ScenarioStateContext.headerValues.isEmpty())
    }

    @Test
    fun `reset sets bearer token to default bearer token`() {
        ScenarioStateContext.defaultBearerToken = "defaultToken"
        ScenarioStateContext.bearerToken = "sessionToken"
        ScenarioStateContext.reset()
        assertEquals(expected = "defaultToken", actual = ScenarioStateContext.bearerToken)
    }

    @Test
    fun `reset clears json path options`() {
        ScenarioStateContext.addJsonIgnoringExtraFields()
        ScenarioStateContext.reset()
        assertTrue(actual = ScenarioStateContext.getJsonPathOptions().isEmpty())
    }

    @Test
    fun `resolveEntry returns mapped value when key is present in context`() {
        ScenarioStateContext.scenarioContextMap["myKey"] = "myValue"
        assertEquals(
            expected = "myValue",
            actual = ScenarioStateContext.resolveEntry(key = "myKey")
        )
    }

    @Test
    fun `resolveEntry returns key itself when key is absent from context`() {
        assertEquals(
            expected = "unknownKey",
            actual = ScenarioStateContext.resolveEntry(key = "unknownKey")
        )
    }

    @Test
    fun `addJsonIgnoringExtraFields adds IGNORING_EXTRA_FIELDS option`() {
        ScenarioStateContext.addJsonIgnoringExtraFields()
        assertTrue(actual = ScenarioStateContext.getJsonPathOptions().contains(Option.IGNORING_EXTRA_FIELDS))
    }

    @Test
    fun `addJsonIgnoringExtraArrayElements adds IGNORING_EXTRA_ARRAY_ITEMS option`() {
        ScenarioStateContext.addJsonIgnoringExtraArrayElements()
        assertTrue(actual = ScenarioStateContext.getJsonPathOptions().contains(Option.IGNORING_EXTRA_ARRAY_ITEMS))
    }

    @Test
    fun `addJsonIgnoringArrayOrder adds IGNORING_ARRAY_ORDER option`() {
        ScenarioStateContext.addJsonIgnoringArrayOrder()
        assertTrue(actual = ScenarioStateContext.getJsonPathOptions().contains(Option.IGNORING_ARRAY_ORDER))
    }
}
