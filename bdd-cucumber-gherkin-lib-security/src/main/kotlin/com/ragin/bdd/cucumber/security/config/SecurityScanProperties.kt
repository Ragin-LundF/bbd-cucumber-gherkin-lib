package com.ragin.bdd.cucumber.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration of the BDD security scan.
 *
 * Scanner independent on purpose: swapping the scanner implementation must not force a project
 * to rewrite its configuration. Only [ScannerProperties.image] names a concrete product.
 *
 * Everything is optional. With [enabled] false - the default - all hooks and steps are no-ops,
 * so the module can stay on the test classpath of the regular cucumber run.
 */
@ConfigurationProperties(prefix = "cucumbertest.security")
data class SecurityScanProperties(
    /** Master switch. Set to `true` only in the security scan profile. */
    val enabled: Boolean = false,
    val scanner: ScannerProperties = ScannerProperties(),
    val target: TargetProperties = TargetProperties(),
    val api: ApiProperties = ApiProperties(),
    val scan: ScanProperties = ScanProperties(),
    val alerts: AlertProperties = AlertProperties(),
    val report: ReportProperties = ReportProperties(),
    val recording: RecordingProperties = RecordingProperties()
)
