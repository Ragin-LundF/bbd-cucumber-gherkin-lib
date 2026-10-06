package com.ragin.bdd.cucumber.security.config

/** Where and how the human readable scan reports are written. */
data class ReportProperties @JvmOverloads constructor(
    val title: String = "Security scan",
    /**
     * Directory the reports are written to, absolute or relative to the working directory.
     *
     * Nothing here reads system properties: a project that creates this object itself hands the
     * directory over. With Spring it is `cucumbertest.security.report.output-dir` of the normal
     * configuration, where a system property of that name still overrides it.
     */
    val outputDir: String = ".",
    /**
     * Every report to write after the scan, each with its own template and file name.
     *
     * The default is the library's own all-in-one HTML report for people and CI servers, plus
     * `traditional-xml` for collecting the results of several projects.
     */
    val templates: List<ReportTemplateProperties> = listOf(
        ReportTemplateProperties(template = OWN_HTML_TEMPLATE, fileName = "security-report.html"),
        ReportTemplateProperties(template = "traditional-xml", fileName = "security-report.xml")
    ),
    /**
     * Also lists the suppressed alerts in every report.
     *
     * Suppressed means marked as false positive by the scanner: the alerts matched by an
     * `alerts.alert-filter` with the risk 'False Positive' and every rule of
     * `alerts.ignored-rule-ids`. Off by default, so the report shows only what is left to fix; turn
     * it on to review what was suppressed. An alert filter with another risk suppresses nothing -
     * those alerts are always reported at their new risk. The gate is not affected: it drops
     * suppressed alerts either way.
     */
    val includeSuppressedAlerts: Boolean = false
) {
    companion object {
        /**
         * The library's own report: one self-contained HTML file with every finding, its
         * description, solution, references and the evidence of each instance.
         */
        const val OWN_HTML_TEMPLATE = "bdd-modern-plus"
    }
}
