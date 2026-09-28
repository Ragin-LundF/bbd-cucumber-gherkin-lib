package com.ragin.bdd.cucumber.rest.extensions

import io.cucumber.datatable.DataTable
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 * Reads the first column as form field name and the last column as its value.
 *
 * Cucumber returns an empty cell as `null`. A field needs a name, but an empty value is sent as an
 * empty form field.
 */
fun DataTable.asMultiValueMap(): MultiValueMap<String, String> {
    val formDataMap: MultiValueMap<String, String> = LinkedMultiValueMap()
    val lists = this.asLists()
    for (list in lists) {
        val value = list.last().orEmpty()
        val key = requireNotNull(list.first()) { "DataTable contains an empty field name for value '$value'" }
        formDataMap.add(key, value)
    }
    return formDataMap
}

/**
 * Reads a two column DataTable as key/value map in table order.
 *
 * Cucumber returns an empty cell as `null`. None of the key/value sentences accept one, so an empty
 * cell fails the step with the row it was found in.
 */
fun DataTable.asNonNullMap(): Map<String, String> {
    return asMap(String::class.java, String::class.java).entries.associate { (key, value) ->
        val nonNullKey = requireNotNull(key) { "DataTable contains an empty key for value '$value'" }
        nonNullKey to requireNotNull(value) { "DataTable contains an empty value for key '$nonNullKey'" }
    }
}
