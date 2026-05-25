package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumbertests.extensions.toJsonString
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
        return mapOf(
            "error" to "CONTAINS_AN_ERROR",
            "message" to "Something went wrong."
        ).toJsonString()
    }
}
