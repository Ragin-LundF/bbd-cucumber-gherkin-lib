package com.ragin.bdd.cucumber.core

import net.javacrumbs.jsonunit.core.Option
import org.springframework.http.ResponseEntity
import java.util.*

object ScenarioStateContext {
    var latestResponse: ResponseEntity<String>? = null
    var fileBasePath = ""
    var urlBasePath = ""
    var bearerToken: String? = null
    var editableBody = ""
    var uriPath = ""
    var defaultBearerToken = ""
    var headerValues: HashMap<String, String> = HashMap()
    var scenarioContextMap: HashMap<String, String> = HashMap()
    var userTokenMap: HashMap<String, String> = HashMap()
    private var jsonPathOptions: MutableList<Option> = ArrayList(0)
    var executionTime = -1L
    var polling = Polling()

    /**
     * Add IGNORING_EXTRA_ARRAY_ITEMS option to the jsonPathOptions
     */
    fun addJsonIgnoringExtraArrayElements() {
        jsonPathOptions.add(Option.IGNORING_EXTRA_ARRAY_ITEMS)
    }

    /**
     * Add IGNORING_EXTRA_FIELDS option to the jsonPathOptions
     */
    fun addJsonIgnoringExtraFields() {
        jsonPathOptions.add(Option.IGNORING_EXTRA_FIELDS)
    }

    /**
     * Add IGNORING_ARRAY_ORDER option to the jsonPathOptions
     */
    fun addJsonIgnoringArrayOrder() {
        jsonPathOptions.add(Option.IGNORING_ARRAY_ORDER)
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
        headerValues = HashMap()
        jsonPathOptions = ArrayList(0)
        bearerToken = defaultBearerToken
        polling = Polling()
    }

    fun resolveEntry(key: String): String {
        var value = scenarioContextMap[key]
        if (Objects.isNull(value)) {
            value = key
        }

        return value!!
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