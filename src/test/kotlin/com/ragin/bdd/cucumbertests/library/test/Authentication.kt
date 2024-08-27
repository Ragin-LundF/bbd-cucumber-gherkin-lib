package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.config.BddProperties
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class Authentication(
    private val bddProperties: BddProperties
) {

    @GetMapping("/api/v1/unauthorized")
    fun stubUnauthorizedGet(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @DeleteMapping("/api/v1/unauthorized")
    fun stubUnauthorizedDelete(@RequestBody body: String?): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @PostMapping("/api/v1/unauthorized")
    fun stubUnauthorizedPost(@RequestBody body: String?): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @PutMapping("/api/v1/unauthorized")
    fun stubUnauthorizedPut(@RequestBody body: String?): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @PatchMapping("/api/v1/unauthorized")
    fun stubUnauthorizedPatch(@RequestBody body: String?): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @GetMapping("/api/v1/authorized")
    fun stubAuthenticatedWithTokenGet(
        @RequestHeader("Authorization") authToken: String
    ): ResponseEntity<String> {
        if (authToken == BEARER + bddProperties.authorization?.bearerToken?.default) {
            return ResponseEntity.ok().body(createAuthorizedResponse())
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @DeleteMapping("/api/v1/authorized")
    fun stubAuthenticatedWithTokenDelete(
        @RequestHeader("Authorization") authToken: String,
        @RequestBody body: String?
    ): ResponseEntity<String> {
        if (authToken == BEARER + bddProperties.authorization?.bearerToken?.default) {
            return ResponseEntity.ok().body(createAuthorizedResponse())
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @PostMapping("/api/v1/authorized")
    fun stubAuthenticatedWithTokenPost(
        @RequestHeader("Authorization") authToken: String,
        @RequestBody body: String?
    ): ResponseEntity<String> {
        if (authToken == BEARER + bddProperties.authorization?.bearerToken?.default) {
            return ResponseEntity.ok().body(createAuthorizedResponse())
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @PutMapping("/api/v1/authorized")
    fun stubAuthenticatedWithTokenPut(
        @RequestHeader("Authorization") authToken: String,
        @RequestBody body: String?
    ): ResponseEntity<String> {
        if (authToken == BEARER + bddProperties.authorization?.bearerToken?.default) {
            return ResponseEntity.ok().body(createAuthorizedResponse())
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    @PatchMapping("/api/v1/authorized")
    fun stubAuthenticatedWithTokenPatch(
        @RequestHeader("Authorization") authToken: String,
        @RequestBody body: String?
    ): ResponseEntity<String> {
        if (authToken == BEARER + bddProperties.authorization?.bearerToken?.default) {
            return ResponseEntity.ok().body(createAuthorizedResponse())
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse())
    }

    private fun createAuthorizedResponse(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("status", "ok")
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }

    private fun createUnauthorizedResponse(): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("error", "unauthorized")
            jsonObject.put("error_description", "Full authentication is required to access this resource")
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }

    companion object {
        private const val BEARER = "Bearer "
    }
}
