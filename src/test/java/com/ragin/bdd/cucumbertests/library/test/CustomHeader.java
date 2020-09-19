package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomHeader {
    @GetMapping("/api/v1/customHeader")
    public ResponseEntity<String> stubCustomHeader(@RequestHeader("X-My-Custom-Header") final String header) {
        return ResponseEntity.status(HttpStatus.OK).body(createHeaderResponse(header));
    }

    @GetMapping("/api/v1/overwrittenAuthHeader")
    public ResponseEntity<String> stubAuthHeader(@RequestHeader("Authorization") final String header) {
        return ResponseEntity.status(HttpStatus.OK).body(createHeaderResponse(header));
    }

    private String createHeaderResponse(final String header) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("header", header);
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
