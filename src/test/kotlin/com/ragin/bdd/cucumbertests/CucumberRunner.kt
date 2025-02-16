package com.ragin.bdd.cucumbertests

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = [
        "classpath:features"
    ],
    glue = [
        "com.ragin.bdd.cucumber.glue",
        "com.ragin.bdd.cucumber.hooks",
        "com.ragin.bdd.cucumbertests.hooks"
    ],
    plugin = [
        "json:build/reports/cucumber/cucumber.json",
        "html:build/reports/cucumber/cucumber.html",
        "junit:build/reports/cucumber/cucumber.xml"
    ],
    publish = true,
    tags = "not @ignore"
)
class CucumberRunner
