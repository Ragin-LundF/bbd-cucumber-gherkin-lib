package com.ragin.bdd.cucumber.security.config

/** Export and replay of the traffic the functional scenarios produced. */
data class RecordingProperties @JvmOverloads constructor(
    /** Export the recorded traffic (HAR) after the run. */
    val export: Boolean = true,
    val exportPath: String = "build/reports/security/recording.har",
    /**
     * Host path of a previously exported recording.
     *
     * When set, the functional scenarios are skipped and the recording is replayed into the
     * scanner instead - useful to iterate on the scan without re-running the whole suite.
     */
    val replayFrom: String? = null
) {
    val replayEnabled: Boolean get() = !replayFrom.isNullOrBlank()
}
