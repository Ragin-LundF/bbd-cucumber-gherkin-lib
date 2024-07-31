package com.ragin.bdd.cucumbertests.library.test

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class FieldValidation {
    @GetMapping("/api/v1/fieldValidation")
    fun stubFieldValidation(): ResponseEntity<String> {
        return ResponseEntity.ok(createResponse())
    }

    private fun createResponse(): String {
        val jsonObject = JSONObject()
        try {
            val jsonArray = JSONArray()
            jsonArray.put("First")
            jsonArray.put("Second")

            val subObject = JSONObject()
            subObject.put("firstname", "John")
            subObject.put("lastname", "Doe")

            val listObject = JSONObject()
            listObject.put("first", 1)
            listObject.put("second", 2)

            val listObject2 = JSONObject()
            listObject2.put("first", 3)
            listObject2.put("second", 4)

            val objectArray = JSONArray()
            objectArray.put(listObject)
            objectArray.put(listObject2)

            jsonObject.put("string", "is a string")
            jsonObject.put("number", 12)
            jsonObject.put("boolean", true)
            jsonObject.put("list", jsonArray)
            jsonObject.put("object", subObject)
            jsonObject.put("uuid", UUID.randomUUID())
            jsonObject.put("objectList", objectArray)
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }
}
