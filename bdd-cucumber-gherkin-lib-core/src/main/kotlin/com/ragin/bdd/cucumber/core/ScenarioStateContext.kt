package com.ragin.bdd.cucumber.core

import io.cucumber.java.Scenario
import net.javacrumbs.jsonunit.core.Option
import org.springframework.http.ResponseEntity

object ScenarioStateContext {
    var latestResponse: ResponseEntity<String>? = null

    /**
     * The running Cucumber scenario, so that every glue class can report into the Cucumber report.
     *
     * Set by the logging hook at order 1 and therefore available to every step. `null` outside of a
     * Cucumber run, which makes reporting a no-op instead of a failure.
     *
     * Deliberately **not** cleared by [reset]: that runs at order 3, which is after the hook that
     * sets this, so clearing it here would throw away the scenario before the first step and every
     * report would come out empty. Every scenario overwrites it, so no state leaks between them.
     */
    var scenario: Scenario? = null
    var fileBasePath: String = ""
    var urlBasePath: String = ""

    /**
     * Logical name of the service (web server) the next requests are sent to.
     *
     * `null` means that the service is derived from the requested path, which is the default.
     */
    var serviceName: String? = null
    var bearerToken: String? = null
    var editableBody: String? = ""
    var uriPath: String = ""
    var defaultBearerToken: String = ""
    var headerValues: HashMap<String, String> = hashMapOf()
    var scenarioContextMap: HashMap<String, String> = hashMapOf()
    var scenarioContextFileMap: HashMap<String, ByteArray> = hashMapOf()
    var userTokenMap: HashMap<String, String> = hashMapOf()
    private var jsonPathOptions: MutableList<Option> = mutableListOf()
    var executionTime = -1L
    var dynamicProxyHost: String? = null
    var dynamicProxyPort: Int? = null
    var polling = Polling()

    /**
     * Add IGNORING_EXTRA_ARRAY_ITEMS option to the jsonPathOptions
     */
    fun addJsonIgnoringExtraArrayElements() {
        jsonPathOptions.add(element = Option.IGNORING_EXTRA_ARRAY_ITEMS)
    }

    /**
     * Add IGNORING_EXTRA_FIELDS option to the jsonPathOptions
     */
    fun addJsonIgnoringExtraFields() {
        jsonPathOptions.add(element = Option.IGNORING_EXTRA_FIELDS)
    }

    /**
     * Add IGNORING_ARRAY_ORDER option to the jsonPathOptions
     */
    fun addJsonIgnoringArrayOrder() {
        jsonPathOptions.add(element = Option.IGNORING_ARRAY_ORDER)
    }

    /**
     * Reset states
     */
    fun reset() {
        executionTime = System.currentTimeMillis()
        latestResponse = null
        fileBasePath = ""
        urlBasePath = ""
        serviceName = null
        editableBody = ""
        headerValues.clear()
        jsonPathOptions.clear()
        bearerToken = defaultBearerToken
        polling.clear()
        scenarioContextFileMap.clear()
    }

    fun resolveEntry(key: String): String {
        return scenarioContextMap[key] ?: key
    }

    fun resolveFileEntry(key: String): ByteArray {
        val value = scenarioContextFileMap[key]
        require(value != null) { "Entry $key not found in scenario file context" }

        return value
    }

    fun getJsonPathOptions(): List<Option> {
        // Defensive copy: handing out the backing list lets callers mutate global state.
        return jsonPathOptions.toList()
    }

    @Deprecated(
        message = "Replacing the option list wholesale bypasses reset() and has no callers. " +
            "Use addJsonIgnoringExtraFields/addJsonIgnoringExtraArrayElements/" +
            "addJsonIgnoringArrayOrder instead. Will be removed in the next major release."
    )
    fun setJsonPathOptions(jsonPathOptions: MutableList<Option>) {
        this.jsonPathOptions = jsonPathOptions
    }

    @JvmStatic
    fun current(): ScenarioStateContext {
        return this
    }

    class Polling {
        var pollEverySeconds: Long = 0
        var numberOfPolls: Int = -1

        fun clear() {
            pollEverySeconds = 0
            numberOfPolls = -1
        }
    }
}
