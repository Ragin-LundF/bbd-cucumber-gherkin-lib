# Kotlin Coding Guidelines

Use these rules for all Kotlin code unless a more specific repository instruction says otherwise.

## Kotlin style

- New services and new modules should be written in Kotlin.
- Use Kotlin null-safety deliberately. Do not use nullable types unless `null` is a valid domain state.
- Prefer `val` over `var`.
- Prefer immutable data structures and immutable data classes where practical.
- Use primary constructors and avoid boilerplate mapping constructors.
- Do not use Lombok in Kotlin code.
- Use named arguments whenever calling Kotlin functions, constructors, assertions, or builders where named arguments improve readability.
- Do not rely on named arguments for Java APIs or APIs where Kotlin named arguments are unavailable.
- Implement functions with block bodies. Do not use expression-body functions.
- Keep functions small and focused on one responsibility.
- Split functions that become too long, too deeply nested, or mix multiple responsibilities.
- Prefer early returns and guard clauses over deep nesting.
- Use `runCatching` instead of `try/catch` when it keeps behavior correct and readable.
- Use `try/catch` when `runCatching` would make behavior less clear or less correct, for example with `finally`, resource cleanup, cancellation propagation, or explicit exception flow.
- Avoid files with multiple top-level classes, interfaces, enums, or objects. Prefer one top-level declaration per file.
- Use descriptive variable, class, and function names.
- Add comments for complex logic, non-obvious decisions, trade-offs, or domain rules.
- Do not add comments that merely repeat obvious code.
- Follow the official Kotlin style guide where this package does not define a rule.
- Follow `.editorconfig` exactly for indentation, line endings, charset, final newlines, max line length, Gherkin indentation, and IDE formatter settings.

## Design expectations

- Classes, functions, and modules must have a single responsibility.
- Public APIs must be explicit, predictable, and easy to test.
- Preserve existing behavior unless the task explicitly asks for a behavior change.
- Do not hide complexity in overly generic abstractions.
- Prefer typed identifiers, value classes, enums, and sealed types over raw strings when a value has domain meaning.
- Keep generated code separate from handwritten code. Do not manually edit generated sources unless the repository explicitly requires it.

## Error handling

- Use domain-specific exceptions for unrecoverable structural failures.
- Use validation result objects or diagnostics for semantic validation errors that should be reported without aborting early.
- Do not swallow exceptions silently.

## Parameters and APIs

- Parameter names must describe intent, not implementation detail.
- Avoid boolean parameters that obscure behavior. Prefer expressive enums or separate functions where appropriate.
- Long parameter lists are tolerated only for generated code, DTO construction, or stable API boundaries. For handwritten domain logic, introduce a meaningful parameter object.

## Forbidden shortcuts

- Do not disable or suppress static analysis without a narrow justification in code or commit notes.
- Do not reduce visibility to make tests easier if production design becomes worse.
- Do not add unused production hooks only for tests.
- Do not change production behavior to satisfy a brittle test unless the behavior change is requested.
