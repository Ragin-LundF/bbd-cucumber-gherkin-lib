package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestErrors {
    @GetMapping("/api/v1/error/{errorCode}")
    public ResponseEntity<String> stubError500(@PathVariable("errorCode") final Integer errorCode) {
        return ResponseEntity.status(errorCode).body(createErrorMessage());
    }

    private String createErrorMessage() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("error", "CONTAINS_AN_ERROR");
            jsonObject.put("message", "Something went wrong.");
        } catch (JSONException je) {
        }
        return jsonObject.toString();
    }
}
