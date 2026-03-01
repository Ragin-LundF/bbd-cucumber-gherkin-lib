package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumbertests.extensions.toJsonString
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
        return mapOf(
            "exists" to "this field exists",
            "isIgnored" to "New field is ignored",
            "isIgnoredToo" to "New field is ignored too"
        ).toJsonString()
    }

    private fun createUnorderedArrayResponse(): String {
        return mapOf(
            "unsorted" to listOf("Last Element", "First Element", "Second Element")
        ).toJsonString()
    }
}
