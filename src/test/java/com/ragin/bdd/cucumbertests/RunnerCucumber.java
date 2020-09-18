package com.ragin.bdd.cucumbertests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "classpath:features"
        },
        glue = {
                "com.ragin.bdd.cucumber.glue",
                "com.ragin.bdd.cucumber.hooks",
                "com.ragin.bdd.cucumbertests.hooks"
        },
        plugin = {
                "json:target/reports/cucumber/cucumber.json",
                "html:target/reports/cucumber/cucumber.html"
        },
        publish = true, // publish it to https://reports.cucumber.io. Decide if you wish it!
        tags = "not @ignore"
)
public class RunnerCucumber {
}
