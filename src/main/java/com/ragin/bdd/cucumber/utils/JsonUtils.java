package com.ragin.bdd.cucumber.utils;

import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.ragin.bdd.cucumber.BddLibConstants;
import com.ragin.bdd.cucumber.core.ScenarioStateContext;
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import com.ragin.bdd.cucumber.matcher.ScenarioStateContextMatcher;
import com.ragin.bdd.cucumber.matcher.UUIDMatcher;
import com.ragin.bdd.cucumber.matcher.ValidDateMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.extern.apachecommons.CommonsLog;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class used to work with JSON objects.
 */
@Component
@CommonsLog
public final class JsonUtils {
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
            Configuration configuration = createJsonAssertConfiguration();

            JsonAssert.assertJsonEquals(
                    expectedJSON,
                    actualJSON,
                    configuration
            );
        } catch (AssertionError error) {
            final String minimizedExpected = minimizeJSON(expectedJSON);
            final String minimizedActual = minimizeJSON(actualJSON);
            log.error("JSON comparison failed.\nExpected:\n\t" + minimizedExpected + "\nActual:\n\t" + minimizedActual + "\n");

            // rethrow error to make the test fail
            throw error;
        }
    }

    /**
     * Configure JSON Assert
     *
     * @return Configuration class with default configuration
     */
    private Configuration createJsonAssertConfiguration() {
        // base configuration
        Configuration configuration = JsonAssert.withTolerance(0)
                .when(TREATING_NULL_AS_ABSENT)
                .withMatcher("isValidDate", new ValidDateMatcher())
                .withMatcher("isValidUUID", new UUIDMatcher())
                .withMatcher("isEqualToScenarioContext", new ScenarioStateContextMatcher());

        // add additional options
        for (Option jsonOption : ScenarioStateContext.current().getJsonPathOptions()) {
            configuration = configuration.when(jsonOption);
        }

        // add additional matcher
        if (jsonMatcher != null && ! jsonMatcher.isEmpty()) {
            for (BddCucumberJsonMatcher matcher : jsonMatcher) {
                configuration = addMatcherConfiguration(configuration, matcher);
            }
        }

        return configuration;
    }

    /**
     * Add more matcher to existing configuration.
     *
     * @param configuration Configuration
     * @param matcher       Class implementation of BddCucumberJsonMatcher interface
     * @return              Configuration with added matcher
     */
    private Configuration addMatcherConfiguration(Configuration configuration, BddCucumberJsonMatcher matcher) {
        try {
            configuration = configuration.withMatcher(
                    matcher.matcherName(),
                    matcher.matcherClass().getDeclaredConstructor().newInstance()
            );
        } catch (Exception e) {
            log.error("Unable to instantiate the matcher [" + matcher.matcherName() + "]");
        }
        return configuration;
    }

    /**
     * Remove an element from a JSON file.
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
     * Validate an element from a JSON file.
     *
     * @param originalJson  the JSON file in which the field should be edited
     * @param fieldPath     the field path to the field that should be edited
     * @param expectedValue the new value for the field
     */
    @SuppressWarnings("squid:S5960")
    public void validateJsonField(final String originalJson, @NotNull final String fieldPath, @NotNull final String expectedValue) {
        // set a JSON path
        String fieldJSONPath = fieldPath;
        if (! fieldJSONPath.startsWith("$.")) {
            fieldJSONPath = "$." + fieldJSONPath;
        }

        // try to resolve value from context. If nothing was found, reset to original
        String expectedValueToCompare = ScenarioStateContext.current().getScenarioContextMap().get(expectedValue);
        if (expectedValueToCompare == null) {
            expectedValueToCompare = expectedValue;
        }

        // read document
        final DocumentContext documentContext = JsonPath.parse(originalJson);
        Object fieldValue = null;
        try {
            fieldValue = documentContext.read(fieldJSONPath, Object.class);
            assertFieldValidation(expectedValueToCompare, fieldValue.toString());
        } catch (PathNotFoundException pnfe) {
            if (BddLibConstants.BDD_LIB_NOT_EXISTS.equalsIgnoreCase(expectedValueToCompare)) {
                Assert.assertNull(fieldValue);
            } else {
                throw pnfe;
            }
        }
    }

    private void assertFieldValidation(final String expectedValueToCompare, final String fieldValue) {
        final Configuration assertConfig = createJsonAssertConfiguration();
        Optional<Matcher<?>> matcher = Optional.empty();
        Optional<String> matcherName = findMatcherName(expectedValueToCompare);
        if (matcherName.isPresent()) {
            matcher = Optional.ofNullable(assertConfig.getMatcher(matcherName.get()));
        }

        if (expectedValueToCompare.startsWith(BddLibConstants.BDD_LIB_NOT)) {
            if (matcher.isPresent()) {
                final boolean fieldMatches = matcher.get().matches(fieldValue);
                Assert.assertFalse(fieldMatches);
            } else {
                Assert.assertNotEquals(expectedValueToCompare.substring(BddLibConstants.BDD_LIB_NOT.length()), fieldValue);
            }
        } else {
            if (matcher.isPresent()) {
                final boolean fieldMatches = matcher.get().matches(fieldValue);
                Assert.assertTrue(fieldMatches);
            } else {
                Assert.assertEquals(expectedValueToCompare, fieldValue);
            }
        }
    }

    /**
     * Find custom matcher
     *
     * @param possibleMatcherName   string which possibly contains a matcher name
     * @return  if matcher name was found the matcher name
     */
    private Optional<String> findMatcherName(final String possibleMatcherName) {
        java.util.regex.Matcher matcher = BddLibConstants.BDD_LIB_MATCHER_PATTERN.matcher(possibleMatcherName);
        if (matcher.find() && matcher.groupCount() == 3) {
            return Optional.of(matcher.group(2));
        }
        return Optional.empty();
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
