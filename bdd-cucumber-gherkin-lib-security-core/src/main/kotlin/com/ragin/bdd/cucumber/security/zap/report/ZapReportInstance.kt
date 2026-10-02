package com.ragin.bdd.cucumber.security.zap.report

/** One place an alert was found, with the HTTP message that shows it. */
data class ZapReportInstance(
    val uri: String,
    val method: String,
    val parameter: String,
    val attack: String,
    val evidence: String,
    val otherInfo: String,
    val requestHeader: String,
    val requestBody: String,
    val responseHeader: String,
    val responseBody: String
)
