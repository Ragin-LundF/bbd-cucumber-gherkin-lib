package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserSelection {
    @GetMapping("/api/v1/user/{username}")
    public ResponseEntity<String> stubUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) final String token,
            @PathVariable("username") final String username
    ) {
        if (token != null && username != null) {
            return ResponseEntity.status(HttpStatus.OK).body(createUserResponse(token, username));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{}");
    }

    private String createUserResponse(
            @NonNull final String token,
            @NonNull final String username
    ) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("token", token);
        } catch (JSONException je) {
        }
        return jsonObject.toString();
    }
}
