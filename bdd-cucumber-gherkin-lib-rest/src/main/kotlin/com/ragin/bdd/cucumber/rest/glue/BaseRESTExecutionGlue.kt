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
import com.ragin.bdd.cucumber.rest.utils.ServiceUrlResolver
import com.ragin.bdd.cucumber.rest.utils.UrlUtils
import com.ragin.bdd.cucumber.utils.BddJsonUtils
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.createHTTPHeader
import com.ragin.bdd.cucumber.utils.RESTCommunicationUtils.prepareDynamicURLWithDataTable
import io.cucumber.datatable.DataTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.exchange
import org.springframework.boot.resttestclient.postForEntity
import org.springframework.core.env.Environment
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
    /**
     * Nullable and not `lateinit` on purpose: a glue class that is constructed outside of Spring
     * must keep working. A missing environment means that no service is discovered, which falls
     * back to resolving URLs from `cucumbertest.server.*` alone.
     */
    @Autowired(required = false)
    protected var environment: Environment? = null
    protected val clientHttpRequestFactory = ClientHttpRequestFactory(bddProperties = bddProperties)

    private val requestLogger = RequestLoggerUtils(options = bddProperties.logging)

    private val serviceUrlResolver: ServiceUrlResolver? by lazy {
        environment?.let { resolvedEnvironment ->
            ServiceUrlResolver(environment = resolvedEnvironment, bddProperties = bddProperties)
        }
    }

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
     */
    protected fun executeRequest(httpMethod: HttpMethod, authorized: Boolean) {
        executeRequest(
            dataTable = DataTable.emptyDataTable(),
            httpMethod = httpMethod,
            authorized = authorized
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
        authorized: Boolean
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
        // Resolved outside of runCatching so that a configuration error surfaces as itself instead
        // of being turned into a missing response by handleRestError.
        val targetUrl = targetUrlFor(path = path)
        requestLogger.logRequest(httpMethod = httpMethod, url = targetUrl, body = body, headers = headers)

        val startedAt = System.currentTimeMillis()
        runCatching {
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
        requestLogger.logResponse(durationMillis = System.currentTimeMillis() - startedAt)
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
                    formDataMap.add(
                        entry.key,
                        object : ByteArrayResource(byteArray) {
                            override fun getFilename(): String {
                                return scenarioContextMapValue ?: entryItem
                            }
                        }
                    )
                } else {
                    formDataMap.add(entry.key, scenarioContextMapValue ?: entryItem)
                }
            }
        }

        val request = HttpEntity(formDataMap, headers)
        // Resolved outside of runCatching so that a configuration error surfaces as itself instead
        // of being turned into a missing response by handleRestError.
        val targetUrl = targetUrlFor(path = path)
        requestLogger.logRequest(httpMethod = HttpMethod.POST, url = targetUrl, headers = headers)

        val startedAt = System.currentTimeMillis()
        runCatching {
            setLatestResponse(
                latestResponse = restTemplate.postForEntity<String>(
                    url = targetUrl,
                    request = request
                )
            )
        }.onFailure { error ->
            handleRestError(error = error)
        }
        requestLogger.logResponse(durationMillis = System.currentTimeMillis() - startedAt)
    }

    /**
     * Executes an application/x-www-form-urlencoded data post request with dynamic values with data from DataTable.
     *
     * @param dataTable     DataTable which contains the form-urlencoded data
     * @param authorized    should the request execute authorized or unauthorized (true = authorized)
     */
    protected fun executeUrlEncodedRequest(dataTable: DataTable, authorized: Boolean) {
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
        // Resolved outside of runCatching so that a configuration error surfaces as itself instead
        // of being turned into a missing response by handleRestError.
        val targetUrl = targetUrlFor(path = path)
        requestLogger.logRequest(
            httpMethod = HttpMethod.POST,
            url = targetUrl,
            headers = headers,
            encodedDataMap = map
        )

        val startedAt = System.currentTimeMillis()
        runCatching {
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
        requestLogger.logResponse(durationMillis = System.currentTimeMillis() - startedAt)
    }

    /**
     * Builds the URL the request is sent to.
     *
     * The service is picked from the path or from the service the scenario selected explicitly.
     * When no service matches, the URL is built from `cucumbertest.server.*` exactly as before,
     * which keeps a relative path relative so that the TestRestTemplate resolves it against the
     * application under test.
     *
     * @param path  requested path with all placeholders already replaced
     * @return absolute or relative URL to call
     */
    protected fun targetUrlFor(path: String): String {
        val service = serviceUrlResolver?.resolveFor(
            relativePath = UrlUtils.appendPathElements(path = "", ScenarioStateContext.urlBasePath, path),
            explicitServiceName = ScenarioStateContext.serviceName
        )

        if (service != null) {
            return UrlUtils.fullURLFor(
                path = path,
                protocol = service.protocol,
                host = service.host,
                port = service.port,
                servicePath = service.basePath
            )
        }

        return UrlUtils.fullURLFor(
            path = path,
            protocol = bddProperties.server?.protocol,
            host = bddProperties.server?.host,
            port = bddProperties.server?.port
        )
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
