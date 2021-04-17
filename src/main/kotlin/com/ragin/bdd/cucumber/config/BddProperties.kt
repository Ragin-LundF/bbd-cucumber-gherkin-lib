package com.ragin.bdd.cucumber.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
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
        @ConstructorBinding
        data class AuthorizationBearer(
            val default : String,
            val noscope: String
        )
    }

    /**
     * Proxy
     */
    @ConstructorBinding
    data class Proxy(
        val host : String = "http",
        val port : Int?
    )

    /**
     * Server
     */
    @ConstructorBinding
    data class Server(
        val protocol : String = "http",
        val host : String?,
        val port : String?
    )

    /**
     * SSL
     */
    @ConstructorBinding
    data class SSL(
        val disableCheck : Boolean = false
    )
}
