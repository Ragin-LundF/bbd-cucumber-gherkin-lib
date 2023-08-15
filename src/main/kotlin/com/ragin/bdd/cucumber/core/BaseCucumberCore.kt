package com.ragin.bdd.cucumber.core

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext.bearerToken
import com.ragin.bdd.cucumber.core.ScenarioStateContext.fileBasePath
import com.ragin.bdd.cucumber.utils.JsonUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets

@Component
open class BaseCucumberCore(protected val jsonUtils: JsonUtils, protected val bddProperties: BddProperties) {
    /**
     * Handle BearerToken
     *
     * @param defaultBearerToken default Bearer token
     */
    fun setDefaultBearerToken(defaultBearerToken: String?) {
        if (StringUtils.isNotEmpty(bddProperties.authorization?.bearerToken?.default)) {
            ScenarioStateContext.defaultBearerToken = bddProperties.authorization?.bearerToken?.default!!
        }
        if (StringUtils.isEmpty(bearerToken)) {
            bearerToken = defaultBearerToken
        }
    }

    fun getFilePath(path: String): String {
        return if (path.startsWith(KEYWORD_ABSOLUTE_PATH)) {
            var prefixLength = KEYWORD_ABSOLUTE_PATH.length
            if (path.startsWith("$KEYWORD_ABSOLUTE_PATH/")) {
                prefixLength += 1
            }
            path.substring(prefixLength)
        } else {
            fileBasePath + path
        }
    }

    /**
     * Read file and return content as String.
     * If the path contains the reserved word "absolutePath:" it tries to resolve the file from the classpath root.
     *
     * @param path              Path to file
     * @return                  Content of file as String
     * @throws IOException      Error while reading file
     */
    @Throws(IOException::class)
    protected fun readFileAsString(path: String): String {
        val name = getFilePath(path)
        val resourceAsStream = javaClass.classLoader.getResourceAsStream(name)
                ?: throw FileNotFoundException("Could not find the file $name in the class path.")
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
    }

    /**
     * Read file and return content as ByteArray.
     * If the path contains the reserved word "absolutePath:" it tries to resolve the file from the classpath root.
     *
     * @param path              Path to file
     * @return                  Content of file as ByteArray
     * @throws IOException      Error while reading file
     */
    @Throws(IOException::class)
    protected fun readFileAsByteArray(path: String): ByteArray {
        val name = getFilePath(path)
        val resourceAsStream = javaClass.classLoader.getResourceAsStream(name)
                ?: throw FileNotFoundException("Could not find the file $name in the class path.")
        return resourceAsStream.readBytes()
    }

    /**
     * Assert that JSONs are equal
     *
     * @param expected  expected JSON
     * @param actual    actual JSON
     */
    protected fun assertJSONisEqual(expected: String?, actual: String?) {
        jsonUtils.assertJsonEquals(expected, actual)
    }

    companion object {
        private const val KEYWORD_ABSOLUTE_PATH = "absolutePath:"
    }
}
