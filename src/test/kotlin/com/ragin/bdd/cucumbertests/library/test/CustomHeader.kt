package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.utils.JacksonUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

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
        return JacksonUtils.mapper.writeValueAsString(
            mapOf(
                "header" to header
            )
        )
    }

    private fun createHeadersResponse(headers: HttpHeaders): String {
        val bodyMap = mutableMapOf<String, String>()
        val headerSet = headers.headerSet()
        for ((key, value) in headerSet) {
            bodyMap[key] = value.joinToString(";")
        }
        return JacksonUtils.mapper.writeValueAsString(bodyMap)
    }
}
