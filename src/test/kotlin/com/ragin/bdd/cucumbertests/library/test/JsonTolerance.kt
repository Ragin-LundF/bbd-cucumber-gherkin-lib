package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class JsonTolerance {
    @GetMapping("/api/v1/jsonWithUnsortedArray")
    fun stubJsonWithUnsortedList(): ResponseEntity<String> {
        return ResponseEntity.ok(createUnorderedArrayResponse())
    }

    @GetMapping("/api/v1/jsonWithExtraFields")
    fun stubJsonWithExtraFields(): ResponseEntity<String> {
        return ResponseEntity.ok(createExtraFieldsResponse())
    }

    private fun createExtraFieldsResponse(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("exists", "this field exists")
            jsonObject.put("isIgnored", "New field is ignored")
            jsonObject.put("isIgnoredToo", "New field is ignored too")
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }

    private fun createUnorderedArrayResponse(): String {
        val jsonObject = JSONObject()
        try {
            val array = JSONArray()
            array.put("Last Element")
            array.put("First Element")
            array.put("Second Element")

            jsonObject.put("unsorted", array)
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }
}
