package com.ragin.bdd.cucumber.rest.utils

import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.urlBasePath
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.PlaceHolders.PLACEHOLDER_PORT_NONE
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.UrlPath.HTTPS_PROTOCOL_W_SEPARATOR
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.UrlPath.HTTP_PROTOCOL_W_SEPARATOR
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.UrlPath.PATH_SEPARATOR
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.UrlPath.PROTOCOL_SEPARATOR
import org.apache.commons.text.StringSubstitutor

object UrlUtils {

    /**
     * Constructs the full URL for the specified path by combining it with the given protocol, host, and port.
     * If the path already contains a protocol prefix (e.g., "http://" or "https://"), it is returned as is.
     *
     * @param path The relative or absolute path for which the full URL should be generated.
     * @param protocol The protocol to be used (e.g., "http" or "https"). Optional, can be null.
     * @param host The host name or IP address. Optional, can be null.
     * @param port The port number as a string. Optional, can be null.
     * @return A string representing the full URL composed of the provided components and the specified path.
     */
    fun fullURLFor(
        path: String,
        protocol: String? = null,
        host: String? = null,
        port: String? = null,
    ): String {
        if (path.startsWith(prefix = HTTP_PROTOCOL_W_SEPARATOR) ||
            path.startsWith(prefix = HTTPS_PROTOCOL_W_SEPARATOR)
        ) {
            return path
        }
        val basePath = StringBuilder()

        if (!protocol.isNullOrEmpty() && !host.isNullOrEmpty()) {
            basePath.append(protocol)
            basePath.append(PROTOCOL_SEPARATOR)
            basePath.append(host)
            if (PLACEHOLDER_PORT_NONE == port) {
                basePath.append(":").append(port)
            } else if (port != null && port.trim { it <= ' ' } != "") {
                basePath.append(":").append(port)
            }
        }

        return appendPathElements(path = basePath.toString(), urlBasePath, path)
    }

    /**
     * Replaces placeholders in the specified path with corresponding values from the scenario context map.
     *
     * @param path The string containing placeholders that need to be replaced.
     * @return A string with all placeholders replaced by their corresponding values.
     */
    fun replacePathPlaceholders(path: String): String {
        // Build StringSubstitutor
        val sub = StringSubstitutor(scenarioContextMap)

        // Replace
        return sub.replace(path)
    }

    /**
     * Appends multiple path elements to a given base path, ensuring proper handling of path separators.
     * This method normalizes the resulting path by avoiding duplicate or missing path separators.
     *
     * @param path The base path to which additional elements will be appended.
     * @param extendedPath A variable number of path elements to append to the base path. Nullable elements are ignored.
     * @return A string representing the combined path with appropriate path separators.
     */
    fun appendPathElements(path: String, vararg extendedPath: String?): String {
        return extendedPath.fold(initial = path) { currentPath, nextPath ->
            if (nextPath.isNullOrEmpty()) {
                currentPath
            } else {
                val pathEndsWithSeparator = currentPath.endsWith(suffix = PATH_SEPARATOR)
                val extendedStartsWithSeparator = nextPath.startsWith(prefix = PATH_SEPARATOR)

                when {
                    pathEndsWithSeparator && extendedStartsWithSeparator ->
                        currentPath + nextPath.dropWhile { it == PATH_SEPARATOR.first() }

                    pathEndsWithSeparator || extendedStartsWithSeparator ->
                        currentPath + nextPath

                    else ->
                        currentPath + PATH_SEPARATOR + nextPath
                }
            }
        }
    }
}
