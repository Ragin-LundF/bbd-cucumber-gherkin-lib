package com.ragin.bdd.cucumber.glue

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
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpServerErrorException
import java.io.IOException
import javax.annotation.PostConstruct

abstract class BaseRESTExecutionGlue(jsonUtils: JsonUtils, private val restTemplate: TestRestTemplate) : BaseCucumberCore(jsonUtils) {
    @LocalServerPort
    protected var port = 0

    @Value("\${cucumberTest.server.protocol:http}")
    private val serverProtocol: String? = null

    @Value("\${cucumberTest.server.host:localhost}")
    private val serverHost: String? = null

    @Value("\${cucumberTest.server.port:none}")
    private val serverPort: String? = null
    protected fun setLatestResponse(latestResponse: ResponseEntity<String>?) {
        ScenarioStateContext.latestResponse = latestResponse
    }

    @PostConstruct
    fun init() {
        // https://stackoverflow.com/questions/16748969/java-net-httpretryexception-cannot-retry-due-to-server-authentication-in-strea
        // https://github.com/spring-projects/spring-framework/issues/14004
        restTemplate.restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
        restTemplate.restTemplate.errorHandler = object : DefaultResponseErrorHandler() {
            @Throws(IOException::class)
            override fun hasError(response: ClientHttpResponse): Boolean {
                val statusCode = response.statusCode
                return statusCode.series() == HttpStatus.Series.SERVER_ERROR
            }
        }
    }

    /**
     * Executes a request with given httpMethod
     *
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request executed authorized or unauthorized (true = authorized)
     */
    protected fun executeRequest(
            httpMethod: HttpMethod,
            authorized: Boolean
    ) {
        executeRequest(DataTable.emptyDataTable(), httpMethod, authorized)
    }

    /**
     * Executes a call with dynamic URL and replaces dynamic values with data from DataTable
     *
     * @param dataTable     DataTable which contains dynamic values mapping. If null, no URI parameter will be mapped.
     * @param httpMethod    HttpMethod of the request
     * @param authorized    should the request executed authorized or unauthorized (true = authorized)
     */
    protected fun executeRequest(
            dataTable: DataTable,
            httpMethod: HttpMethod,
            authorized: Boolean
    ) {
        // Prepare path with dynamic URLs from datatable
        var path: String = if (!dataTable.isEmpty) {
            prepareDynamicURLWithDataTable(dataTable)
        } else {
            uriPath
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
            val response = ResponseEntity(hsee.responseBodyAsString, hsee.statusCode)
            setLatestResponse(response)
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
        basePath.append(serverProtocol)
        basePath.append("://")
        basePath.append(serverHost)
        if (PLACEHOLDER == serverPort) {
            basePath.append(":").append(port)
        } else if (serverPort != null && serverPort.trim { it <= ' ' } != "") {
            basePath.append(":").append(serverPort)
        }
        if (!urlBasePath.startsWith("/")) {
            basePath.append("/")
        }
        return basePath.toString() + urlBasePath + path
    }

    companion object {
        protected const val PLACEHOLDER = "none"
    }
}