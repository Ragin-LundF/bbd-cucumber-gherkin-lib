package com.ragin.bdd.cucumber.security.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ScannerPropertiesTests {
    @Test
    internal fun `a request body size of zero is rejected`() {
        assertFailsWith<IllegalArgumentException> { ScannerProperties(maxRequestBodySize = 0) }
    }

    @Test
    internal fun `a negative response body size is rejected`() {
        assertFailsWith<IllegalArgumentException> { ScannerProperties(maxResponseBodySize = -1) }
    }

    @Test
    internal fun `a body size of one byte is accepted`() {
        val properties = ScannerProperties(maxRequestBodySize = 1, maxResponseBodySize = 1)

        assertEquals(expected = 1, actual = properties.maxRequestBodySize)
        assertEquals(expected = 1, actual = properties.maxResponseBodySize)
    }
}
