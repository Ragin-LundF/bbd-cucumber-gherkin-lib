package com.ragin.bdd.cucumber.security.config

/** Optional API definitions that widen the attack surface beyond the recorded traffic. */
data class ApiProperties @JvmOverloads constructor(
    /**
     * API definitions to import before the scan.
     *
     * Optional: recorded traffic already defines the attack surface. Importing a definition
     * additionally covers endpoints that no scenario touches.
     */
    val definitionUrls: List<String> = emptyList()
)
