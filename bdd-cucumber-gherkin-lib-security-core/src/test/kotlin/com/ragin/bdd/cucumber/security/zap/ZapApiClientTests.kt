package com.ragin.bdd.cucumber.security.zap

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The client against a local stub of the ZAP API, so the exact query each call sends is visible
 * without a container.
 */
internal class ZapApiClientTests {
    // one entry per request, in order; separate lists because Konsist allows no helper class in a test file
    private val paths = mutableListOf<String>()
    private val params = mutableListOf<Map<String, String>>()
    private val hosts = mutableListOf<String?>()
    private var responseBody = "{}"
    private lateinit var server: HttpServer
    private lateinit var client: ZapApiClient

    @BeforeTest
    fun startStub() {
        server = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/") { exchange ->
                paths += exchange.requestURI.path
                params += parse(query = exchange.requestURI.rawQuery)
                hosts += exchange.requestHeaders.getFirst(ZapContainer.HOST_HEADER)
                val body = responseBody.toByteArray()
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            start()
        }
        client = ZapApiClient(apiBaseUrl = { "http://localhost:${server.address.port}" })
    }

    @AfterTest
    fun stopStub() {
        server.stop(0)
    }

    @Test
    internal fun `global alert filter marks the rule as false positive`() {
        client.addGlobalAlertFilter(ruleId = "40042")

        assertEquals(expected = listOf("/JSON/alertFilter/action/addGlobalAlertFilter/"), actual = paths)
        assertEquals(
            expected = listOf(mapOf("ruleId" to "40042", "newLevel" to "-1", "enabled" to "true")),
            actual = params
        )
        assertEquals(expected = listOf<String?>(ZapContainer.API_HOST), actual = hosts)
    }

    @Test
    internal fun `global alert filter fails when ZAP rejects it`() {
        responseBody = """{"code":"no_implementor","message":"No Implementor"}"""

        val failure = assertFailsWith<IllegalStateException> { client.addGlobalAlertFilter(ruleId = "40042") }

        assertEquals(
            expected = true,
            actual = failure.message!!.contains(other = "/JSON/alertFilter/action/addGlobalAlertFilter/")
        )
    }

    @Test
    internal fun `report leaves out false positives`() {
        val containerPath = client.generateReport(title = "Scan", template = "traditional-html", fileName = "r.html")

        assertEquals(expected = listOf("/JSON/reports/action/generate/"), actual = paths)
        assertEquals(
            expected = mapOf(
                "title" to "Scan",
                "template" to "traditional-html",
                "reportFileName" to "r.html",
                "reportDir" to "/home/zap",
                "includedConfidences" to "Low|Medium|High|Confirmed"
            ),
            actual = params.single()
        )
        assertEquals(expected = "/home/zap/r.html", actual = containerPath)
    }

    private fun parse(query: String?): Map<String, String> {
        if (query.isNullOrEmpty()) {
            return emptyMap()
        }
        return query.split("&").associate { pair ->
            val (key, value) = pair.split("=", limit = 2)
            key to URLDecoder.decode(value, StandardCharsets.UTF_8)
        }
    }
}
