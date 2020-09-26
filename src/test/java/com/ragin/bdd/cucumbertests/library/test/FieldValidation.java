package com.ragin.bdd.cucumbertests.library.test;

import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FieldValidation {
    @GetMapping("/api/v1/fieldValidation")
    public ResponseEntity<String> stubFieldValidation() {
        return ResponseEntity.ok(createResponse());
    }

    private String createResponse() {
        final JSONObject jsonObject = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("First");
            jsonArray.put("Second");

            JSONObject subObject = new JSONObject();
            subObject.put("firstname", "John");
            subObject.put("lastname", "Doe");

            jsonObject.put("string", "is a string");
            jsonObject.put("number", 12);
            jsonObject.put("boolean", true);
            jsonObject.put("list", jsonArray);
            jsonObject.put("object", subObject);
            jsonObject.put("uuid", UUID.randomUUID());
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
