package com.ragin.bdd.cucumbertests.library.test

import jakarta.validation.Valid
import org.apache.commons.lang3.StringUtils
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class FormData {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/filespublic"],
        produces = ["application/json"],
        consumes = ["multipart/form-data"]
    )
    fun formDataRequest(
        @RequestParam(value = "fileContext", required = true) fileContext: String,
        @RequestParam(value = "identifier", required = true) identifier: String,
        @RequestPart("file") file: @Valid MultipartFile?
    ): ResponseEntity<String> {
        if (StringUtils.isNoneBlank(fileContext, identifier) && ! file!!.isEmpty) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createStatus(fileContext, identifier))
        }
        return ResponseEntity.badRequest().body("{}")
    }

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/filessecured"],
        produces = ["application/json"],
        consumes = ["multipart/form-data"]
    )
    fun formDataSecured(
        @RequestHeader("Authorization") authToken: String,
        @RequestParam(value = "fileContext", required = true) fileContext: String,
        @RequestParam(value = "identifier", required = true) identifier: String,
        @RequestPart("file") file: @Valid MultipartFile?
    ): ResponseEntity<String> {
        if (StringUtils.isNoneBlank(authToken, fileContext, identifier) && ! file!!.isEmpty) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createStatus(fileContext, identifier))
        }
        return ResponseEntity.badRequest().body("{}")
    }

    private fun createStatus(fileContext: String, identifier: String): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("fileContext", fileContext)
            jsonObject.put("identifier", identifier)
        } catch (_: JSONException) {
        }

        return jsonObject.toString()
    }
}
