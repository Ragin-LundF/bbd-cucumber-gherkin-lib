package com.ragin.bdd.cucumber.matcher

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

internal class MatcherKonsistTests {
    @Test
    internal fun `Konsist Matcher - Check consistence of file names`() {
        konsistDirectoryScope()
            .classes()
            .assertTrue {
                it.hasNameEndingWith(suffix = "Matcher")
            }
    }

    @Test
    internal fun `Konsist Matcher - Code must implement from BddCucumberJsonMatcher`() {
        konsistDirectoryScope()
            .classes()
            .assertTrue {
                it.hasParentOf(
                    names = listOf(
                        BddCucumberJsonMatcher::class
                    ),
                    indirectParents = true
                )
            }
    }

    private fun konsistDirectoryScope(): KoScope {
        return Konsist.scopeFromDirectories(
            paths = listOf(
                "src/main/kotlin/com/ragin/bdd/cucumber/matcher"
            )
        )
    }
}
