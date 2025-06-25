package com.ragin.bdd.cucumbertests

import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.ExcludeTags
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.ragin.bdd.cucumber.glue, " +
            "com.ragin.bdd.cucumber.hooks, " +
            "com.ragin.bdd.cucumbertests.hooks"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "json:build/reports/cucumber/cucumber.json, " +
            "html:build/reports/cucumber/cucumber.html, " +
            "junit:build/reports/cucumber/cucumber.xml"
)
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true")
// @ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
@ExcludeTags("ignore")
class CucumberRunner
