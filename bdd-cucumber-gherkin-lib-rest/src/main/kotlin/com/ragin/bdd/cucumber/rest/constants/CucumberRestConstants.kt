package com.ragin.bdd.cucumber.rest.constants

object CucumberRestConstants {
    object PlaceHolders {
        const val PLACEHOLDER_PORT_NONE = "none"
    }

    object UrlPath {
        const val PROTOCOL_SEPARATOR = "://"
        const val HTTP_PROTOCOL = "http"
        const val HTTPS_PROTOCOL = "https"
        const val HTTP_PROTOCOL_W_SEPARATOR = "$HTTP_PROTOCOL$PROTOCOL_SEPARATOR"
        const val HTTPS_PROTOCOL_W_SEPARATOR = "$HTTPS_PROTOCOL$PROTOCOL_SEPARATOR"
        const val PATH_SEPARATOR = "/"
    }

    object Services {
        /** Name Spring Boot uses for the main web server namespace. */
        const val SERVICE_NAME_SERVER = "server"

        /** Name Spring Boot uses for the actuator child context namespace. */
        const val SERVICE_NAME_MANAGEMENT = "management"

        /** Host that is assumed when neither the service nor `cucumbertest.server.host` names one. */
        const val DEFAULT_HOST = "localhost"

        /**
         * Matches the `local.<namespace>.port` properties that Spring Boot registers for every
         * web server it starts. The captured group is the namespace and becomes the service name.
         */
        const val LOCAL_PORT_PATTERN = "^local\\.(.+)\\.port$"

        const val PROPERTY_SERVLET_CONTEXT_PATH = "server.servlet.context-path"
        const val PROPERTY_WEBFLUX_BASE_PATH = "spring.webflux.base-path"
        const val PROPERTY_MANAGEMENT_BASE_PATH = "management.server.base-path"
        const val PROPERTY_MANAGEMENT_WEB_BASE_PATH = "management.endpoints.web.base-path"

        /** Start of a Spring property placeholder, left in place when it cannot be resolved. */
        const val PLACEHOLDER_PREFIX = "\${"

        /** Spring Boot default of `management.endpoints.web.base-path`. */
        const val DEFAULT_MANAGEMENT_WEB_BASE_PATH = "/actuator"
    }
}
