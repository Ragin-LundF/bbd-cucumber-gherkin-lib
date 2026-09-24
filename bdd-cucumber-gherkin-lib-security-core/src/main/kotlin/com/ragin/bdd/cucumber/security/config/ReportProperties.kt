package com.ragin.bdd.cucumber.security.config

/** Where and how the human readable scan report is written. */
data class ReportProperties @JvmOverloads constructor(
    val template: String = "traditional-html",
    val title: String = "Security scan",
    /**
     * Directory the report is written to, absolute or relative to the working directory.
     *
     * Nothing here reads system properties: a project that creates this object itself hands the
     * directory over. With Spring it is `cucumbertest.security.report.output-dir` of the normal
     * configuration, where a system property of that name still overrides it.
     */
    val outputDir: String = ".",
    val fileName: String = "security-report.html"
)
