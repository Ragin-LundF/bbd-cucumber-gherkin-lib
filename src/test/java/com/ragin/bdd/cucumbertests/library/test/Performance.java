package com.ragin.bdd.cucumbertests.library.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Performance {
    @GetMapping("/api/v1/performance/bad")
    public ResponseEntity<String> badPerformance() throws InterruptedException {
        Thread.sleep(500);
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping("/api/v1/performance/good")
    public ResponseEntity<String> goodPerformance() throws InterruptedException {
        return ResponseEntity.ok().body("ok");
    }
}
