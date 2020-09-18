package com.ragin.bdd.cucumbertests.library.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PathManipulation {
    @PostMapping("/api/v1/{resourceId}/{subResourceId}")
    public ResponseEntity<String> stubDynamicPath(
            final @NonNull @PathVariable("resourceId") String resourceId,
            final @NonNull @PathVariable("subResourceId") String subResourceId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createDynamicPathResponse(resourceId, subResourceId));
    }

    private String createDynamicPathResponse(
            @NonNull final String resourceId,
            @NonNull final String subResourceId
    ) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("resourceId", resourceId);
            jsonObject.put("subResourceId", subResourceId);
            jsonObject.put("regexValue", UUID.randomUUID().toString());
            jsonObject.put("ignorableValue", UUID.randomUUID().toString());
            jsonObject.put("validDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        } catch (JSONException je) {
        }
        return jsonObject.toString();
    }
}
