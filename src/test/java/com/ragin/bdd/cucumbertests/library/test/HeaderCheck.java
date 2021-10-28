package com.ragin.bdd.cucumbertests.library.test;

import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HeaderCheck {
    @GetMapping("/api/v1/header")
    public ResponseEntity<String> stubUnauthorizedGet() {
        return ResponseEntity.status(HttpStatus.OK)
                .header("X-TEST-HEADER", "present")
                .body(createResponse());
    }

    private String createResponse() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", "ok");
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
