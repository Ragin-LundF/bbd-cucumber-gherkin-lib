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
 * Runs the DAST security scan of the security module against the dummy application of this
 * repository, so that its sentences are proven the same way every other sentence is.
 *
 * Separate from [CucumberRunner] on purpose: the scan needs Docker, pulls a scanner image and
 * attacks the application, which has no place in the regular run. The `cucumberSecurity` Gradle
 * task is the only thing that executes this suite, and only when it is asked for by name.
 *
 * Two details differ from the setup a consuming project would write:
 *
 * * The scan configuration arrives as system properties from the Gradle task rather than through
 *   a Spring profile, because the context class of this repository pins its profile with
 *   `@ActiveProfiles`, which would win over `spring.profiles.active`.
 * * Features are selected from the classpath resource instead of a package, because that is how
 *   the feature files of this repository are laid out.
 *
 * Only features tagged `@securityScan` contribute traffic. The execution order is pinned to
 * lexical and the scan itself lives in `features/zzz_securityscan/`, so it runs after every
 * feature that seeds it.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.EXECUTION_ORDER_PROPERTY_NAME, value = "lexical")
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST_DATABASE +
        BddLibConfigConstants.Base.COMMA +
        BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY +
        BddLibConfigConstants.Base.COMMA +
        "com.ragin.bdd.cucumbertests.hooks"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "html:build/reports/cucumber-security/cucumber.html, " +
        BddLibConfigConstants.Plugin.PLUGIN_PROPERTY_VALUES_DEFAULT
)
@IncludeTags("securityScan")
@ExcludeTags("ignore")
class CucumberSecurityRunner
