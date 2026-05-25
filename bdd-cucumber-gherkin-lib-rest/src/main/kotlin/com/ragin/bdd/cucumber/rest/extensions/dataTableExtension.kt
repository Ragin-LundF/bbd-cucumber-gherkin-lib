package com.ragin.bdd.cucumber.rest.extensions

import io.cucumber.datatable.DataTable
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


fun DataTable.asMultiValueMap(): MultiValueMap<String, String> {
    val formDataMap: MultiValueMap<String, String> = LinkedMultiValueMap()
    val lists = this.asLists()
    for (list in lists) {
        formDataMap.add(list.first(), list.last())
    }
    return formDataMap
}
