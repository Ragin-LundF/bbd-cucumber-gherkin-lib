package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IBANMatcherCheck {
    @GetMapping("/api/v1/iban")
    fun returnIban(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(createResponse())
    }

    private fun createResponse(): String {
        val jsonObject = JSONObject()
        ibanList.forEach { iban: String ->
            try {
                jsonObject.put(iban.substring(0, 2), iban)
            } catch (_: JSONException) {
            }
        }

        return jsonObject.toString()
    }

    companion object {
        private val ibanList = listOf(
            "AT702040483252512583",
            "CH8889144635889488182",
            "DE13500105177816296336",
            "EE921271662289332351",
            "FO3585232853768988",
            "NL73INGB3670561447"
        )
    }
}
