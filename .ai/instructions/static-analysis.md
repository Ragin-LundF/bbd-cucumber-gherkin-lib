# Static Analysis and Formatting

Use this file for Detekt, SonarQube, ktlint, `.editorconfig`, and lint cleanup.

## Tools

- Use Detekt for Kotlin static analysis.
- Use SonarQube standard rules where configured by the host project.
- Use the repository `.editorconfig` for formatting.
- The bundled `config/detekt.yml` contains project-specific overwrites and should be merged with the host project config when adopted.

## Suppression policy

Suppression is a last resort.

Before suppressing:

1. Understand the finding.
2. Try a small design or readability improvement.
3. Confirm behavior remains tested.
4. Add the narrowest possible suppression.
5. Document why the suppression is correct.

Never suppress findings to hide generated bad code, missing tests, security weaknesses, or rushed implementation.

If suppression is needed, it must use `@Suppress(<detekt reason>)`. For example: `@Suppress("MagicNumber")`.

## Cleanup policy

- Preserve behavior before refactoring style.
- Separate mechanical formatting changes from behavioral changes where possible.
- Do not reformat unrelated files unless the task is explicitly a formatting-only cleanup.
- Run or request the relevant Gradle/Maven checks before declaring completion.

## Expected verification commands

- Verification commands run (e.g. `./gradlew test detekt`, `./gradlew cucumber`).

If commands cannot be run, state exactly which commands were not run and why.
