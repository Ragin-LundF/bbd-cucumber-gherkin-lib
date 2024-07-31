package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
class CustomHeader {
    @GetMapping("/api/v1/customHeader")
    fun stubCustomHeader(@RequestHeader("X-My-Custom-Header") header: String): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(createHeaderResponse(header))
    }

    @GetMapping("/api/v1/overwrittenAuthHeader")
    fun stubAuthHeader(@RequestHeader("Authorization") header: String): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(createHeaderResponse(header))
    }

    @GetMapping("/api/v1/allHeaders")
    fun allHeaders(@RequestHeader headers: HttpHeaders): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK).body(createHeadersResponse(headers))
    }

    private fun createHeaderResponse(header: String): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("header", header)
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }

    private fun createHeadersResponse(headers: HttpHeaders): String {
        val jsonObject = JSONObject()
        try {
            for ((key, value) in headers) {
                val headerValueStr = value.stream()
                    .map { obj: String? -> java.lang.String.valueOf(obj) }
                    .collect(Collectors.joining(";"))
                jsonObject.put(key, headerValueStr)
            }
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }
}
