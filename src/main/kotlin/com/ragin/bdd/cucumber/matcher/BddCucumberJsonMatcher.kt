package com.ragin.bdd.cucumber.matcher

import org.hamcrest.BaseMatcher

interface BddCucumberJsonMatcher {
    fun matcherName(): String
    fun matcherClass(): Class<out BaseMatcher<*>>
}
