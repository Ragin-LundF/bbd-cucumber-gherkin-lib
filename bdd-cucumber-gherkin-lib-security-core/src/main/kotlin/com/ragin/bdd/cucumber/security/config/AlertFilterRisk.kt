package com.ragin.bdd.cucumber.security.config

/**
 * The risk an alert filter sets, named after the `newRisk` values of the ZAP Automation
 * Framework `alertFilter` job: 'False Positive', 'Info', 'Low', 'Medium', 'High'.
 *
 * Spring binds enums leniently, so `new-risk: False Positive` - spelled as in a ZAP plan - binds
 * onto [FALSE_POSITIVE].
 */
enum class AlertFilterRisk {
    FALSE_POSITIVE,
    INFO,
    LOW,
    MEDIUM,
    HIGH
}
