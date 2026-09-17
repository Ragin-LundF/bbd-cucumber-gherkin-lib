package com.ragin.bdd.cucumber.rest.utils

/**
 * A single web server of the application under test, addressable by a logical name.
 *
 * Built by [ServiceUrlResolver] by merging what was discovered from the Spring environment with
 * what was configured under `cucumbertest.services`. All placeholders are already resolved.
 */
data class ResolvedService(
    val name: String,
    val protocol: String,
    val host: String,
    val port: String?,
    val basePath: String,
    val pathPrefixes: List<String>
)
