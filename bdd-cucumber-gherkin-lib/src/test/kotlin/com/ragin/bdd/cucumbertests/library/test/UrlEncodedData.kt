package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumbertests.extensions.toJsonString
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoint for the url-encoded POST sentence, which had no example before.
 */
@RestController
class UrlEncodedData {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/urlencoded"],
        produces = ["application/json"],
        consumes = ["application/x-www-form-urlencoded"]
    )
    fun urlEncodedRequest(
        @RequestParam(value = "identifier", required = true) identifier: String,
        @RequestParam(value = "fileContext", required = true) fileContext: String
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "identifier" to identifier,
                "fileContext" to fileContext
            ).toJsonString()
        )
    }
}
