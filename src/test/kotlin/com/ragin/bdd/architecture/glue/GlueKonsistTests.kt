package com.ragin.bdd.architecture.glue

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.verify.assertTrue
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import kotlin.test.Test

internal class GlueKonsistTests {
    @Test
    internal fun `Konsist Glue - Check consistence of file names`() {
        konsistDirectoryScope()
            .classes()
            .assertTrue {
                it.hasNameEndingWith(suffix = "Glue")
            }
    }

    @Test
    internal fun `Konsist Glue - Code must extend from BaseCucumberCore or BaseRESTExecutionGlue`() {
        konsistDirectoryScope()
            .classes()
            .assertTrue {
                it.hasParentOf(
                    names = listOf(
                        BaseCucumberCore::class
                    ),
                    indirectParents = true
                )
            }
    }

    private fun konsistDirectoryScope(): KoScope {
        return Konsist.scopeFromDirectories(
            paths = listOf(
                "src/main/kotlin/com/ragin/bdd/cucumber/glue"
            )
        )
    }
}
