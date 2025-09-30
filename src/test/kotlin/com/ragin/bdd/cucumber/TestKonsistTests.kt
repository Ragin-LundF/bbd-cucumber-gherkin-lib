package com.ragin.bdd.cucumber

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.ext.list.declarations
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withInternalModifier
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationNamed
import com.lemonappdev.konsist.api.provider.KoAnnotationProvider
import com.lemonappdev.konsist.api.provider.modifier.KoVisibilityModifierProvider
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

internal class TestKonsistTests {
    @Test
    internal fun `Konsist Tests - Check that unit tests have correct filename`() {
        konsistDirectoryScope()
            .classes()
            .assertTrue {
                it.hasNameEndingWith(suffix = "Tests")
            }
    }

    @Test
    internal fun `Konsist Tests - Check that unit test classes are using Kotlin Test annotations`() {
        konsistDirectoryScope()
            .files
            .assertTrue {
                it.imports.any { import -> import.name == "kotlin.test.Test" }
            }

        konsistDirectoryScope()
            .classes()
            .withInternalModifier()
            .functions()
            .withInternalModifier()
            .assertTrue {
                it.hasAnnotationWithName("Test")
            }
    }

    @Test
    fun `Konsist Tests - Test classes should have all members private besides tests`() {
        konsistDirectoryScope()
            .classes()
            .declarations()
            .filterIsInstance<KoAnnotationProvider>()
            .withoutAnnotationNamed(
                names = listOf(
                    "Test",
                    "ParameterizedTest",
                    "RepeatedTest"
                )
            ).filterIsInstance<KoVisibilityModifierProvider>()
            .assertTrue { it.hasPrivateModifier }
    }

    private fun konsistDirectoryScope(): KoScope {
        return Konsist.scopeFromDirectories(
            paths = listOf(
                "src/test/kotlin/com/ragin/bdd/cucumber"
            )
        )
    }
}
