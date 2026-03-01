package com.ragin.bdd.cucumber.utils

import com.ragin.bdd.cucumber.BddLibConstants.BDD_URI_ELEMENTS
import com.ragin.bdd.cucumber.BddLibConstants.BDD_URI_VALUES
import com.ragin.bdd.cucumber.core.ScenarioStateContext.bearerToken
import com.ragin.bdd.cucumber.core.ScenarioStateContext.headerValues
import com.ragin.bdd.cucumber.core.ScenarioStateContext.scenarioContextMap
import com.ragin.bdd.cucumber.core.ScenarioStateContext.uriPath
import io.cucumber.datatable.DataTable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import kotlin.test.assertNotNull

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

        if (! headerValues.containsKey(CONTENT_TYPE)) {
            headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        }

        if (! headerValues.containsKey(ACCEPT)) {
            headers.add(ACCEPT, APPLICATION_JSON_VALUE)
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
        assertNotNull(
            actual = path,
            message = "No given path found"
        )

        // Read datatable
        val dataTableRowList = dataTable.asMaps(String::class.java, String::class.java)
        for (stringStringMap in dataTableRowList) {
            // Try to resolve value from context map
            if (! stringStringMap[BDD_URI_VALUES].isNullOrEmpty()) {
                var uriValue = scenarioContextMap[stringStringMap[BDD_URI_VALUES]]
                // If context map knows nothing about the value, use value directly
                if (uriValue == null) {
                    uriValue = stringStringMap[BDD_URI_VALUES]
                }
                // replace path with URI key and URI value
                path = path.replace(
                    oldValue = "{${stringStringMap[BDD_URI_ELEMENTS]}}",
                    newValue = uriValue!!
                )
            }
        }
        return path
    }
}
