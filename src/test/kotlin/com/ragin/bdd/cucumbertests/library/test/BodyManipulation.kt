package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
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
        val jsonObject = JSONObject()
        try {
            val newIdsArray = JSONArray()
            body.ids.forEach { o: String? -> newIdsArray.put(o) }

            jsonObject.put("newName", body.name)
            jsonObject.put("newIds", newIdsArray)
        } catch (_: JSONException) {
        }
        return jsonObject.toString()
    }

    data class StubBody(
        val name: String? = null,
        val ids: List<String>
    )
}
