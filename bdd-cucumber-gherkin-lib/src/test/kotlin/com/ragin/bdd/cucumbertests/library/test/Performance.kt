package com.ragin.bdd.cucumbertests.library.test

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Performance {
    @GetMapping("/api/v1/performance/bad")
    @Throws(InterruptedException::class)
    fun badPerformance(): ResponseEntity<String> {
        Thread.sleep(500)
        return ResponseEntity.ok().body("ok")
    }

    @GetMapping("/api/v1/performance/good")
    @Throws(InterruptedException::class)
    fun goodPerformance(): ResponseEntity<String> {
        return ResponseEntity.ok().body("ok")
    }
}
