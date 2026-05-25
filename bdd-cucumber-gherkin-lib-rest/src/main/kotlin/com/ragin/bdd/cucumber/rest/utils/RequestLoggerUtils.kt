package com.ragin.bdd.cucumber.rest.utils

import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.rest.constants.NotLoggableSubtype
import io.cucumber.java.Scenario
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpMethod
import org.springframework.util.MultiValueMap

object RequestLoggerUtils {
    val log = KotlinLogging.logger {}
    val notLoggableSubtypes = NotLoggableSubtype.entries.map { it.subtype }

    /**
     * Logs information about an HTTP request being executed in a scenario.
     *
     * @param httpMethod The HTTP method of the request (e.g., GET, POST, etc.).
     * @param url The URL to which the request is being sent.
     * @param encodedDataMap An optional map of URL-encoded data to include in the request.
     * @param scenario The Cucumber scenario under which the request is executed.
     */
    fun logRequest(
        httpMethod: HttpMethod,
        url: String,
        encodedDataMap: MultiValueMap<String, String>? = null,
        scenario: Scenario
    ) {
        scenario.log("Request:")
        scenario.log("========")
        scenario.log("HTTP Method: ${httpMethod.name()}")
        scenario.log("HTTP URL   : $url")
        if (! encodedDataMap.isNullOrEmpty()) {
            scenario.log("URL Encoded Data:")
            encodedDataMap.entries.forEach { pair ->
                scenario.log("  ${pair.key}=${pair.value}")
            }
        }

        log.info { "Executing call to [${httpMethod.name()}][$url]" }
    }

    /**
     * Logs the HTTP response details, including status code, headers, and body, for the given Cucumber scenario.
     *
     * @param scenario The Cucumber scenario instance used for logging response details.
     */
    fun logResponse(scenario: Scenario) {
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
}
