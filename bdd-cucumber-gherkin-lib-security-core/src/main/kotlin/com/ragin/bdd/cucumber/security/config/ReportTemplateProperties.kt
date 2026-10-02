package com.ragin.bdd.cucumber.security.config

/**
 * One report to write: which template renders it and the file it lands in below
 * [ReportProperties.outputDir].
 *
 * [template] is passed to the scanner as is - for ZAP every name of
 * https://www.zaproxy.org/docs/desktop/addons/report-generation/templates/ works, e.g.
 * `traditional-html-plus` or `traditional-xml` - except [ReportProperties.OWN_HTML_TEMPLATE],
 * which is the library's own all-in-one HTML report.
 */
data class ReportTemplateProperties(
    val template: String,
    val fileName: String
) {
    init {
        require(template.isNotBlank()) { "A report needs a template." }
        require(fileName.isNotBlank()) { "The report with template '$template' needs a fileName." }
    }
}
