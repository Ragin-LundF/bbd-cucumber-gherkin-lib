package com.ragin.bdd.cucumber.security.models

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SecurityAlertTests {
    @Test
    internal fun `describes a finding that names no parameter`() {
        val alert = alert(parameter = "")

        assertEquals(
            expected = "[HIGH risk, MEDIUM confidence] SQL Injection (rule 40018) - POST http://localhost:8080/api",
            actual = alert.describe()
        )
    }

    @Test
    internal fun `appends the parameter a finding was raised on`() {
        val alert = alert(parameter = "keyword")

        assertEquals(
            expected = "[HIGH risk, MEDIUM confidence] SQL Injection (rule 40018) - " +
                "POST http://localhost:8080/api (parameter 'keyword')",
            actual = alert.describe()
        )
    }

    private fun alert(parameter: String): SecurityAlert {
        return SecurityAlert(
            ruleId = "40018",
            name = "SQL Injection",
            risk = SecurityRisk.HIGH,
            confidence = SecurityRisk.MEDIUM,
            url = "http://localhost:8080/api",
            method = "POST",
            parameter = parameter
        )
    }
}
