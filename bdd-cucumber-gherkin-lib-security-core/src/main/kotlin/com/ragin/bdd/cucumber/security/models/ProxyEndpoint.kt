package com.ragin.bdd.cucumber.security.models

/** Host and port of the HTTP proxy a security scanner listens on. */
data class ProxyEndpoint(
    val host: String,
    val port: Int
)
