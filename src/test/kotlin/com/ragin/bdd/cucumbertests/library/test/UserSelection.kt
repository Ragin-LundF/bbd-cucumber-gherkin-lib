package com.ragin.bdd.cucumbertests.library.test

import com.ragin.bdd.cucumber.utils.JacksonUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserSelection {
    @GetMapping("/api/v1/user/{username}")
    fun stubUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String?,
        @PathVariable("username") username: String?
    ): ResponseEntity<String> {
        if (token != null && username != null) {
            return ResponseEntity.status(HttpStatus.OK).body(
                createUserResponse(
                    token = token,
                    username = username
                )
            )
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}")
    }

    private fun createUserResponse(
        token: String,
        username: String
    ): String {
        return JacksonUtils.mapper.writeValueAsString(
            mapOf(
                "username" to username,
                "token" to token
            )
        )
    }
}
