package com.ragin.bdd.cucumber.security.zap

import com.ragin.bdd.cucumber.security.config.AlertFilterProperties
import com.ragin.bdd.cucumber.security.config.AlertFilterRisk
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
        client.addGlobalAlertFilter(filter = AlertFilterProperties(ruleId = "40042"))

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

        val failure = assertFailsWith<IllegalStateException> {
            client.addGlobalAlertFilter(filter = AlertFilterProperties(ruleId = "40042"))
        }

        assertEquals(
            expected = true,
            actual = failure.message!!.contains(other = "/JSON/alertFilter/action/addGlobalAlertFilter/")
        )
    }

    @Test
    internal fun `global alert filter maps the automation framework fields onto the API names`() {
        client.addGlobalAlertFilter(
            filter = AlertFilterProperties(
                ruleId = "10038",
                ruleName = "Content Security Policy (CSP) Header Not Set",
                newRisk = AlertFilterRisk.LOW,
                url = ".*/actuator/.*",
                urlRegex = true,
                parameter = "id",
                parameterRegex = true,
                attack = "<script>",
                attackRegex = true,
                evidence = "Server: .*",
                evidenceRegex = true,
                methods = listOf("GET", "POST")
            )
        )

        assertEquals(
            expected = listOf(
                mapOf(
                    "ruleId" to "10038",
                    "newLevel" to "1",
                    "enabled" to "true",
                    "url" to ".*/actuator/.*",
                    "urlIsRegex" to "true",
                    "parameter" to "id",
                    "parameterIsRegex" to "true",
                    "attack" to "<script>",
                    "attackIsRegex" to "true",
                    "evidence" to "Server: .*",
                    "evidenceIsRegex" to "true",
                    "methods" to "GET,POST"
                )
            ),
            actual = params
        )
    }

    @Test
    internal fun `global alert filter sends a matcher without its regex flag when it is a plain string`() {
        client.addGlobalAlertFilter(
            filter = AlertFilterProperties(ruleId = "10038", url = "http://localhost/actuator/health")
        )

        assertEquals(
            expected = listOf(
                mapOf(
                    "ruleId" to "10038",
                    "newLevel" to "-1",
                    "enabled" to "true",
                    "url" to "http://localhost/actuator/health"
                )
            ),
            actual = params
        )
    }

    @Test
    internal fun `global alert filter sends the ZAP level of every new risk`() {
        AlertFilterRisk.entries.forEach { risk ->
            client.addGlobalAlertFilter(filter = AlertFilterProperties(ruleId = "10038", newRisk = risk))
        }

        assertEquals(
            expected = listOf("-1", "0", "1", "2", "3"),
            actual = params.map { it.getValue("newLevel") }
        )
    }

    @Test
    internal fun `disables the scan rules as one comma separated list`() {
        client.disableScanRules(ruleIds = listOf("40026", "40027"))

        assertEquals(expected = listOf("/JSON/ascan/action/disableScanners/"), actual = paths)
        assertEquals(expected = listOf(mapOf("ids" to "40026,40027")), actual = params)
        assertEquals(expected = listOf<String?>(ZapContainer.API_HOST), actual = hosts)
    }

    @Test
    internal fun `disabling scan rules fails when ZAP rejects it`() {
        responseBody = """{"code":"does_not_exist","message":"Does Not Exist"}"""

        val failure = assertFailsWith<IllegalStateException> { client.disableScanRules(ruleIds = listOf("40026")) }

        assertEquals(
            expected = true,
            actual = failure.message!!.contains(other = "/JSON/ascan/action/disableScanners/")
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
