package com.ragin.bdd.cucumbertests.library.test;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FormData {
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/api/v1/filespublic",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<String> formData(
            @RequestParam(value = "fileContext", required = true) final String fileContext,
            @RequestParam(value = "identifier", required = true) final String identifier,
            @Valid @RequestPart("file") final Resource file) {
        if (StringUtils.isNoneBlank(fileContext, identifier) && file.exists()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createStatus(fileContext, identifier));
        }
        return ResponseEntity.badRequest().body("{}");
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/api/v1/filessecured",
            produces = "application/json",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<String> formDataSecured(
            @NonNull @RequestHeader("Authorization") final String authToken,
            @RequestParam(value = "fileContext", required = true) final String fileContext,
            @RequestParam(value = "identifier", required = true) final String identifier,
            @Valid @RequestPart("file") final Resource file) {
        if (StringUtils.isNoneBlank(authToken, fileContext, identifier) && file.exists()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createStatus(fileContext, identifier));
        }
        return ResponseEntity.badRequest().body("{}");
    }

    private String createStatus(final String fileContext, final String identifier) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileContext", fileContext);
            jsonObject.put("identifier", identifier);
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }

}
