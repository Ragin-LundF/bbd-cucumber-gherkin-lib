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
}
