package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Authentication {
    @Value("${cucumberTest.authorization.bearerToken.default}")
    private String token;

    @GetMapping("/api/v1/unauthenticated")
    public ResponseEntity<String> stubUnauthenticated() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @GetMapping("/api/v1/authorized")
    public ResponseEntity<String> stubAuthenticatedWithToken(
            final @NonNull @RequestHeader("Authorization") String authToken
    ) {
        if (authToken.equals("Bearer " + token)) {
            return ResponseEntity.ok().body(createAuthorizedResponse());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    private String createAuthorizedResponse() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", "ok");
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }

    private String createUnauthorizedResponse() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("error", "unauthorized");
            jsonObject.put("error_description", "Full authentication is required to access this resource");
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
