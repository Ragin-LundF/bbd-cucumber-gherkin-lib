package com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.core.ScenarioStateContext.editableBody
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextFileMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import com.ragin.bdd.cucumber.core.ScenarioStateContext.urlBasePath
import com.ragin.bdd.cucumber.utils.JsonUtils
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.createHTTPHeader
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.prepareDynamicURLWithDataTable
import io.cucumber.datatable.DataTable
import io.cucumber.java.Scenario
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.StringSubstitutor
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.TlsSocketStrategy
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContexts
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.exchange
import org.springframework.boot.resttestclient.postForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpServerErrorException
import java.io.IOException
import java.net.Proxy

abstract class BaseRESTExecutionGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties,
    val restTemplate: TestRestTemplate
) : BaseCucumberCore(
    jsonUtils = jsonUtils,
    bddProperties = bddProperties
) {
    @LocalServerPort
    protected var port = 0

    init {
        // init ScenarioContext
        bddProperties.scenarioContext.let { scenarioContextMap.putAll(it) }
        if (bddProperties.authorization?.bearerToken?.default.isNullOrEmpty().not()) {
            setDefaultBearerToken(
                defaultBearerToken = bddProperties.authorization.bearerToken.default
            )
        }

        // https://stackoverflow.com/questions/16748969/java-net-httpretryexception-cannot-retry-due-to-server-authentication-in-strea
        // https://github.com/spring-projects/spring-framework/issues/14004
        restTemplate.restTemplate.requestFactory = createRequestFactory()
        restTemplate.restTemplate.errorHandler = object : DefaultResponseErrorHandler() {
            @Throws(IOException::class)
            override fun hasError(response: ClientHttpResponse): Boolean {
                val statusCode = response.statusCode
                return statusCode.is5xxServerError
            }
        }
    }

    protected fun setLatestResponse(latestResponse: ResponseEntity<String>?) {
        ScenarioStateContext.latestResponse = latestResponse
    }

    protected fun createRequestFactory(): ClientHttpRequestFactory {
        return HttpComponentsClientHttpRequestFactory(
            createHttpClient()
        )
    }

    @Suppress("ComplexCondition")
    protected fun createHttpClient(proxyHost: String? = null, proxyPort: Int? = null): CloseableHttpClient {
        val httpClientBuilder = HttpClientBuilder.create()
        httpClientBuilder.disableRedirectHandling()

        if (bddProperties.ssl != null && bddProperties.ssl.disableCheck) {
            val sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build()
            val tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic()
            val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy as TlsSocketStrategy)
                .build()
            httpClientBuilder.setConnectionManager(connectionManager)
        }

        var proxyHostUsed: String? = null
        var proxyPortUsed: Int? = null
        if (bddProperties.proxy != null
            && !StringUtils.isEmpty(bddProperties.proxy.host)
            && bddProperties.proxy.port != null && bddProperties.proxy.port > 0
        ) {
            proxyHostUsed = bddProperties.proxy.host
            proxyPortUsed = bddProperties.proxy.port
        }

        if (proxyHost != null && proxyPort != null) {
            proxyHostUsed = proxyHost
            proxyPortUsed = proxyPort
        }

        val proxy = proxyOrNull(proxyHost = proxyHostUsed, proxyPort = proxyPortUsed)
        if (proxy != null) {
            httpClientBuilder.setProxy(
                HttpHost(
                    Proxy.Type.HTTP.name,
                    proxy.first,
                    proxy.second,
                )
            )
        }
        return httpClientBuilder.build()
    }

    private fun proxyOrNull(proxyHost: String?, proxyPort: Int?): Pair<String, Int>? {
        return if (ScenarioStateContext.dynamicProxyHost != null && ScenarioStateContext.dynamicProxyPort != null) {
            Pair(first = ScenarioStateContext.dynamicProxyHost!!, second = ScenarioStateContext.dynamicProxyPort!!)
        } else if (proxyHost != null && proxyPort != null) {
            Pair(first = proxyHost, second = proxyPort)
        } else {
            null
        }
    }

    /**
     * Executes a request with given httpMethod
     *
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request execute authorized or unauthorized (true = authorized)
     * @param scenario      Cucumber scenario
     */
    protected fun executeRequest(
        httpMethod: HttpMethod,
        authorized: Boolean,
        scenario: Scenario
    ) {
        executeRequest(
            dataTable = DataTable.emptyDataTable(),
            httpMethod = httpMethod,
            authorized = authorized,
            scenario = scenario
        )
    }

    /**
     * Executes a call with dynamic URL and replaces dynamic values with data from DataTable.
     *
     * @param dataTable     DataTable which contains dynamic values mapping. If null, no URI parameter will be mapped.
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request execute authorized or unauthorized (true = authorized)
     */
    protected fun executeRequest(
        dataTable: DataTable,
        httpMethod: HttpMethod,
        authorized: Boolean,
        scenario: Scenario
    ) {
        // Prepare a path with dynamic URLs from datatable
        val path = preparePath(dataTable = dataTable)

        // Prepare headers
        val headers = createHTTPHeader(addAuthorisation = authorized)

        // create HttpEntity
        val body: String? = editableBody

        var httpEntity = HttpEntity<String>(headers)
        if (httpMethod != HttpMethod.GET && !body.isNullOrEmpty()) {
            // there was a body...replace with new entity with body
            httpEntity = HttpEntity(body, headers)
        }
        runCatching {
            val targetUrl = fullURLFor(path = path)
            scenario.log("Request:")
            scenario.log("========")
            scenario.log("HTTP Method: ${httpMethod.name()}")
            scenario.log("HTTP URL   : $targetUrl")
            log.info { "Executing call to [${httpMethod.name()}][$targetUrl]" }

            setLatestResponse(
                latestResponse = restTemplate.exchange<String>(
                    url = targetUrl,
                    method = httpMethod,
                    requestEntity = httpEntity
                )
            )
        }.onFailure { error ->
            when (error) {
                is HttpServerErrorException ->
                    setLatestResponse(
                        latestResponse = ResponseEntity(
                            error.responseBodyAsString,
                            error.statusCode
                        )
                    )

                else -> log.error(error) { "Error during REST call execution" }
            }
        }
        logResponse(scenario = scenario)
    }

    /**
     * Executes a multipart/form data post request with dynamic URL and replaces dynamic
     * values with data from DataTable.
     *
     * @param dataTable     DataTable which contains the form data
     * @param authorized    should the request execute authorized or unauthorized (true = authorized)
     */
    protected fun executeFormDataRequest(dataTable: DataTable, authorized: Boolean) {
        val path = preparePath(dataTable = DataTable.emptyDataTable())

        // Prepare headers
        val headers = createHTTPHeader(addAuthorisation = authorized)
        headers.contentType = MULTIPART_FORM_DATA

        val formDataMap: MultiValueMap<String, Any> = LinkedMultiValueMap()
        dataTable.asMultiValueMap().forEach { entry ->
            for (entryItem in entry.value) {
                val scenarioContextMapValue = scenarioContextMap[entryItem]
                val byteArray = scenarioContextFileMap[entryItem]
                if (byteArray != null) {
                    formDataMap.add(entry.key, object : ByteArrayResource(byteArray) {
                        override fun getFilename(): String {
                            return scenarioContextMapValue ?: entryItem
                        }
                    })
                } else {
                    formDataMap.add(entry.key, scenarioContextMapValue ?: entryItem)
                }
            }
        }

        val request = HttpEntity(formDataMap, headers)
        runCatching {
            val targetUrl = fullURLFor(path = path)
            log.info { "Executing call to [POST][$targetUrl]" }
            setLatestResponse(
                latestResponse = restTemplate.postForEntity<String>(
                    url = targetUrl,
                    request = request
                )
            )
        }.onFailure { error ->
            when (error) {
                is HttpServerErrorException ->
                    setLatestResponse(
                        latestResponse = ResponseEntity(
                            error.responseBodyAsString,
                            error.statusCode
                        )
                    )
            }
        }
    }

    /**
     * Executes an application/x-www-form-urlencoded data post request with dynamic values with data from DataTable.
     *
     * @param dataTable     DataTable which contains the form-urlencoded data
     * @param authorized    should the request execute authorized or unauthorized (true = authorized)
     */
    protected fun executeUrlEncodedRequest(dataTable: DataTable, authorized: Boolean, scenario: Scenario) {
        // Prepare a path with dynamic URLs from datatable
        val path = preparePath(dataTable = dataTable)

        // Prepare headers
        val headers = createHTTPHeader(authorized)
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val dataTableRowList = dataTable.asMaps(String::class.java, String::class.java)
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        dataTableRowList.forEach { list ->
            if (!list["Value"].isNullOrEmpty()) {
                val key = scenarioContextMap[list["Key"]] ?: list["Key"]
                val value = scenarioContextMap[list["Value"]] ?: list["Value"]
                if (!key.isNullOrEmpty() && !value.isNullOrEmpty()) {
                    map.add(key, value)
                }
            }
        }

        // create HttpEntity
        val httpEntity = HttpEntity(map, headers)
        runCatching {
            val targetUrl = fullURLFor(path = path)
            scenario.log("Request:")
            scenario.log("========")
            scenario.log("HTTP URL   : $targetUrl")
            scenario.log("URL Encoded Data:")
            map.entries.forEach { pair ->
                scenario.log("  ${pair.key}=${pair.value}")
            }

            log.info { "Executing call to [$targetUrl]" }
            setLatestResponse(
                latestResponse = restTemplate.exchange<String>(
                    url = targetUrl,
                    method = HttpMethod.POST,
                    requestEntity = httpEntity
                )
            )
        }.onFailure { error ->
            when (error) {
                is HttpServerErrorException ->
                    setLatestResponse(
                        latestResponse = ResponseEntity(
                            error.responseBodyAsString,
                            error.statusCode
                        )
                    )
            }
        }
        logResponse(scenario = scenario)
    }

    protected fun preparePath(dataTable: DataTable): String {
        var path: String = if (!dataTable.isEmpty) {
            prepareDynamicURLWithDataTable(dataTable = dataTable)
        } else {
            uriPath
        }

        val resolvedUri = scenarioContextMap[path]
        if (resolvedUri != null) {
            path = resolvedUri
        }

        return replacePathPlaceholders(path = path)
    }

    /**
     * Replace placeholder like ${myContextItem} with the available items from the context.
     *
     * @param path  Path which can contain the placeholder
     * @return      replaced path
     */
    protected fun replacePathPlaceholders(path: String): String {
        // Build StringSubstitutor
        val sub = StringSubstitutor(scenarioContextMap)

        // Replace
        return sub.replace(path)
    }

    /**
     * Full URL for URI path
     *
     * @param path Path of URI
     * @return  full URL as protocol:server_host:port/basePath/path
     */
    protected fun fullURLFor(path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val basePath = StringBuilder()

        if (bddProperties.server != null) {
            basePath.append(bddProperties.server.protocol)
            basePath.append("://")
            basePath.append(bddProperties.server.host)
            if (PLACEHOLDER == bddProperties.server.port) {
                basePath.append(":").append(port)
            } else if (bddProperties.server.port != null && bddProperties.server.port.trim { it <= ' ' } != "") {
                basePath.append(":").append(bddProperties.server.port)
            }
        }
        if (!basePath.endsWith("/") && basePath.length > 2 && !urlBasePath.startsWith(prefix = "/")) {
            basePath.append("/")
        }

        return basePath.toString() + urlBasePath + path
    }

    fun DataTable.asMultiValueMap(): MultiValueMap<String, String> {
        val formDataMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        val lists = this.asLists()
        for (list in lists) {
            formDataMap.add(list.first(), list.last())
        }
        return formDataMap
    }

    protected fun logResponse(scenario: Scenario) {
        val contentType = ScenarioStateContext.latestResponse?.headers?.contentType
        val response = if (contentType != null && notLoggableSubtypes.contains(contentType.subtype.lowercase())) {
            "Content type ${contentType.subtype} received."
        } else {
            ScenarioStateContext.latestResponse?.body.toString()
        }

        scenario.log("Response:")
        scenario.log("========")
        scenario.log("Status Code: ${ScenarioStateContext.latestResponse?.statusCode}")
        scenario.log("Body       : $response")
    }

    companion object {
        protected const val PLACEHOLDER = "none"
        protected val notLoggableSubtypes = listOf(
            "pdf",
            "octet-stream",
            "zip"
        )
        private val log = KotlinLogging.logger { }
    }
}
