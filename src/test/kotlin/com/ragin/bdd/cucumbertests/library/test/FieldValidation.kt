package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.utils.JacksonUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class FieldValidation {
    @GetMapping("/api/v1/fieldValidation")
    fun stubFieldValidation(): ResponseEntity<String> {
        return ResponseEntity.ok(createResponse())
    }

    private fun createResponse(): String {
        val jsonAsMap = mapOf(
            "string" to "is a string",
            "number" to 12,
            "boolean" to true,
            "list" to listOf("First", "Second"),
            "object" to mapOf(
                "firstname" to "John",
                "lastname" to "Doe"
            ),
            "uuid" to UUID.randomUUID(),
            "objectList" to listOf(
                mapOf(
                    "first" to 1,
                    "second" to 2
                ),
                mapOf(
                    "first" to 3,
                    "second" to 4
                )
            )
        )

        return JacksonUtils.mapper.writeValueAsString(jsonAsMap)
    }
}
