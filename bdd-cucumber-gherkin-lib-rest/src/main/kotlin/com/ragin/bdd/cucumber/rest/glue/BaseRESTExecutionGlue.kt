package com.ragin.bdd.cucumber.rest.glue

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.core.ScenarioStateContext.editableBody
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextFileMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import com.ragin.bdd.cucumber.rest.extensions.asMultiValueMap
import com.ragin.bdd.cucumber.rest.httpclient.ClientHttpRequestFactory
import com.ragin.bdd.cucumber.rest.utils.RequestLoggerUtils
import com.ragin.bdd.cucumber.rest.utils.UrlUtils
import com.ragin.bdd.cucumber.utils.BddJsonUtils
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.createHTTPHeader
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.prepareDynamicURLWithDataTable
import io.cucumber.datatable.DataTable
import io.cucumber.java.Scenario
import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpServerErrorException
import java.io.IOException

abstract class BaseRESTExecutionGlue(
    jsonUtils: BddJsonUtils,
    bddProperties: BddProperties,
    val restTemplate: TestRestTemplate
) : BaseCucumberCore(
    jsonUtils = jsonUtils,
    bddProperties = bddProperties
) {
    @LocalServerPort
    protected var port = 0
    protected val clientHttpRequestFactory = ClientHttpRequestFactory(bddProperties = bddProperties)

    init {
        // init ScenarioContext
        bddProperties.scenarioContext.let { scenarioContextMap.putAll(it) }
        if (bddProperties.authorization?.bearerToken?.default.isNullOrEmpty().not()) {
            setDefaultBearerToken(
                defaultBearerToken = bddProperties.authorization!!.bearerToken.default
            )
        }

        // https://stackoverflow.com/questions/16748969/java-net-httpretryexception-cannot-retry-due-to-server-authentication-in-strea
        // https://github.com/spring-projects/spring-framework/issues/14004
        restTemplate.restTemplate.requestFactory = clientHttpRequestFactory.createRequestFactory()
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
            val targetUrl = UrlUtils.fullURLFor(path = path)
            RequestLoggerUtils.logRequest(httpMethod = httpMethod, url = targetUrl, scenario = scenario)

            setLatestResponse(
                latestResponse = restTemplate.exchange<String>(
                    url = targetUrl,
                    method = httpMethod,
                    requestEntity = httpEntity
                )
            )
        }.onFailure { error ->
            handleRestError(error = error)
        }
        RequestLoggerUtils.logResponse(scenario = scenario)
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
            val targetUrl = UrlUtils.fullURLFor(path = path)
            log.info { "Executing call to [POST][$targetUrl]" }
            setLatestResponse(
                latestResponse = restTemplate.postForEntity<String>(
                    url = targetUrl,
                    request = request
                )
            )
        }.onFailure { error ->
            handleRestError(error = error)
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
        val headers = createHTTPHeader(addAuthorisation = authorized)
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
            val targetUrl = UrlUtils.fullURLFor(path = path)
            RequestLoggerUtils.logRequest(
                httpMethod = HttpMethod.POST,
                url = targetUrl,
                encodedDataMap = map,
                scenario = scenario
            )

            setLatestResponse(
                latestResponse = restTemplate.exchange<String>(
                    url = targetUrl,
                    method = HttpMethod.POST,
                    requestEntity = httpEntity
                )
            )
        }.onFailure { error ->
            handleRestError(error = error)
        }
        RequestLoggerUtils.logResponse(scenario = scenario)
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

        return UrlUtils.replacePathPlaceholders(path = path)
    }

    protected fun handleRestError(error: Throwable) {
        when (error) {
            is HttpServerErrorException ->
                setLatestResponse(
                    latestResponse = ResponseEntity(
                        error.responseBodyAsString,
                        error.statusCode
                    )
                )
            else -> log.error(throwable = error) { "Error during REST call execution" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
