package com.ragin.bdd.cucumbertests

import com.ragin.bdd.cucumber.constants.BddLibConfigConstants
import io.cucumber.junit.platform.engine.Constants
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.ExcludeTags
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.IncludeTags
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

/**
 * Runs the CVE scan of the dependencies of the dummy application of this repository, so that its
 * sentences are proven the same way every other sentence is.
 *
 * Separate from [CucumberRunner] on purpose: the scan needs Docker, pulls the scanner image and its
 * databases. The `cucumberCve` Gradle task is the only thing that executes this suite, and only when
 * it is asked for by name. It enables the scan through a system property, for the same reason as
 * [CucumberSecurityRunner].
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST_DATABASE +
        BddLibConfigConstants.Base.COMMA +
        BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY_CVE +
        BddLibConfigConstants.Base.COMMA +
        "com.ragin.bdd.cucumbertests.hooks"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "html:build/reports/cucumber-cve/cucumber.html, " +
        BddLibConfigConstants.Plugin.PLUGIN_PROPERTY_VALUES_DEFAULT
)
@IncludeTags("cveScan")
@ExcludeTags("ignore")
class CucumberCveRunner
