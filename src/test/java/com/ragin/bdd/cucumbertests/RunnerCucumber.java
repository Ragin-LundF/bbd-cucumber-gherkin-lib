package com.ragin.bdd.cucumbertests;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ConfigurationParameters;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore"),
        @ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true"),
        @ConfigurationParameter(
                key = Constants.GLUE_PROPERTY_NAME,
                value = "com.ragin.bdd.cucumbertests.glue"),
        @ConfigurationParameter(
                key = Constants.PLUGIN_PROPERTY_NAME,
                value = "json:target/reports/cucumber/cucumber.json,html:target/reports/cucumber/cucumber.html"
        )
})
// Its also possible to use jUnits tags instead of ConfigurationParameter
// @ExcludeTags("ignore")
// @IncludeTags("all")
public class RunnerCucumber {
}
