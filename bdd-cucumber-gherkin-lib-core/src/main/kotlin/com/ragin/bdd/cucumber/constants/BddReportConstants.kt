package com.ragin.bdd.cucumber.constants

/**
 * Constants for everything the library reports into the Cucumber report.
 *
 * The media type decides how the Cucumber HTML report renders an attachment: `application/json` and
 * `text/plain` become a collapsed block titled with the attachment name. A plain `scenario.log` line
 * has no title and is always expanded, which is why it is used for the short summary lines only.
 */
object BddReportConstants {
    object MediaTypes {
        const val JSON = "application/json"
        const val TEXT = "text/plain"
    }

    object Markers {
        const val REQUEST = "→"
        const val RESPONSE = "←"
        const val POLL = "↻"
        const val DATABASE = "⛁"
        const val SCENARIO_START = "▶"
        const val SCENARIO_END = "◀"
        const val SCENARIO_FAILED = "✖"
    }

    object AttachmentNames {
        const val REQUEST_BODY = "Request body"
        const val RESPONSE_BODY = "Response body"
        const val REQUEST_HEADERS = "Request headers"
        const val RESPONSE_HEADERS = "Response headers"
        const val EXPECTED_BODY = "Expected body"
        const val ACTUAL_BODY = "Actual body"
        const val FORM_DATA = "Form data"
        const val SQL = "SQL"
        const val QUERY_RESULT = "Query result (CSV)"
        const val EXPECTED_QUERY_RESULT = "Expected (CSV)"
        const val FAILED_RESPONSE_BODY = "Response body of the failed scenario"
    }

    /**
     * Header names whose value is obfuscated before it reaches a log or a report.
     *
     * Compared case-insensitively. A usable bearer token in a shared HTML report or a CI log is a
     * leak, so this list is not configurable.
     */
    val OBFUSCATED_HEADERS = setOf(
        "authorization",
        "proxy-authorization",
        "cookie",
        "set-cookie",
        "x-api-key"
    )

    /**
     * Content subtypes whose body is described instead of attached.
     *
     * Attaching binary content as text produces noise, not information.
     */
    val NOT_LOGGABLE_SUBTYPES = setOf(
        "pdf",
        "octet-stream",
        "zip"
    )

    /** Marks a subtype that should be attached as JSON rather than as plain text. */
    const val JSON_SUBTYPE_MARKER = "json"
}
