package com.ragin.bdd.cucumbertests.library.test;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Polling {
    private static final AtomicInteger RUN_COUNTER_POLLING = new AtomicInteger(0);

    @GetMapping("/api/v1/polling")
    public ResponseEntity<String> polling() {
        if (RUN_COUNTER_POLLING.incrementAndGet() < 3) {
            return ResponseEntity.ok().body(createMessage("NOT_READY"));
        }
        RUN_COUNTER_POLLING.set(0);
        return ResponseEntity.ok().body(createMessage("SUCCESSFUL"));
    }

    @GetMapping("/api/v1/pollingAuth")
    public ResponseEntity<String> pollingAuthorized(final @NonNull @RequestHeader("Authorization") String authToken) {
        if (StringUtils.isEmpty(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createMessage("UNAUTHORIZED"));
        }

        if (RUN_COUNTER_POLLING.incrementAndGet() < 3) {
            return ResponseEntity.ok().body(createMessage("NOT_READY"));
        }
        RUN_COUNTER_POLLING.set(0);
        return ResponseEntity.ok().body(createMessage("SUCCESSFUL"));
    }

    @PostMapping("/api/v1/polling")
    public ResponseEntity<String> pollingPost(@RequestBody PollingRequest body) {
        if (RUN_COUNTER_POLLING.incrementAndGet() < 3) {
            return ResponseEntity.status(HttpStatus.TOO_EARLY).body(createMessage("NOT_READY"));
        }
        RUN_COUNTER_POLLING.set(0);
        if (StringUtils.isEmpty(body.getPostExample())) {
            return ResponseEntity.badRequest().body(createMessage("INVALID PARAMETER"));
        }
        return ResponseEntity.ok().body(createMessage("SUCCESSFUL"));
    }

    private String createMessage(final String message) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", message);
        } catch (JSONException ignored) {
        }

        return jsonObject.toString();
    }

    @Data
    public static class PollingRequest {
        String postExample;
    }
}
