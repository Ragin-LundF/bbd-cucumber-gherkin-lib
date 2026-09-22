package com.ragin.bdd.cucumber.security.config

/**
 * Configuration of the BDD security scan, bound from the prefix `cucumbertest.security`.
 *
 * A plain data class on purpose: this module is consumed by projects on another Spring Boot
 * generation, so the binding annotation lives with whoever creates the bean, not here.
 *
 * Scanner independent on purpose as well: swapping the scanner implementation must not force a
 * project to rewrite its configuration. Only [ScannerProperties.image] names a concrete product.
 *
 * Everything is optional. With [enabled] false - the default - all hooks and steps are no-ops,
 * so the module can stay on the test classpath of the regular cucumber run.
 */
data class SecurityScanProperties @JvmOverloads constructor(
    /** Master switch. Set to `true` only in the security scan profile. */
    val enabled: Boolean = false,
    val scanner: ScannerProperties = ScannerProperties(),
    val target: TargetProperties = TargetProperties(),
    val api: ApiProperties = ApiProperties(),
    val scan: ScanProperties = ScanProperties(),
    val alerts: AlertProperties = AlertProperties(),
    val report: ReportProperties = ReportProperties(),
    val recording: RecordingProperties = RecordingProperties()
) {
    companion object {
        /** The configuration prefix this type binds from. */
        const val PREFIX = "cucumbertest.security"
    }
}
