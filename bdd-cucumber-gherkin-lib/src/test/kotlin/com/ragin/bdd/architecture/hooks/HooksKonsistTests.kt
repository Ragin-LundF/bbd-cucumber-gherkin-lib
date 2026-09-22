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
        return Konsist.scopeFromPackage(
            packagee = "com.ragin.bdd.cucumber.hooks",
            sourceSetName = "main"
        ) + Konsist.scopeFromPackage(
            packagee = "com.ragin.bdd.cucumber.database.hooks",
            sourceSetName = "main"
        ) + Konsist.scopeFromPackage(
            packagee = "com.ragin.bdd.cucumber.security.hooks",
            sourceSetName = "main"
        )
    }
}
