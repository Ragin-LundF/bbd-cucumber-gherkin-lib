package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Authentication {
    @Value("${cucumberTest.authorization.bearerToken.default}")
    private String token;

    @GetMapping("/api/v1/unauthorized")
    public ResponseEntity<String> stubUnauthorizedGet() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @DeleteMapping("/api/v1/unauthorized")
    public ResponseEntity<String> stubUnauthorizedDelete(final @RequestBody String body) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @PostMapping("/api/v1/unauthorized")
    public ResponseEntity<String> stubUnauthorizedPost(final @RequestBody String body) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @PutMapping("/api/v1/unauthorized")
    public ResponseEntity<String> stubUnauthorizedPut(final @RequestBody String body) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @PatchMapping("/api/v1/unauthorized")
    public ResponseEntity<String> stubUnauthorizedPatch(final @RequestBody String body) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @GetMapping("/api/v1/authorized")
    public ResponseEntity<String> stubAuthenticatedWithTokenGet(
            final @NonNull @RequestHeader("Authorization") String authToken
    ) {
        if (authToken.equals("Bearer " + token)) {
            return ResponseEntity.ok().body(createAuthorizedResponse());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @DeleteMapping("/api/v1/authorized")
    public ResponseEntity<String> stubAuthenticatedWithTokenDelete(
            final @NonNull @RequestHeader("Authorization") String authToken,
            final @RequestBody String body
    ) {
        if (authToken.equals("Bearer " + token)) {
            return ResponseEntity.ok().body(createAuthorizedResponse());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @PostMapping("/api/v1/authorized")
    public ResponseEntity<String> stubAuthenticatedWithTokenPost(
            final @NonNull @RequestHeader("Authorization") String authToken,
            final @RequestBody String body
    ) {
        if (authToken.equals("Bearer " + token)) {
            return ResponseEntity.ok().body(createAuthorizedResponse());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @PutMapping("/api/v1/authorized")
    public ResponseEntity<String> stubAuthenticatedWithTokenPut(
            final @NonNull @RequestHeader("Authorization") String authToken,
            final @RequestBody String body
    ) {
        if (authToken.equals("Bearer " + token)) {
            return ResponseEntity.ok().body(createAuthorizedResponse());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createUnauthorizedResponse());
    }

    @PatchMapping("/api/v1/authorized")
    public ResponseEntity<String> stubAuthenticatedWithTokenPatch(
            final @NonNull @RequestHeader("Authorization") String authToken,
            final @RequestBody String body
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
