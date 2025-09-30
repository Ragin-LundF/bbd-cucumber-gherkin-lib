package com.ragin.bdd.architecture.hooks

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

internal class HooksKonsistTests {
    @Test
    internal fun `Konsist Hooks - Check consistency of file names`() {
        konsistDirectoryScope()
            .classes()
            .assertTrue {
                it.hasNameEndingWith(suffix = "Hooks")
            }
    }

    private fun konsistDirectoryScope(): KoScope {
        return Konsist.scopeFromDirectories(
            paths = listOf(
                "src/main/kotlin/com/ragin/bdd/cucumber/hooks"
            )
        )
    }
}
