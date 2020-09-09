package com.ragin.bdd.cucumber.utils;

import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.springframework.http.HttpHeaders;

/**
 * Utils class for common REST communication methods
 */
public final class RESTCommunicationUtils {
    private RESTCommunicationUtils() {}

    /**
     * Create HTTP header
     *
     * @param addAuthorisation  true = add BearerToken | false = no Authorization header
     * @return  default Headers
     */
    public static HttpHeaders createHTTPHeader(final boolean addAuthorisation) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Accept-Language", "en");
        headers.add("Content-Type", "application/json");

        if (addAuthorisation) {
            headers.add("Authorization", "Bearer " + ScenarioStateContext.current().getBearerToken());
        }

        return headers;
    }

    /**
     * Prepare dynamic URL with data from datatable to exchange the dynamic values
     *
     * @param dataTable     DataTable from Cucumber file
     * @return              path with replaced values
     */
    public static String prepareDynamicURLWithDataTable(final DataTable dataTable) {
        // Prepare request
        String path = ScenarioStateContext.current().getUriPath();

        // assert that given for path and body was previously done
        Assert.assertNotNull("No given path found", path);

        // Read datatable
        final List<Map<String, String>> dataTableRowList = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> stringStringMap : dataTableRowList) {
            // Try to resolve value from context map
            String uriValue = ScenarioStateContext.current().getScenarioContextMap().get(stringStringMap.get("URI Values"));
            // If context map knows nothing about the value, use value directly
            if (uriValue == null) {
                uriValue = stringStringMap.get("URI Values");
            }
            // replace path with URI key and URI value
            path = path.replace(
                    "{" + stringStringMap.get("URI Elements") + "}",
                    uriValue
            );
        }

        return path;
    }
}
