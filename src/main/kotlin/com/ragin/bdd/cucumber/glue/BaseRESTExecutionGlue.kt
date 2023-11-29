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
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.apache.commons.text.StringSubstitutor
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
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
import java.util.*

abstract class BaseRESTExecutionGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties,
    private val restTemplate: TestRestTemplate
) : BaseCucumberCore(jsonUtils, bddProperties) {
    @LocalServerPort
    protected var port = 0

    protected fun setLatestResponse(latestResponse: ResponseEntity<String>?) {
        ScenarioStateContext.latestResponse = latestResponse
    }

    init {
        // init ScenarioContext
        bddProperties.scenarioContext?.let { scenarioContextMap.putAll(it) }
        if (bddProperties.authorization?.bearerToken?.default != null) {
            setDefaultBearerToken(bddProperties.authorization.bearerToken.default)
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

    protected fun createRequestFactory(): ClientHttpRequestFactory {
        return HttpComponentsClientHttpRequestFactory(
            createHttpClient()
        )
    }

    @Suppress("ComplexCondition")
    protected fun createHttpClient(): CloseableHttpClient {
        val httpClientBuilder = HttpClientBuilder.create()
        httpClientBuilder.disableRedirectHandling()

        if (bddProperties.ssl != null && bddProperties.ssl.disableCheck) {
            httpClientBuilder.setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(
                            SSLContextBuilder.create()
                            .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                            .build())
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build())
                    .build())
                .build()
        }

        if (bddProperties.proxy != null
            && !StringUtils.isEmpty(bddProperties.proxy.host)
            && bddProperties.proxy.port != null && bddProperties.proxy.port > 0
        ) {
            httpClientBuilder.setProxy(
                HttpHost(
                    Proxy.Type.HTTP.name,
                    bddProperties.proxy.host,
                    bddProperties.proxy.port,
                )
            )
        }
        return httpClientBuilder.build()
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
        val path = preparePath(dataTable)

        // Prepare headers
        val headers = createHTTPHeader(authorized)

        // create HttpEntity
        val body: String? = editableBody

        var httpEntity = HttpEntity<String?>(headers)
        if (httpMethod != HttpMethod.GET && ! body.isNullOrEmpty()) {
            // there was a body...replace with new entity with body
            httpEntity = HttpEntity(body, headers)
        }
        try {
            val targetUrl = fullURLFor(path)
            scenario.log("Request:")
            scenario.log("========")
            scenario.log("HTTP Method: ${httpMethod.name()}")
            scenario.log("HTTP URL   : $targetUrl")
            log.info("Executing call to [${httpMethod.name()}][$targetUrl]")
            setLatestResponse(
                restTemplate.exchange(
                    targetUrl,
                    httpMethod,
                    httpEntity,
                    String::class.java
                )
            )
        } catch (hsee: HttpServerErrorException) {
            setLatestResponse(ResponseEntity(hsee.responseBodyAsString, hsee.statusCode))
        }
        scenario.log("Response:")
        scenario.log("========")
        scenario.log("Status Code: ${ScenarioStateContext.latestResponse?.statusCode}")
        scenario.log("Body       : ${ScenarioStateContext.latestResponse?.body}")
    }

    /**
     * Executes a multipart/form data post request with dynamic URL and replaces dynamic
     * values with data from DataTable.
     *
     * @param dataTable     DataTable which contains the form data
     * @param authorized    should the request execute authorized or unauthorized (true = authorized)
     */
    protected fun executeFormDataRequest(dataTable: DataTable, authorized: Boolean) {
        val path = preparePath(DataTable.emptyDataTable())

        // Prepare headers
        val headers = createHTTPHeader(authorized)
        headers.contentType = MULTIPART_FORM_DATA

        val formDataMap: MultiValueMap<String, Any> = LinkedMultiValueMap()
        dataTable.asMap().forEach { entry ->
            val byteArray = scenarioContextFileMap[entry.value]
            if (Objects.nonNull(byteArray)) {
                formDataMap.add(entry.key, byteArray)
            } else {
                formDataMap.add(entry.key, scenarioContextMap[entry.value] ?: entry.value)
            }
        }

        val request = HttpEntity(formDataMap, headers)
        try {
            val targetUrl = fullURLFor(path)
            log.info("Executing call to [POST][$targetUrl]")
            setLatestResponse(
                restTemplate.postForEntity(targetUrl, request, String::class.java)
            )
        } catch (hsee: HttpServerErrorException) {
            setLatestResponse(ResponseEntity(hsee.responseBodyAsString, hsee.statusCode))
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
        val path = preparePath(dataTable)

        // Prepare headers
        val headers = createHTTPHeader(authorized)
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val dataTableRowList = dataTable.asMaps(String::class.java, String::class.java)
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        dataTableRowList.forEach { list ->
            if (! list["Value"].isNullOrEmpty()) {
                val key = scenarioContextMap[list["Key"]] ?: list["Key"]
                val value = scenarioContextMap[list["Value"]] ?: list["Value"]
                if (! key.isNullOrEmpty() && ! value.isNullOrEmpty()) {
                    map.add(key, value)
                }
            }
        }

        // create HttpEntity
        val httpEntity = HttpEntity(map, headers)
        try {
            val targetUrl = fullURLFor(path)
            scenario.log("Request:")
            scenario.log("========")
            scenario.log("HTTP URL   : $targetUrl")
            scenario.log("URL Encoded Data:")
            map.entries.forEach { pair ->
                scenario.log("  ${pair.key}=${pair.value}")
            }

            log.info("Executing call to [$targetUrl]")
            setLatestResponse(
                restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    httpEntity,
                    String::class.java
                )
            )
        } catch (hsee: HttpServerErrorException) {
            setLatestResponse(ResponseEntity(hsee.responseBodyAsString, hsee.statusCode))
        }
        scenario.log("Response:")
        scenario.log("========")
        scenario.log("Status Code: ${ScenarioStateContext.latestResponse?.statusCode}")
        scenario.log("Body       : ${ScenarioStateContext.latestResponse?.body}")
    }

    protected fun preparePath(dataTable: DataTable): String {
        var path: String = if (!dataTable.isEmpty) {
            prepareDynamicURLWithDataTable(dataTable)
        } else {
            uriPath
        }

        val resolvedUri = scenarioContextMap[path]
        if (resolvedUri != null) {
            path = resolvedUri
        }

        return replacePathPlaceholders(path)
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
        if (! basePath.endsWith("/") && basePath.length > 2 && !urlBasePath.startsWith("/")) {
            basePath.append("/")
        }

        return basePath.toString() + urlBasePath + path
    }

    companion object {
        protected const val PLACEHOLDER = "none"
        private val log = LogFactory.getLog(BaseRESTExecutionGlue::class.java)
    }
}
