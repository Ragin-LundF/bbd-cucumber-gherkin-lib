package com.ragin.bdd.cucumber.core

import net.javacrumbs.jsonunit.core.Option
import org.springframework.http.ResponseEntity

object ScenarioStateContext {
    var latestResponse: ResponseEntity<String>? = null
    var fileBasePath: String = ""
    var urlBasePath: String = ""
    var bearerToken: String? = null
    var editableBody: String? = ""
    var uriPath:String = ""
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
        editableBody = ""
        headerValues = hashMapOf()
        jsonPathOptions = mutableListOf()
        bearerToken = defaultBearerToken
        polling = Polling()
        scenarioContextFileMap = hashMapOf()
    }

    fun resolveEntry(key: String): String {
        return scenarioContextMap[key] ?: key
    }

    fun resolveFileEntry(key: String): ByteArray {
        val value = scenarioContextFileMap[key]
        require(value != null) {"Entry $key not found in scendario file context"}

        return value
    }

    fun getJsonPathOptions(): List<Option> {
        return jsonPathOptions
    }

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
    }
}
