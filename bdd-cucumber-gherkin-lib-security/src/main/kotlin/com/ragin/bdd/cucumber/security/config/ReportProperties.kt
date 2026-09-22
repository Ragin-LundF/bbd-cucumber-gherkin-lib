package com.ragin.bdd.cucumber.security.config

/** Where and how the human readable scan report is written. */
data class ReportProperties(
    val template: String = "traditional-html",
    val title: String = "Security scan",
    /**
     * Directory the report is written to, absolute or relative to the working directory.
     *
     * A dedicated Gradle task usually points it at the root project directory, so the report
     * ends up next to the other top level build artifacts rather than buried in a module's
     * `build/`. The default only applies to runs that do not go through Gradle.
     */
    val outputDir: String = ".",
    val fileName: String = "security-report.html"
)
