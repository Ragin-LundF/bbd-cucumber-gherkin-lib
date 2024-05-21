package com.ragin.bdd.cucumber.matcher

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.springframework.stereotype.Component

/**
 * Matcher for valid IBAN.
 *
 * ${json-unit.matches:isValidIBAN}
 */
@Component
class IBANMatcher: BaseMatcher<String?>(), BddCucumberJsonMatcher {
    override fun matcherName(): String {
        return "isValidIBAN"
    }

    override fun matcherClass(): Class<out BaseMatcher<*>> {
        return this::class.java
    }

    @Suppress("LongMethod")
    override fun matches(actual: Any): Boolean {
        val ibanRegex = ("^AL\\d{10}[0-9A-Z]{16}\$|" +
                "^AD\\d{10}[0-9A-Z]{12}\$|" +
                "^AT\\d{18}\$|" +
                "^BH\\d{2}[A-Z]{4}[0-9A-Z]{14}\$|" +
                "^BE\\d{14}\$|" +
                "^BA\\d{18}\$|" +
                "^BG\\d{2}[A-Z]{4}\\d{6}[0-9A-Z]{8}\$|" +
                "^HR\\d{19}\$|" +
                "^CY\\d{10}[0-9A-Z]{16}\$|" +
                "^CZ\\d{22}\$|" +
                "^DK\\d{16}\$|" +
                "^FO\\d{16}\$|" +
                "^GL\\d{16}\$|" +
                "^DO\\d{2}[0-9A-Z]{4}\\d{20}\$|" +
                "^EE\\d{18}\$|" +
                "^FI\\d{16}\$|" +
                "^FR\\d{12}[0-9A-Z]{11}\\d{2}\$|" +
                "^GE\\d{2}[A-Z]{2}\\d{16}\$|" +
                "^DE\\d{20}\$|" +
                "^GI\\d{2}[A-Z]{4}[0-9A-Z]{15}\$|" +
                "^GR\\d{9}[0-9A-Z]{16}\$|" +
                "^HU\\d{26}\$|" +
                "^IS\\d{24}\$|" +
                "^IE\\d{2}[A-Z]{4}\\d{14}\$|" +
                "^IL\\d{21}\$|" +
                "^IT\\d{2}[A-Z]\\d{10}[0-9A-Z]{12}\$|" +
                "^[A-Z]{2}\\d{5}[0-9A-Z]{13}\$|" +
                "^KW\\d{2}[A-Z]{4}22!\$|" +
                "^LV\\d{2}[A-Z]{4}[0-9A-Z]{13}\$|" +
                "^LB\\d{6}[0-9A-Z]{20}\$|" +
                "^LI\\d{7}[0-9A-Z]{12}\$|" +
                "^LT\\d{18}\$|" +
                "^LU\\d{5}[0-9A-Z]{13}\$|" +
                "^MK\\d{5}[0-9A-Z]{10}\\d{2}\$|" +
                "^MT\\d{2}[A-Z]{4}\\d{5}[0-9A-Z]{18}\$|" +
                "^MR13\\d{23}\$|" +
                "^MU\\d{2}[A-Z]{4}\\d{19}[A-Z]{3}\$|" +
                "^MC\\d{12}[0-9A-Z]{11}\\d{2}\$|" +
                "^ME\\d{20}\$|" +
                "^NL\\d{2}[A-Z]{4}\\d{10}\$|" +
                "^NO\\d{13}\$|" +
                "^PL\\d{10}[0-9A-Z]{16}\$|" +
                "^PT\\d{23}\$|" +
                "^RO\\d{2}[A-Z]{4}[0-9A-Z]{16}\$|" +
                "^SM\\d{2}[A-Z]\\d{10}[0-9A-Z]{12}\$|" +
                "^SA\\d{4}[0-9A-Z]{18}\$|" +
                "^RS\\d{20}\$|" +
                "^SK\\d{22}\$|" +
                "^SI\\d{17}\$|" +
                "^ES\\d{22}\$|" +
                "^SE\\d{22}\$|" +
                "^CH\\d{7}[0-9A-Z]{12}\$|" +
                "^TN59\\d{20}\$|" +
                "^TR\\d{7}[0-9A-Z]{17}\$|" +
                "^AE\\d{21}\$|" +
                "^GB\\d{2}[A-Z]{4}\\d{14}\$").toRegex()
        if (actual is String) {
            val actualString = actual.toString()
            return actualString.matches(ibanRegex)
        }
        return false
    }

    override fun describeTo(description: Description) {
        description.appendText("IBAN matching")
    }
}
