package com.ragin.bdd.cucumber.security.config

import java.time.Duration

/**
 * Reach of the active scan.
 *
 * The time budget is not configured here: it is part of the Gherkin sentence that starts the
 * scan, so a feature file states its own limit and nothing can silently override it.
 */
data class ScanProperties @JvmOverloads constructor(
    val pollInterval: Duration = Duration.ofSeconds(DEFAULT_POLL_INTERVAL_SECONDS),
    val recurse: Boolean = true,
    /** Also attack URLs the scanner does not consider part of a configured context. */
    val inScopeOnly: Boolean = false
) {
    private companion object {
        const val DEFAULT_POLL_INTERVAL_SECONDS = 10L
    }
}
