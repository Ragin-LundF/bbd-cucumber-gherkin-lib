package com.ragin.bdd.architecture.glue

import com.ragin.bdd.cucumber.constants.BddLibConfigConstants
import com.ragin.bdd.cucumber.constants.BddLibConfigConstants.Base.COMMA
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Cucumber 8 calls `getDeclaringClass()` on every class of a glue package to print glue hints.
 *
 * Kotlin can generate classes the JVM rejects there with `IncompatibleClassChangeError ... disagree
 * on InnerClasses attribute`, e.g. for a reified inline function that creates an anonymous object
 * inside a lambda (`runCatching { restTemplate.exchange<String>(...) }`). Such a class aborts the
 * whole Cucumber run of every project that uses the glue, so it must never end up in a glue package.
 */
internal class GlueClassLoadingTests {
    @Test
    internal fun `every class of the library glue packages can be inspected by Cucumber`() {
        val classNames = gluePackages().flatMap(::classNamesIn)
        assertTrue(actual = classNames.isNotEmpty(), message = "No classes found in the glue packages")

        val rejected = classNames.mapNotNull { className ->
            runCatching { Class.forName(className, false, javaClass.classLoader).declaringClass }
                .exceptionOrNull()
                ?.let { error -> "$className: $error" }
        }

        if (rejected.isNotEmpty()) {
            fail(
                message = "Classes the JVM rejects on getDeclaringClass():\n" +
                    rejected.joinToString(separator = "\n")
            )
        }
    }

    private fun gluePackages(): Set<String> {
        return listOf(
            BddLibConfigConstants.GLUE_PROPERTY_VALUES_REST_DATABASE,
            BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY,
            BddLibConfigConstants.GLUE_PROPERTY_VALUES_SECURITY_CVE
        ).flatMap { value -> value.split(COMMA) }
            .map(String::trim)
            .toSet()
    }

    private fun classNamesIn(gluePackage: String): List<String> {
        val pattern = "classpath*:${gluePackage.replace(oldChar = '.', newChar = '/')}/**/*.class"
        val metadataReaderFactory = CachingMetadataReaderFactory()

        return PathMatchingResourcePatternResolver().getResources(pattern).map { resource ->
            metadataReaderFactory.getMetadataReader(resource).classMetadata.className
        }
    }
}
