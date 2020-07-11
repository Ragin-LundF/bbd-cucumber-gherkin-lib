package com.ragin.bdd.cucumber.utils;

import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import com.ragin.bdd.cucumber.core.Loggable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Utility class used to work with JSON objects.
 * <p>This class offers the following <code>static</code> methods:
 * <ul>
 * <li>{@link #assertJsonEquals(String, String)}</li>
 * <li>{@link #removeJsonField(String, String)}</li>
 * <li>{@link #editJsonField(String, String, String)}</li>
 * </ul>
 */
public final class JsonUtils extends Loggable {
    private JsonUtils() {
    }

    /**
     * Assert that two JSON Strings are equal
     *
     * @param expectedJSON  expected JSON as String
     * @param actualJSON    actual JSON as String
     */
    public static void assertJsonEquals(String expectedJSON, String actualJSON) {
        try {
            JsonAssert.assertJsonEquals(
                    expectedJSON,
                    actualJSON,
                    JsonAssert
                            .withMatcher("isValidDate", new ValidDateMatcher())
                            .when(TREATING_NULL_AS_ABSENT)
                            .withTolerance(0)
            );
        } catch (AssertionError error) {
            String minimizedExpected = JsonUtils.minimizeJSON(expectedJSON);
            String minimizedActual = JsonUtils.minimizeJSON(actualJSON);
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
     * @param originalJson  the JSON file in which the field should be removed
     * @param fieldPath     the JSON path to the field that should be removed
     * @return              the JSON file as String without the field
     */
    public static String removeJsonField(final String originalJson, final String fieldPath) {
        JsonReader jsonReader = Json.createReader(new StringReader(originalJson));
        JsonObject json = jsonReader.readObject();
        jsonReader.close();

        JsonArray patchOps = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                .add("op", "remove")
                .add("path", fieldPath)
        ).build();

        return Json.createPatch(patchOps).apply(json).toString();
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
    public static String editJsonField(final String originalJson, final String fieldPath, final String newValue) {
        JsonReader jsonReader = Json.createReader(new StringReader(originalJson));
        JsonObject json = jsonReader.readObject();
        jsonReader.close();

        JsonArray patchOps = Json.createArrayBuilder().add(Json.createObjectBuilder()
                .add("op", "add")
                .add("path", fieldPath)
                .add("value", newValue)
        ).build();

        return Json.createPatch(patchOps).apply(json).toString();
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
     * @return     minimized JSON string
     */
    private static String minimizeJSON(final String json) {
        String nullSafeJson = json != null ? json : "{}";
        return Arrays.stream(nullSafeJson.split("\n"))
                .map(line -> line.replace("\r", ""))
                .map(line -> line.replace("\": ", "\":"))
                .map(String::trim)
                .collect(Collectors.joining(""));
    }
}
