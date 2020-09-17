package com.ragin.bdd.cucumbertests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "src/test/resources"
        },
        glue = {
                "com.ragin.cucumber.glue",
                "com.ragin.cucumber.hooks",
                "com.ragin.bdd.cucumbertests.hooks"
        },
        plugin = {
                "json:target/reports/cucumber/cucumber.json",
                "html:target/reports/cucumber/cucumber.html"
        },
        tags = "not @ignore"
)
public class Runner {
}
