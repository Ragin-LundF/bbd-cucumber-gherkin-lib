package com.ragin.bdd.cucumber.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cucumbertest")
data class BddProperties(
    val authorization : Authorization?,
    val proxy : Proxy?,
    val server: Server?,
    val ssl : SSL?,
    val scenarioContext : Map<String, String>?
) {
    data class Authorization(
        val bearerToken: AuthorizationBearer
    ) {
        /**
         * Authorization
         */
        data class AuthorizationBearer(
            val default : String,
            val noscope: String
        )
    }

    /**
     * Proxy
     */
    data class Proxy(
        val host : String = "http",
        val port : Int?
    )

    /**
     * Server
     */
    data class Server(
        val protocol : String = "http",
        val host : String?,
        val port : String?
    )

    /**
     * SSL
     */
    data class SSL(
        val disableCheck : Boolean = false
    )
}
