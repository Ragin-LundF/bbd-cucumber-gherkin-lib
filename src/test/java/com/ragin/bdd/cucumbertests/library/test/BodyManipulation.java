package com.ragin.bdd.cucumbertests.library.test;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BodyManipulation {
    @PostMapping("/api/v1/body/manipulate")
    public ResponseEntity<String> stubBodyManipulation(
            final @NonNull @RequestBody StubBody body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createManipulatedBodyResponse(body));
    }

    private String createManipulatedBodyResponse(@NonNull final StubBody body) {
        final JSONObject jsonObject = new JSONObject();
        try {
            final JSONArray newIdsArray = new JSONArray();
            body.ids.forEach(newIdsArray::put);

            jsonObject.put("newName", body.getName());
            jsonObject.put("newIds", newIdsArray);
        } catch (JSONException je) {
        }
        return jsonObject.toString();
    }

    @Getter
    @Setter
    public static class StubBody {
        private String name;
        private List<String> ids;
    }
}
