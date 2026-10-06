package com.ragin.bdd.cucumber.security.config

import java.time.Duration

/** How the scanner itself is started - the only part of the configuration bound to a product. */
data class ScannerProperties @JvmOverloads constructor(
    /**
     * Image of the scanner to run.
     *
     * A floating tag keeps the rule set current but means two builds of the same commit can
     * report different findings; pin it to a version when a run has to be reproducible.
     */
    val image: String = "zaproxy/zap-stable:latest",
    val startupTimeout: Duration = Duration.ofMinutes(DEFAULT_STARTUP_TIMEOUT_MINUTES),
    /**
     * Scanner plugins to install on start-up.
     *
     * Empty by default because installing needs access to the scanner's marketplace, which
     * a locked-down build agent may not have.
     */
    val plugins: List<String> = emptyList(),
    /**
     * Lets the scanner start a headless browser for rules that need one (ZAP: DOM based XSS).
     *
     * Off by default: a REST API has no DOM to attack, and the scanner image usually cannot
     * launch the browser anyway, which only adds warnings and scan time.
     */
    val browserEnabled: Boolean = false,
    /**
     * Lets the scanner write the recovery log of its session database (ZAP: `database.recoverylog`).
     *
     * Off by default: the scanner container is thrown away after the run, so a log to recover the
     * session from only costs disk I/O and scan time. `true` restores ZAP's own default.
     */
    val databaseRecoveryLog: Boolean = false
) {
    private companion object {
        const val DEFAULT_STARTUP_TIMEOUT_MINUTES = 5L
    }
}
