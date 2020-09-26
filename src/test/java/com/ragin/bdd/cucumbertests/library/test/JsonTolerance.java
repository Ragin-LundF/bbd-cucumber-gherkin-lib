package com.ragin.bdd.cucumbertests.library.test;

import org.json.JSONArray;
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
public class JsonTolerance {
    @GetMapping("/api/v1/jsonWithUnsortedArray")
    public ResponseEntity<String> stubJsonWithUnsortedList() {
        return ResponseEntity.ok(createUnorderedArrayResponse());
    }

    @GetMapping("/api/v1/jsonWithExtraFields")
    public ResponseEntity<String> stubJsonWithExtraFields() {
        return ResponseEntity.ok(createExtraFieldsResponse());
    }

    private String createExtraFieldsResponse() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("exists", "this field exists");
            jsonObject.put("isIgnored", "New field is ignored");
            jsonObject.put("isIgnoredToo", "New field is ignored too");
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }

    private String createUnorderedArrayResponse() {
        final JSONObject jsonObject = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            array.put("Last Element");
            array.put("First Element");
            array.put("Second Element");

            jsonObject.put("unsorted", array);
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
