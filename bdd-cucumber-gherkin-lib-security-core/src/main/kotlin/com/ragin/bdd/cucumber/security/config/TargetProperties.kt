package com.ragin.bdd.cucumber.security.config

/** Where the application under test is reachable while the scan runs. */
data class TargetProperties @JvmOverloads constructor(
    /** How the application under test is reachable from inside the scanner container. */
    val host: String = "host.testcontainers.internal",
    /** Primary port. When `null`, the port the application actually bound is used. */
    val port: Int? = null,
    /** All host ports that must be reachable from the container (public, intranet, ...). */
    val exposedPorts: List<Int> = emptyList()
)
