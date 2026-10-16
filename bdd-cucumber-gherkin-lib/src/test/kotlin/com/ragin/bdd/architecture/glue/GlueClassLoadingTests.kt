package com.ragin.bdd.architecture.glue

import com.ragin.bdd.cucumber.constants.BddLibConfigConstants.Base.BASE_PACKAGE
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
 *
 * Every class below the base package is checked, not only the glue packages of the library: projects
 * often configure a broader glue path (e.g. `com.ragin.bdd.cucumber.security`), which also pulls in
 * helpers like the HTML reports. Kotlin's inline comparator builders (`compareByDescending`, `thenBy`,
 * `sortedByDescending`, ...) produce such classes, the non-inline `compareBy(vararg selectors)` does not.
 */
internal class GlueClassLoadingTests {
    @Test
    internal fun `every class of the library can be inspected by Cucumber`() {
        val classNames = classNamesIn(basePackage = BASE_PACKAGE)
        assertTrue(actual = classNames.isNotEmpty(), message = "No classes found in $BASE_PACKAGE")

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

    private fun classNamesIn(basePackage: String): List<String> {
        val pattern = "classpath*:${basePackage.replace(oldChar = '.', newChar = '/')}/**/*.class"
        val metadataReaderFactory = CachingMetadataReaderFactory()

        return PathMatchingResourcePatternResolver().getResources(pattern).map { resource ->
            metadataReaderFactory.getMetadataReader(resource).classMetadata.className
        }
    }
}
