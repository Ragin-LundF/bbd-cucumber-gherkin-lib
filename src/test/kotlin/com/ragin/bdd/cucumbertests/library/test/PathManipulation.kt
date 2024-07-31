package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@RestController
class PathManipulation {
    @PostMapping("/api/v1/{resourceId}/{subResourceId}")
    fun stubDynamicPath(
        @PathVariable("resourceId") resourceId: String,
        @PathVariable("subResourceId") subResourceId: String
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CREATED).body(createDynamicPathResponse(resourceId, subResourceId))
    }

    private fun createDynamicPathResponse(
        resourceId: String,
        subResourceId: String
    ): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("resourceId", resourceId)
            jsonObject.put("subResourceId", subResourceId)
            jsonObject.put("regexValue", UUID.randomUUID().toString())
            jsonObject.put("ignorableValue", UUID.randomUUID().toString())
            jsonObject.put("validDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date()))
        } catch (_: JSONException) {
        }
        return jsonObject.toString()
    }
}
