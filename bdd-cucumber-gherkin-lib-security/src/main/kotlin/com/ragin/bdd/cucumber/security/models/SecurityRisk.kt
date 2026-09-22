package com.ragin.bdd.cucumber.security.models

/**
 * Risk rating of a security finding, ordered so that a threshold comparison works.
 *
 * Scanner independent: every scanner reports some notion of severity, and its adapter maps
 * onto these four levels.
 */
enum class SecurityRisk(val level: Int) {
    INFORMATIONAL(level = 0),
    LOW(level = 1),
    MEDIUM(level = 2),
    HIGH(level = 3),
    ;

    companion object {
        /** Parses a severity as a scanner spells it, falling back to the least severe level. */
        fun fromLabel(value: String?): SecurityRisk {
            return when (value?.trim()?.uppercase()) {
                "INFORMATIONAL", "INFORMATIONAL (LOW)", "INFO", "FALSE POSITIVE" -> INFORMATIONAL
                "LOW" -> LOW
                "MEDIUM" -> MEDIUM
                "HIGH", "USER CONFIRMED" -> HIGH
                else -> INFORMATIONAL
            }
        }
    }
}
