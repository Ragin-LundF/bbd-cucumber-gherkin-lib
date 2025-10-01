package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.utils.JacksonUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HeaderCheck {
    @GetMapping("/api/v1/header")
    fun stubUnauthorizedGet(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK)
            .header("X-TEST-HEADER", "present")
            .body(createResponse())
    }

    private fun createResponse(): String {
        return JacksonUtils.mapper.writeValueAsString(
            mapOf(
                "status" to "ok"
            )
        )
    }
}
