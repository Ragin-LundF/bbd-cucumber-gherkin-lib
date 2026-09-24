package com.ragin.bdd.cucumber.security.zap

import com.ragin.bdd.cucumber.security.models.SecurityAlert
import com.ragin.bdd.cucumber.security.models.SecurityRisk
import io.github.oshai.kotlinlogging.KotlinLogging
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets

/**
 * Thin typed client for the subset of the ZAP REST API that the scan needs.
 *
 * ZAP answers on two prefixes: `/JSON/...` for the structured API and `/OTHER/...` for
 * endpoints that stream a document (recording export, reports).
 *
 * Every call carries `Host: zap`. ZAP serves the proxy and its own API on one port and uses that
 * header to decide which one is meant - without it the mapped Testcontainers port is read as a
 * proxy target and the call is forwarded instead of answered. See [ZapContainer.API_HOST].
 *
 * `Host` is a restricted header, so `jdk.httpclient.allowRestrictedHeaders=host` has to be set on
 * the test JVM before the first request. Without it the JDK drops the header silently and every
 * call here fails in a way that is hard to read.
 *
 * [apiBaseUrl] is read on every call because the container only knows its mapped port once it runs.
 */
class ZapApiClient(private val apiBaseUrl: () -> String) {
    constructor(container: ZapContainer) : this(apiBaseUrl = { container.apiBaseUrl })

    // HTTP/1.1 on purpose: the JDK client defaults to HTTP/2 and ZAP accepts the h2c upgrade on the first
    // call. From then on the connection speaks HTTP/2, which has no Host header - the JDK sends the
    // authority of the URL instead - so ZAP reads every later call as a proxy request and answers 502.
    private val http: HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

    fun version(): String {
        return json(path = "/JSON/core/view/version/").path("version").asString("")
    }

    /** Requires the `openapi` add-on. */
    fun importOpenApi(url: String, hostOverride: String? = null): JsonNode {
        return json(
            path = "/JSON/openapi/action/importUrl/",
            "url" to url,
            "hostOverride" to hostOverride
        )
    }

    /** Requires the `exim` add-on. */
    fun importHar(containerFilePath: String): JsonNode {
        return json(path = "/JSON/exim/action/importHar/", "filePath" to containerFilePath)
    }

    /**
     * Marks every alert of [ruleId] raised from now on as false positive. Requires the
     * `alertFilters` add-on, which `zap-stable` bundles.
     *
     * A filter only touches alerts raised after it exists, so it has to be in place before any
     * traffic is proxied or replayed.
     */
    fun addGlobalAlertFilter(ruleId: String): JsonNode {
        return json(
            path = "/JSON/alertFilter/action/addGlobalAlertFilter/",
            "ruleId" to ruleId,
            "newLevel" to FALSE_POSITIVE_LEVEL,
            "enabled" to "true"
        )
    }

    fun startActiveScan(url: String, recurse: Boolean, inScopeOnly: Boolean): String {
        return required(
            response = json(
                path = "/JSON/ascan/action/scan/",
                "url" to url,
                "recurse" to recurse.toString(),
                "inScopeOnly" to inScopeOnly.toString()
            ),
            field = "scan"
        )
    }

    fun activeScanStatus(scanId: String): Int {
        return required(
            response = json(path = "/JSON/ascan/view/status/", "scanId" to scanId),
            field = "status"
        ).toInt()
    }

    fun passiveScanRecordsToScan(): Int {
        return required(
            response = json(path = "/JSON/pscan/view/recordsToScan/"),
            field = "recordsToScan"
        ).toInt()
    }

    /** All alerts below [baseUrl], read page by page because ZAP caps the page size. */
    fun alerts(baseUrl: String): List<SecurityAlert> {
        val collected = mutableListOf<SecurityAlert>()
        var start = 0
        while (true) {
            val page = json(
                path = "/JSON/core/view/alerts/",
                "baseurl" to baseUrl,
                "start" to start.toString(),
                "count" to PAGE_SIZE.toString()
            ).path("alerts")

            page.forEach { alert -> collected += toAlert(alert = alert) }

            if (page.size() < PAGE_SIZE) {
                return collected
            }
            start += PAGE_SIZE
        }
    }

