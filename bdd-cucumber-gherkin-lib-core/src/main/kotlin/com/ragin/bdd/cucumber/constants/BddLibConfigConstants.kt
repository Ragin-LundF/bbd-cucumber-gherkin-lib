package com.ragin.bdd.cucumber.constants

import com.ragin.bdd.cucumber.constants.BddLibConfigConstants.Base.BASE_PACKAGE
import com.ragin.bdd.cucumber.constants.BddLibConfigConstants.Base.COMMA

object BddLibConfigConstants {
    object Base {
        const val COMMA = ","
        const val BASE_PACKAGE = "com.ragin.bdd.cucumber"
    }

    object Core {
        const val GLUE_PROPERTY_VALUES_HOOKS_CORE = "$BASE_PACKAGE.hooks"
    }

    object Database {
        const val GLUE_PROPERTY_VALUES_HOOKS_DATABASE = "$BASE_PACKAGE.database.hooks"
        const val GLUE_PROPERTY_VALUES_GLUE_DATABASE = "$BASE_PACKAGE.database.glue"
    }

    object Rest {
        const val GLUE_PROPERTY_VALUES_GLUE_REST = "$BASE_PACKAGE.rest.glue"
    }

    /**
     * Report plugins for the Cucumber runner.
     *
     * Cucumber has no way to register a plugin automatically, so these are appended to the plugins
     * a project already configures:
     *
     * ```
     * @ConfigurationParameter(
     *     key = Constants.PLUGIN_PROPERTY_NAME,
     *     value = "html:build/reports/cucumber/cucumber.html, " +
     *         BddLibConfigConstants.Plugin.PLUGIN_PROPERTY_VALUES_DEFAULT
     * )
     * ```
     *
     * Leaving them out costs only the scenario and step lines on the console. The request, response
     * and failure lines come from the logging framework and need no plugin at all.
     */
    object Plugin {
        const val BDD_PRETTY = "$BASE_PACKAGE.plugin.BddPrettyFormatter"

        const val PLUGIN_PROPERTY_VALUES_DEFAULT = "$BDD_PRETTY$COMMA summary"
    }

    const val GLUE_PROPERTY_VALUES_REST = Core.GLUE_PROPERTY_VALUES_HOOKS_CORE +
        COMMA +
        Rest.GLUE_PROPERTY_VALUES_GLUE_REST

    const val GLUE_PROPERTY_VALUES_DATABASE = Core.GLUE_PROPERTY_VALUES_HOOKS_CORE +
        COMMA +
        Database.GLUE_PROPERTY_VALUES_HOOKS_DATABASE +
        COMMA +
        Database.GLUE_PROPERTY_VALUES_GLUE_DATABASE

    const val GLUE_PROPERTY_VALUES_REST_DATABASE = Core.GLUE_PROPERTY_VALUES_HOOKS_CORE +
        COMMA +
        Rest.GLUE_PROPERTY_VALUES_GLUE_REST +
        COMMA +
        Database.GLUE_PROPERTY_VALUES_HOOKS_DATABASE +
        COMMA +
        Database.GLUE_PROPERTY_VALUES_GLUE_DATABASE
}
