package com.ragin.bdd.cucumber.rest.utils

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UrlUtilsTests {

    @BeforeTest
    fun setUp() {
        ScenarioStateContext.defaultBearerToken = ""
        ScenarioStateContext.reset()
        ScenarioStateContext.scenarioContextMap.clear()
    }

    @Test
    internal fun `appendPathElements adds separator when neither base nor next has one`() {
        assertEquals(
            expected = "base/path",
            actual = UrlUtils.appendPathElements("base", "path")
        )
    }

    @Test
    internal fun `appendPathElements does not duplicate separator when base ends with slash`() {
        assertEquals(
            expected = "base/path",
            actual = UrlUtils.appendPathElements("base/", "path")
        )
    }

    @Test
    internal fun `appendPathElements does not duplicate separator when next starts with slash`() {
        assertEquals(
            expected = "base/path",
            actual = UrlUtils.appendPathElements("base", "/path")
        )
    }

    @Test
    internal fun `appendPathElements removes duplicate separator when both sides have slash`() {
        assertEquals(
            expected = "base/path",
            actual = UrlUtils.appendPathElements("base/", "/path")
        )
    }

    @Test
    internal fun `appendPathElements ignores null elements`() {
        assertEquals(
            expected = "base",
            actual = UrlUtils.appendPathElements("base", null)
        )
    }

    @Test
    internal fun `appendPathElements ignores empty elements`() {
        assertEquals(
            expected = "base",
            actual = UrlUtils.appendPathElements("base", "")
        )
    }

    @Test
    internal fun `appendPathElements chains multiple segments correctly`() {
        assertEquals(
            expected = "base/path/sub",
            actual = UrlUtils.appendPathElements("base", "/path", "/sub")
        )
    }

    @Test
    internal fun `appendPathElements returns unchanged path when all extended paths are null`() {
        assertEquals(
            expected = "base/path",
            actual = UrlUtils.appendPathElements("base/path", null, null)
        )
    }

    @Test
    internal fun `appendPathElements returns empty string when base and next are both empty`() {
        assertEquals(
            expected = "",
            actual = UrlUtils.appendPathElements("", "")
        )
    }

    // --- fullURLFor ---

    @Test
    internal fun `fullURLFor returns path unchanged when it already starts with http`() {
        val absoluteUrl = "http://example.com/api"
        assertEquals(
            expected = absoluteUrl,
            actual = UrlUtils.fullURLFor(path = absoluteUrl)
        )
    }

    @Test
    internal fun `fullURLFor returns path unchanged when it already starts with https`() {
        val absoluteUrl = "https://example.com/api"
        assertEquals(
            expected = absoluteUrl,
            actual = UrlUtils.fullURLFor(path = absoluteUrl)
        )
    }

    @Test
    internal fun `fullURLFor constructs full URL with protocol host and port`() {
        assertEquals(
            expected = "http://localhost:8080/api",
            actual = UrlUtils.fullURLFor(
                path = "/api",
                protocol = "http",
                host = "localhost",
                port = "8080"
            )
        )
    }

    @Test
    internal fun `fullURLFor omits port when port is null`() {
        assertEquals(
            expected = "http://localhost/api",
            actual = UrlUtils.fullURLFor(
                path = "/api",
                protocol = "http",
                host = "localhost",
                port = null
            )
        )
    }

    @Test
    internal fun `fullURLFor omits port when port is empty string`() {
        assertEquals(
            expected = "http://localhost/api",
            actual = UrlUtils.fullURLFor(
                path = "/api",
                protocol = "http",
                host = "localhost",
                port = ""
            )
        )
    }

    @Test
    internal fun `fullURLFor omits port when port is blank`() {
        assertEquals(
            expected = "http://localhost/api",
            actual = UrlUtils.fullURLFor(
                path = "/api",
                protocol = "http",
                host = "localhost",
                port = "   "
            )
        )
    }

    @Test
    internal fun `fullURLFor builds URL without protocol prefix when protocol and host are null`() {
        assertEquals(
            expected = "/api/v1",
            actual = UrlUtils.fullURLFor(path = "/api/v1")
        )
    }

    @Test
    internal fun `fullURLFor uses urlBasePath from scenario context as middle segment`() {
        ScenarioStateContext.urlBasePath = "base"
        assertEquals(
            expected = "http://localhost:9090/base/api",
            actual = UrlUtils.fullURLFor(
                path = "/api",
                protocol = "http",
                host = "localhost",
                port = "9090"
            )
        )
    }

    @Test
    internal fun `fullURLFor uses https protocol`() {
        assertEquals(
            expected = "https://secure.host:443/v1",
            actual = UrlUtils.fullURLFor(
                path = "/v1",
                protocol = "https",
                host = "secure.host",
                port = "443"
            )
        )
    }

    // --- replacePathPlaceholders ---

    @Test
    internal fun `replacePathPlaceholders substitutes single placeholder from context`() {
        ScenarioStateContext.scenarioContextMap["resourceId"] = "abc-123"
        assertEquals(
            expected = "/api/abc-123",
            actual = UrlUtils.replacePathPlaceholders(path = $$"/api/${resourceId}")
        )
    }

    @Test
    internal fun `replacePathPlaceholders substitutes multiple placeholders from context`() {
        ScenarioStateContext.scenarioContextMap["tenantId"] = "tenant-1"
        ScenarioStateContext.scenarioContextMap["itemId"] = "item-42"
        assertEquals(
            expected = "/tenants/tenant-1/items/item-42",
            actual = UrlUtils.replacePathPlaceholders(path = $$"/tenants/${tenantId}/items/${itemId}")
        )
    }

    @Test
    internal fun `replacePathPlaceholders returns path unchanged when no placeholders present`() {
        assertEquals(
            expected = "/api/v1/resource",
            actual = UrlUtils.replacePathPlaceholders(path = "/api/v1/resource")
        )
    }

    @Test
    internal fun `replacePathPlaceholders leaves placeholder intact when key is absent from context`() {
        assertEquals(
            expected = $$"/api/${unknownKey}",
            actual = UrlUtils.replacePathPlaceholders(path = $$"/api/${unknownKey}")
        )
    }

    @Test
    internal fun `replacePathPlaceholders returns empty string for empty path`() {
        assertEquals(
            expected = "",
            actual = UrlUtils.replacePathPlaceholders(path = "")
        )
    }
}