    /**
     * Generates a report inside the container and returns its container-side path.
     *
     * The file is written into the container because the report add-on only writes to disk;
     * [ZapContainer.copyFileFromContainer] brings it back to the host.
     *
     * The confidences are listed explicitly because an empty list makes ZAP include every
     * confidence, false positives - and with them every ignored rule - included.
     */
    fun generateReport(title: String, template: String, fileName: String): String {
        json(
            path = "/JSON/reports/action/generate/",
            "title" to title,
            "template" to template,
            "reportFileName" to fileName,
            "reportDir" to REPORT_DIR,
            "includedConfidences" to REPORTED_CONFIDENCES
        )
        return "$REPORT_DIR/$fileName"
    }

    /** Exports everything ZAP recorded as a HAR document. */
    fun exportHar(): ByteArray {
        return http.send(request(path = "/OTHER/exim/other/exportHar/"), BodyHandlers.ofByteArray())
            .body()
            ?: ByteArray(size = 0)
    }

    private fun toAlert(alert: JsonNode): SecurityAlert {
        return SecurityAlert(
            ruleId = alert.path("pluginId").asString(""),
            name = alert.path("alert").asString("").ifBlank { alert.path("name").asString("") },
            risk = SecurityRisk.fromLabel(value = alert.path("risk").asString("")),
            confidence = SecurityRisk.fromLabel(value = alert.path("confidence").asString("")),
            url = alert.path("url").asString(""),
            method = alert.path("method").asString(""),
            parameter = alert.path("param").asString("")
        )
    }

    /**
     * Reads a field the caller cannot work without.
     *
     * ZAP leaves the field out instead of answering with an error status when it dislikes a
     * request, so failing here names the field rather than producing an empty scan id or a
     * `NumberFormatException` several frames later.
     */
    private fun required(response: JsonNode, field: String): String {
        val value = response.path(field)
        check(!value.isMissingNode) { "The ZAP API response does not contain '$field': $response" }
        return value.asString("")
    }

    private fun json(path: String, vararg params: Pair<String, String?>): JsonNode {
        log.debug { "calling ZAP API $path" }
        val body = http.send(request(path = path, params = params), BodyHandlers.ofString()).body()
        val response = mapper.readTree(checkNotNull(body) { "empty response from ZAP API $path" })

        // ZAP answers 200 with a body of {"code":..,"message":..} for rejected calls.
        check(!response.has("code")) { "ZAP API $path failed: $response" }
        return response
    }

    /** A GET against the ZAP API, with the `Host` header that separates it from the proxy. */
    private fun request(path: String, vararg params: Pair<String, String?>): HttpRequest {
        val query = params.mapNotNull { (key, value) ->
            value?.let { "$key=" + URLEncoder.encode(it, StandardCharsets.UTF_8) }
        }.joinToString(separator = "&")
        val uri = URI.create(apiBaseUrl() + path + if (query.isEmpty()) "" else "?$query")

        return HttpRequest.newBuilder(uri)
            .header(ZapContainer.HOST_HEADER, ZapContainer.API_HOST)
            .GET()
            .build()
    }

    private companion object {
        const val REPORT_DIR = "/home/zap"
        const val PAGE_SIZE = 500

        /** The `newLevel` of an alert filter that marks the alert as false positive. */
        const val FALSE_POSITIVE_LEVEL = "-1"

        /** Every ZAP confidence except "False Positive", in the `|` separated form the API expects. */
        const val REPORTED_CONFIDENCES = "Low|Medium|High|Confirmed"
        val log = KotlinLogging.logger {}

        /** Only trees are read here, so the mapper needs none of the modules the library configures. */
        val mapper = JsonMapper.builder().build()
    }
}
