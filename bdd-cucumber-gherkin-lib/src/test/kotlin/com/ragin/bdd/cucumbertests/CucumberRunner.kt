package com.ragin.bdd.cucumbertests

import com.ragin.bdd.cucumber.constants.BddLibConfigConstants
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
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST_DATABASE +
        BddLibConfigConstants.Base.COMMA +
        "com.ragin.bdd.cucumbertests.hooks"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "json:build/reports/cucumber/cucumber.json, " +
        "html:build/reports/cucumber/cucumber.html, " +
        "junit:build/reports/cucumber/cucumber.xml, " +
        BddLibConfigConstants.Plugin.PLUGIN_PROPERTY_VALUES_DEFAULT
)
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true")
// @IncludeTags("custom_matcher")
// The scan scenarios have their own runners and Gradle tasks (CucumberSecurityRunner, CucumberCveRunner). Their glue is
// not registered here, so without the exclusion their steps would be undefined and fail this run. Only the scan
// scenarios themselves are excluded: @securityScan also marks the functional features that feed the scan.
@ExcludeTags("ignore", "securityExecuteScan", "cveScan")
class CucumberRunner
