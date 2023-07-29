package com.ragin.bdd.cucumbertests.library.test;

import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class IBANMatcherCheck {
    private static final List<String> ibanMap = List.of(
            "AT702040483252512583",
            "CH8889144635889488182",
            "DE13500105177816296336",
            "EE921271662289332351",
            "FO3585232853768988",
            "NL73INGB3670561447"
    );

    @GetMapping("/api/v1/iban")
    public ResponseEntity<String> returnIban() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(createResponse());
    }

    private String createResponse() {
        final JSONObject jsonObject = new JSONObject();
        ibanMap.forEach(iban -> {
            try {
                jsonObject.put(iban.substring(0, 2), iban);
            } catch (JSONException je) {
            }
        });

        return jsonObject.toString();
    }
}
