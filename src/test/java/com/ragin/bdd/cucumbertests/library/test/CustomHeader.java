package com.ragin.bdd.cucumbertests.library.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
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

    @GetMapping("/api/v1/allHeaders")
    public ResponseEntity<String> allHeaders(@RequestHeader final HttpHeaders headers) {
        return ResponseEntity.status(HttpStatus.OK).body(createHeadersResponse(headers));
    }

    private String createHeaderResponse(final String header) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("header", header);
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }

    private String createHeadersResponse(final HttpHeaders headers) {
        final JSONObject jsonObject = new JSONObject();
        try {
            for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String headerValueStr = entry.getValue().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(";"));
                jsonObject.put(entry.getKey(), headerValueStr);
            }
        } catch (JSONException je) {
        }

        return jsonObject.toString();
    }
}
