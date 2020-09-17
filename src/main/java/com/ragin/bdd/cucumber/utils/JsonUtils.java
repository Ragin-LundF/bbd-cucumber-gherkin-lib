package com.ragin.bdd.cucumber.utils;

import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.ragin.bdd.cucumber.core.Loggable;
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import com.ragin.bdd.cucumber.matcher.ScenarioStateContextMatcher;
import com.ragin.bdd.cucumber.matcher.ValidDateMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class used to work with JSON objects.
 */
@Component
public final class JsonUtils extends Loggable {
    @Autowired(required = false)
    Collection<BddCucumberJsonMatcher> jsonMatcher;

    /**
     * Assert that two JSON Strings are equal
     *
     * @param expectedJSON expected JSON as String
     * @param actualJSON   actual JSON as String
     */
    public void assertJsonEquals(final String expectedJSON, final String actualJSON) {
        try {
            Configuration configuration = JsonAssert.withTolerance(0)
                    .when(TREATING_NULL_AS_ABSENT)
                    .withMatcher("isValidDate", new ValidDateMatcher())
                    .withMatcher("isEqualToScenarioContext", new ScenarioStateContextMatcher());

            if (jsonMatcher != null && ! jsonMatcher.isEmpty()) {
                for (BddCucumberJsonMatcher matcher : jsonMatcher) {
                    try {
                        configuration = configuration.withMatcher(
                                matcher.matcherName(),
                                matcher.matcherClass().newInstance()
                        );
                    } catch (Exception e) {
                        LOG.error("Unable to instantiate the matcher [" + matcher.matcherName() + "]");
                    }
                }
            }

            JsonAssert.assertJsonEquals(
                    expectedJSON,
                    actualJSON,
                    configuration
            );
        } catch (AssertionError error) {
            final String minimizedExpected = minimizeJSON(expectedJSON);
            final String minimizedActual = minimizeJSON(actualJSON);
            LOG.error("JSON comparison failed.\nExpected:\n\t" + minimizedExpected + "\nActual:\n\t" + minimizedActual + "\n");

            // rethrow error to make the test fail
            throw error;
        }
    }

    /**
     * Remove an element from a JSON file.
     * <p>Implements the <b>Remove</b> operation as specified in
     * <a href="https://tools.ietf.org/html/rfc6902#section-4.2">RFC 6902</a></p>
     *
     * @param originalJson the JSON file in which the field should be removed
     * @param fieldPath    the JSON path to the field that should be removed
     * @return the JSON file as String without the field
     */
    public String removeJsonField(final String originalJson, final String fieldPath) {
        return editJsonField(originalJson, fieldPath, null);
    }

    /**
     * Edit an element from a JSON file.
     * <p>Implements the <b>Add</b> operation as specified in
     * <a href="https://tools.ietf.org/html/rfc6902#section-4.1">RFC 6902</a></p>
     *
     * @param originalJson the JSON file in which the field should be edited
     * @param fieldPath    the field path to the field that should be edited
     * @param newValue     the new value for the field
     * @return the JSON file with the edited field
     */
    public String editJsonField(final String originalJson, final String fieldPath, final String newValue) {
        // set a JSON path
        String fieldJSONPath = fieldPath;
        if (! fieldJSONPath.startsWith("$.")) {
            fieldJSONPath = "$." + fieldJSONPath;
        }

        // read document
        final DocumentContext documentContext = JsonPath.parse(originalJson);
        documentContext.set(fieldJSONPath, newValue);

        return documentContext.jsonString();
    }

    /**
     * Minimize JSON object to be better comparable.
     * <p>Actions:</p>
     * <ul>
     *     <li>Replace '\r' and '\n' with ''</li>
     *     <li>Replace '": ' with '":' (space between key/value)</li>
     *     <li>Trim everything</li>
     * </ul>
     *
     * @param json JSON string
     * @return minimized JSON string
     */
    private String minimizeJSON(final String json) {
        final String nullSafeJson = json != null ? json : "{}";
        return Arrays.stream(nullSafeJson.split("\n"))
                .map(line -> line.replace("\r", ""))
                .map(line -> line.replace("\": ", "\":"))
                .map(String::trim)
                .collect(Collectors.joining(""));
    }
}
