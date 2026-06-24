# EditorConfig Style

Use the bundled `.editorconfig` as the formatting source of truth unless the host repository already has stricter project-specific settings.

## Global defaults

- Charset: UTF-8.
- Line endings: LF.
- Indentation: 4 spaces.
- Tab width: 4.
- Final newline: required.
- Max line length: 120.
- Continuation indent: 8.
- Formatter tags: `@formatter:off` and `@formatter:on`.

## Kotlin / ktlint notes

The bundled `.editorconfig` disables selected ktlint rules to avoid formatter issues for generated code:

- `ktlint_standard_no-wildcard-imports = disabled`
- `ktlint_standard_max-line-length = disabled`
- `ktlint_standard_enum-entry-name-case = disabled`

Do not use those disabled rules as a reason to write unreadable code. Keep imports, line lengths, and enum names clear in handwritten sources.

## Gherkin

- `*.feature` files use two-space indentation.
- Keep scenario steps readable and business-focused.

## Adoption

When adding this package to an existing repository:

1. Compare existing `.editorconfig` with the bundled file.
2. Preserve stricter repository-specific settings unless they conflict with required generated-code behavior.
3. Do not silently replace team-specific settings without review.
