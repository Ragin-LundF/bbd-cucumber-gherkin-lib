package com.ragin.bdd.cucumber.utils

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.blackbird.BlackbirdModule
import tools.jackson.module.kotlin.KotlinModule

object BddJacksonUtils {
    val mapper: ObjectMapper = JsonMapper.builder()
        .addModule(BlackbirdModule())
        .addModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .changeDefaultPropertyInclusion { incl ->
            incl.withValueInclusion(JsonInclude.Include.NON_EMPTY)
        }.build()

    /**
     * Formats a JSON string with indentation so it is readable in a log or a report.
     *
     * Anything that is not valid JSON is returned unchanged: this is used for reporting only, where
     * a malformed payload is exactly what the reader needs to see.
     *
     * @param json  the JSON string to format
     * @return the indented JSON, or the input unchanged when it cannot be parsed
     */
    fun prettyPrintOrRaw(json: String): String {
        return runCatching {
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json))
        }.getOrDefault(defaultValue = json)
    }
}
