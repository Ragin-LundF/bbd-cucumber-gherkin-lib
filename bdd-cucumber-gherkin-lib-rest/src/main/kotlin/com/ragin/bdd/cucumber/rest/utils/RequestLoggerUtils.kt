package com.ragin.bdd.cucumber.rest.utils

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.constants.BddReportConstants.AttachmentNames
import com.ragin.bdd.cucumber.constants.BddReportConstants.Markers
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import com.ragin.bdd.cucumber.utils.ScenarioReporter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.util.MultiValueMap

/**
 * Reports a single HTTP call.
 *
 * One short line for the request and one for the response, which stay visible in the report and on
 * the console. Bodies and headers become collapsed blocks in the report, so a large payload never
 * pushes the interesting lines out of sight.
 */
class RequestLoggerUtils(private val options: BddProperties.Logging) {
    private val reporter = ScenarioReporter(options = options)

    /**
     * Reports an outgoing request.
     *
     * @param httpMethod        method of the request
     * @param url               URL the request is sent to
     * @param body              request body, if any
     * @param headers           request headers, or `null` when the caller has none to report
     * @param encodedDataMap    url-encoded form fields, if any
     */
    fun logRequest(
        httpMethod: HttpMethod,
        url: String,
        body: String? = null,
        headers: HttpHeaders? = null,
        encodedDataMap: MultiValueMap<String, String>? = null
    ) {
        reporter.summary(line = "${Markers.REQUEST} ${httpMethod.name()} $url")

        if (options.requestBody) {
            reporter.attachBody(name = AttachmentNames.REQUEST_BODY, body = body, contentSubtype = null)
        }
        reporter.attachHeaders(name = AttachmentNames.REQUEST_HEADERS, headers = asMap(headers = headers))

        if (!encodedDataMap.isNullOrEmpty()) {
            reporter.attachText(
                name = AttachmentNames.FORM_DATA,
                text = encodedDataMap.entries.joinToString(separator = "\n") { field ->
                    "${field.key}=${field.value.joinToString()}"
                }
            )
        }
    }

    /**
     * Reports the response of the last executed request.
     *
     * @param durationMillis    time the call took
     */
    fun logResponse(durationMillis: Long) {
        val response = ScenarioStateContext.latestResponse
        if (response == null) {
            reporter.summary(line = "${Markers.RESPONSE} no response${durationOf(durationMillis = durationMillis)}")
            return
        }

        reporter.summary(
            line = "${Markers.RESPONSE} ${response.statusCode}${durationOf(durationMillis = durationMillis)}"
        )

        if (options.responseBody) {
            reporter.attachBody(
                name = AttachmentNames.RESPONSE_BODY,
                body = response.body,
                contentSubtype = response.headers.contentType?.subtype
            )
        }
        reporter.attachHeaders(
            name = AttachmentNames.RESPONSE_HEADERS,
            headers = asMap(headers = response.headers)
        )
    }

    /**
     * Reports one attempt of a polling request.
     *
     * @param attempt           number of the current attempt, starting at 1
     * @param maximumAttempts   configured number of attempts
     */
    fun logPollAttempt(attempt: Int, maximumAttempts: Int) {
        val status = ScenarioStateContext.latestResponse?.statusCode?.toString() ?: "no response"
        reporter.summary(line = "${Markers.POLL} poll $attempt/$maximumAttempts - $status")
    }

    /**
     * Spring 7 dropped the MultiValueMap contract from HttpHeaders, so the values are collected
     * explicitly rather than relying on the class being a map.
     */
    private fun asMap(headers: HttpHeaders?): Map<String, List<String>> {
        // Nullable because this is a published entry point: the library always passes headers, but a
        // caller outside Kotlin's null checks can hand over nothing.
        if (headers == null) {
            return emptyMap()
        }

        return headers.headerNames().associateWith { name -> headers.getValuesAsList(name) }
    }

    private fun durationOf(durationMillis: Long): String {
        return "  ($durationMillis ms)"
    }
}
