package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.utils.JacksonUtils
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
        return JacksonUtils.mapper.writeValueAsString(
            mapOf(
                "newName" to body.name,
                "newIds" to body.ids
            )
        )
    }

    data class StubBody(
        val name: String? = null,
        val ids: List<String>
    )
}
