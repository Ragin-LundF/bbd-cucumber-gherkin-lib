package com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.core.ScenarioStateContext.editableBody
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import com.ragin.bdd.cucumber.core.ScenarioStateContext.urlBasePath
import com.ragin.bdd.cucumber.utils.JsonUtils
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.createHTTPHeader
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.prepareDynamicURLWithDataTable
import io.cucumber.datatable.DataTable
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.StringSubstitutor
import org.apache.http.HttpHost
import org.apache.http.client.RedirectStrategy
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpServerErrorException
import java.io.IOException
import java.net.Proxy
import javax.annotation.PostConstruct

abstract class BaseRESTExecutionGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties,
    private val restTemplate: TestRestTemplate
) : BaseCucumberCore(jsonUtils, bddProperties) {
    companion object {
        protected const val PLACEHOLDER = "none"
    }

    @LocalServerPort
    protected var port = 0

    protected fun setLatestResponse(latestResponse: ResponseEntity<String>?) {
        ScenarioStateContext.latestResponse = latestResponse
    }

    @PostConstruct
    fun init() {
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
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR
            }
        }
    }

    protected fun createRequestFactory() : ClientHttpRequestFactory {
        return HttpComponentsClientHttpRequestFactory(
            createHttpClient()
        )
    }

    protected fun createHttpClient() : CloseableHttpClient {
        val httpClientBuilder = HttpClientBuilder.create()
        httpClientBuilder.disableRedirectHandling()

        if (bddProperties.ssl != null && bddProperties.ssl.disableCheck) {
            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier())
        }
        if (bddProperties.proxy != null
            && ! StringUtils.isEmpty(bddProperties.proxy.host)
            && bddProperties.proxy.port != null && bddProperties.proxy.port > 0) {
            httpClientBuilder.setProxy(HttpHost(
                bddProperties.proxy.host,
                bddProperties.proxy.port,
                Proxy.Type.HTTP.name
            ))
        }
        return httpClientBuilder.build()
    }

    /**
     * Executes a request with given httpMethod
     *
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request executed authorized or unauthorized (true = authorized)
     */
    protected fun executeRequest(httpMethod: HttpMethod, authorized: Boolean) {
        executeRequest(DataTable.emptyDataTable(), httpMethod, authorized)
    }

    /**
     * Executes a call with dynamic URL and replaces dynamic values with data from DataTable
     *
     * @param dataTable     DataTable which contains dynamic values mapping. If null, no URI parameter will be mapped.
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request executed authorized or unauthorized (true = authorized)
     */
    protected fun executeRequest(dataTable: DataTable, httpMethod: HttpMethod, authorized: Boolean) {
        // Prepare path with dynamic URLs from datatable
        var path: String = if (!dataTable.isEmpty) {
            prepareDynamicURLWithDataTable(dataTable)
        } else {
            uriPath
        }

        val resolvedUri = scenarioContextMap[path]
        if (resolvedUri != null) {
            path = resolvedUri
        }

        path = replacePathPlaceholders(path)

        // Prepare headers
        val headers = createHTTPHeader(authorized)

        // create HttpEntity
        val body = editableBody
        var httpEntity = HttpEntity<String?>(headers)
        if (!StringUtils.isEmpty(body)) {
            // there was a body...replace with new entity with body
            httpEntity = HttpEntity(body, headers)
        }
        try {
            setLatestResponse(
                    restTemplate.exchange(
                            fullURLFor(path),
                            httpMethod,
                            httpEntity,
                            String::class.java
                    )
            )
        } catch (hsee: HttpServerErrorException) {
            setLatestResponse(ResponseEntity(hsee.responseBodyAsString, hsee.statusCode))
        }
    }

    /**
     * Replace placeholder like ${myContextItem} with the available items from the context
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
        if (!urlBasePath.startsWith("/")) {
            basePath.append("/")
        }
        return basePath.toString() + urlBasePath + path
    }
}
