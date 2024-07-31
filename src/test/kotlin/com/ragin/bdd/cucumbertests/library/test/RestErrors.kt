package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class RestErrors {
    @GetMapping("/api/v1/error/{errorCode}")
    fun stubError500(@PathVariable("errorCode") errorCode: Int): ResponseEntity<String> {
        return ResponseEntity.status(errorCode).body(createErrorMessage())
    }

    private fun createErrorMessage(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("error", "CONTAINS_AN_ERROR")
            jsonObject.put("message", "Something went wrong.")
        } catch (_: JSONException) {
        }
        return jsonObject.toString()
    }
}
