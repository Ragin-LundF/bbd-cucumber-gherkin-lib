package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomDateTime {
    @GetMapping("/api/v1/customDateTime")
    public ResponseEntity<String> stubCustomDateTime() {
        return ResponseEntity.status(HttpStatus.OK).body(createCustomDateTime());
    }


    private String createCustomDateTime() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("validDate", "2020.04.30");
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
