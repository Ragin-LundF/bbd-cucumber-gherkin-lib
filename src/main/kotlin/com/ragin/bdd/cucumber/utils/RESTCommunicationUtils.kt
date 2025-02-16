package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.core.ScenarioStateContext.bearerToken
import com.ragin.bdd.cucumber.core.ScenarioStateContext.headerValues
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import io.cucumber.datatable.DataTable
import org.assertj.core.api.Assertions
import org.springframework.http.HttpHeaders

/**
 * Utils class for common REST communication methods
 */
object RESTCommunicationUtils {
    /**
     * Create HTTP header
     *
     * @param addAuthorisation  true = add BearerToken | false = no Authorization header
     * @return  default Headers
     */
    @JvmStatic
    fun createHTTPHeader(addAuthorisation: Boolean): HttpHeaders {
        val headers = HttpHeaders()
        if (addAuthorisation && headerValues[HttpHeaders.AUTHORIZATION] == null) {
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
        }

        // set user specific headers if present
        if (headerValues.isNotEmpty()) {
            for (headerName in headerValues.keys) {
                headers.add(headerName, headerValues[headerName])
            }
        }

        if (! headerValues.containsKey("Content-Type")) {
            headers.add("Content-Type", "application/json")
        }

        if (! headerValues.containsKey("Accept")) {
            headers.add("Accept", "application/json")
        }

        return headers
    }

    /**
     * Prepare dynamic URL with data from datatable to exchange the dynamic values
     *
     * @param dataTable     DataTable from Cucumber file
     * @return              path with replaced values
     */
    @JvmStatic
    fun prepareDynamicURLWithDataTable(dataTable: DataTable): String {
        // Prepare request
        var path = uriPath

        // assert that given for path and body was previously done
        Assertions.assertThat(path).isNotNull
            .describedAs("No given path found")

        // Read datatable
        val dataTableRowList = dataTable.asMaps(String::class.java, String::class.java)
        for (stringStringMap in dataTableRowList) {
            // Try to resolve value from context map
            if (! stringStringMap["URI Values"].isNullOrEmpty()) {
                var uriValue = scenarioContextMap[stringStringMap["URI Values"]]
                // If context map knows nothing about the value, use value directly
                if (uriValue == null) {
                    uriValue = stringStringMap["URI Values"]
                }
                // replace path with URI key and URI value
                path = path.replace(
                    "{${stringStringMap["URI Elements"]}}",
                    uriValue!!
                )
            }
        }
        return path
    }
}
