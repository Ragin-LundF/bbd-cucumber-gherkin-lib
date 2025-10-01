package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.utils.JacksonUtils
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
        return ResponseEntity.status(HttpStatus.CREATED).body(
            createDynamicPathResponse(
                resourceId = resourceId,
                subResourceId = subResourceId
            )
        )
    }

    private fun createDynamicPathResponse(
        resourceId: String,
        subResourceId: String
    ): String {
        return JacksonUtils.mapper.writeValueAsString(
            mapOf(
                "resourceId" to resourceId,
                "subResourceId" to subResourceId,
                "regexValue" to UUID.randomUUID().toString(),
                "ignorableValue" to UUID.randomUUID().toString(),
                "validDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
            )
        )
    }
}
