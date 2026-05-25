package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumbertests.extensions.toJsonString
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BodyManipulation {
    @PostMapping("/api/v1/body/manipulate")
    fun stubBodyManipulation(
        @RequestBody body: StubBody
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CREATED).body(createManipulatedBodyResponse(body))
    }

    private fun createManipulatedBodyResponse(body: StubBody): String {
        return mapOf(
            "newName" to body.name,
            "newIds" to body.ids
        ).toJsonString()
    }

    data class StubBody(
        val name: String? = null,
        val ids: List<String>
    )
}
