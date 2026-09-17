package com.ragin.bdd.cucumber.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cucumbertest")
data class BddProperties(
    val authorization: Authorization?,
    val proxy: Proxy?,
    val server: Server?,
    val ssl: SSL?,
    val scenarioContext: Map<String, String> = hashMapOf(),
    val services: Map<String, Service> = emptyMap(),
    val logging: Logging = Logging()
) {
    data class Authorization(val bearerToken: AuthorizationBearer) {
        /**
         * Authorization
         */
        data class AuthorizationBearer(val default: String? = null, val noscope: String? = null,)
    }

    /**
     * Proxy
     */
    data class Proxy(val host: String = "http", val port: Int?)

    /**
     * Server
     */
    data class Server(val protocol: String = "http", val host: String?, val port: String?)

    /**
     * SSL
     */
    data class SSL(val disableCheck: Boolean = false)

    /**
     * Controls what the library reports about a request while a scenario runs.
     *
     * The summary lines (method, URL, status code, duration) are always reported. These switches only
     * decide how much detail is added next to them. Bodies and headers are attached to the Cucumber
     * report, where they are collapsed, so a large payload does not get in the way.
     */
    data class Logging(
        val requestBody: Boolean = true,
        val responseBody: Boolean = true,
        val headers: Boolean = false,
        val sql: Boolean = false,
        val prettyJson: Boolean = true,
        val maxBodyLength: Int = DEFAULT_MAX_BODY_LENGTH
    ) {
        companion object {
            const val DEFAULT_MAX_BODY_LENGTH = 8192
        }
    }

    /**
     * An additional web server of the application under test, addressable by a logical name.
     *
     * Every field is optional. A field that is left out falls back to the value that was discovered
     * from the Spring environment, so a service that only needs a different port can be configured
     * with that single property. Values may contain `${...}` placeholders, which are resolved
     * against the Spring environment when the request is executed and not when the properties are
     * bound. Random ports are only known after the server has started.
     *
     * `basePath` is prepended to every URL of this service. `pathPrefixes` only decides which
     * service a path is routed to and is never added to the URL. An empty `pathPrefixes` list
     * disables the automatic routing that would otherwise apply to this service.
     */
    data class Service(
        val protocol: String? = null,
        val host: String? = null,
        val port: String? = null,
        val basePath: String? = null,
        val pathPrefixes: List<String>? = null
    )
}
