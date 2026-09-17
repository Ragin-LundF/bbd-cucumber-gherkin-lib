package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.constants.BddReportConstants.AttachmentNames
import com.ragin.bdd.cucumber.constants.BddReportConstants.JSON_SUBTYPE_MARKER
import com.ragin.bdd.cucumber.constants.BddReportConstants.MediaTypes
import com.ragin.bdd.cucumber.constants.BddReportConstants.NOT_LOGGABLE_SUBTYPES
import com.ragin.bdd.cucumber.constants.BddReportConstants.OBFUSCATED_HEADERS
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * The single channel for everything the library reports to a human while a scenario runs.
 *
 * Two kinds of output, because the Cucumber report renders them differently:
 *
 * * [summary] writes one short line that is always visible in the report, next to the step it
 *   belongs to. The same line goes to the log, so it also shows up on the console without any
 *   Cucumber plugin being configured.
 * * [attachJson], [attachText] and [attachBody] add a named block that the report shows collapsed.
 *   Payloads belong here: they are one click away instead of pushing the interesting lines off the
 *   screen. They are deliberately **not** logged, so a body never floods the console.
 *
 * Every function is a no-op when no scenario is running, so the library keeps working outside of a
 * Cucumber run, and a reporting problem can never fail a test.
 */
class ScenarioReporter(private val options: BddProperties.Logging) {

    /**
     * Reports one short line that stays visible in the report and appears on the console.
     *
     * @param line  the line to report, without a trailing newline
     */
    fun summary(line: String) {
        log.info { line }
        runCatching {
            ScenarioStateContext.scenario?.log(line)
        }.onFailure { error ->
            log.debug(throwable = error) { "Could not report the summary line to the scenario" }
        }
    }

    /**
     * Attaches a JSON payload, which the report shows collapsed and indented.
     *
     * @param name  title of the collapsed block
     * @param json  the payload; attached unchanged when it cannot be parsed as JSON
     */
    fun attachJson(name: String, json: String?) {
        val body = json ?: return
        val formatted = if (options.prettyJson) BddJacksonUtils.prettyPrintOrRaw(json = body) else body
        attach(name = name, content = formatted, mediaType = MediaTypes.JSON)
    }

    /**
     * Attaches a text payload, which the report shows collapsed.
     *
     * @param name  title of the collapsed block
     * @param text  the payload
     */
    fun attachText(name: String, text: String?) {
        attach(name = name, content = text ?: return, mediaType = MediaTypes.TEXT)
    }

    /**
     * Attaches a request or response body, choosing the rendering from its content type.
     *
     * A binary body is described instead of attached, because attaching it as text produces noise.
     *
     * @param name              title of the collapsed block
     * @param body              the body
     * @param contentSubtype    subtype of the Content-Type header, for example `json` or `pdf`
     */
    fun attachBody(name: String, body: String?, contentSubtype: String?) {
        val content = body?.takeIf { it.isNotEmpty() } ?: return
        val subtype = contentSubtype?.lowercase()

        if (subtype != null && NOT_LOGGABLE_SUBTYPES.contains(element = subtype)) {
            attachText(name = name, text = "Content type $subtype received, body not attached.")
            return
        }

        if (subtype == null || subtype.contains(other = JSON_SUBTYPE_MARKER)) {
            attachJson(name = name, json = content)
            return
        }

        attachText(name = name, text = content)
    }

    /**
     * Attaches request or response headers with the sensitive values obfuscated.
     *
     * Does nothing unless `cucumbertest.logging.headers` is enabled.
     *
     * @param name      title of the collapsed block
     * @param headers   header names and their values
     */
    fun attachHeaders(name: String, headers: Map<String, List<String>>) {
        if (!options.headers || headers.isEmpty()) {
            return
        }

        val rendered = headers.entries.joinToString(separator = "\n") { (headerName, values) ->
            "$headerName: ${obfuscate(headerName = headerName, values = values)}"
        }

        attachText(name = name, text = rendered)
    }

    /**
     * Attaches the expected and the actual payload of a failed comparison.
     *
     * @param expected  the expected payload
     * @param actual    the actual payload
     */
    fun attachComparison(expected: String?, actual: String?) {
        attachJson(name = AttachmentNames.EXPECTED_BODY, json = expected)
        attachJson(name = AttachmentNames.ACTUAL_BODY, json = actual)
    }

    /**
     * Obfuscates the values of a sensitive header and leaves every other header untouched.
     */
    internal fun obfuscate(headerName: String, values: List<String>): String {
        if (!OBFUSCATED_HEADERS.contains(element = headerName.lowercase())) {
            return values.joinToString()
        }

        return values.joinToString { value -> ValueObfuscator.obfuscate(value = value) }
    }

    private fun attach(name: String, content: String, mediaType: String) {
        val scenario = ScenarioStateContext.scenario ?: return
        val safeContent = ValueObfuscator.obfuscateSecretsIn(text = truncate(content = content))

        runCatching {
            scenario.attach(safeContent, mediaType, name)
        }.onFailure { error ->
            log.debug(throwable = error) { "Could not attach [$name] to the scenario" }
        }
    }

    /**
     * Internal rather than private so the boundary can be asserted directly: `maxBodyLength` is a
     * documented guarantee and an off-by-one here would either cut a byte short or overrun it.
     */
    internal fun truncate(content: String): String {
        if (options.maxBodyLength <= 0 || content.length <= options.maxBodyLength) {
            return content
        }

        val omitted = content.length - options.maxBodyLength

        return content.take(n = options.maxBodyLength) + "\n... truncated, $omitted more characters"
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
