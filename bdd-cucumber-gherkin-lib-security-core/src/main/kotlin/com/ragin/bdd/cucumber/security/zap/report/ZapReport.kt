package com.ragin.bdd.cucumber.security.zap.report

/** What the library's own HTML report shows, read from a ZAP `traditional-json-plus` report. */
data class ZapReport(
    val zapVersion: String,
    val generated: String,
    val sites: List<String>,
    val alerts: List<ZapReportAlert>
)
