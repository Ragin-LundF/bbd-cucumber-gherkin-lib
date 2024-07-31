package com.ragin.bdd.cucumbertests.library.test

import org.apache.commons.lang3.StringUtils
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicInteger

@RestController
class Polling {
    @GetMapping("/api/v1/polling")
    fun doPolling(): ResponseEntity<String> {
        if (RUN_COUNTER_POLLING.incrementAndGet() < 3) {
            return ResponseEntity.status(HttpStatus.TOO_EARLY).body(createMessage("NOT_READY"))
        }
        RUN_COUNTER_POLLING.set(0)
        return ResponseEntity.ok().body(createMessage("SUCCESSFUL"))
    }

    @GetMapping("/api/v1/pollingAuth")
    fun pollingAuthorized(@RequestHeader("Authorization") authToken: String): ResponseEntity<String> {
        if (StringUtils.isEmpty(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createMessage("UNAUTHORIZED"))
        }

        if (RUN_COUNTER_POLLING.incrementAndGet() < 3) {
            return ResponseEntity.status(HttpStatus.TOO_EARLY).body(createMessage("NOT_READY"))
        }
        RUN_COUNTER_POLLING.set(0)
        return ResponseEntity.ok().body(createMessage("SUCCESSFUL"))
    }

    @PostMapping("/api/v1/polling")
    fun pollingPost(@RequestBody body: PollingRequest): ResponseEntity<String> {
        if (RUN_COUNTER_POLLING.incrementAndGet() < 3) {
            return ResponseEntity.status(HttpStatus.TOO_EARLY).body(createMessage("NOT_READY"))
        }
        RUN_COUNTER_POLLING.set(0)
        if (body.postExample.isNullOrEmpty()) {
            return ResponseEntity.badRequest().body(createMessage("INVALID PARAMETER"))
        }
        return ResponseEntity.ok().body(createMessage("SUCCESSFUL"))
    }

    private fun createMessage(message: String): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("message", message)
        } catch (ignored: JSONException) {
        }

        return jsonObject.toString()
    }

    data class PollingRequest(
        var postExample: String? = null
    )

    companion object {
        private val RUN_COUNTER_POLLING = AtomicInteger(0)
    }
}
