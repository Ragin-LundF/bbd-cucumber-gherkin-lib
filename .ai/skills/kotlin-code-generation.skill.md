# Skill: Kotlin Code Generation

Use this skill when writing or changing Kotlin production code.

## Load first

- `../instructions/coding-guidelines.md`
- `../instructions/module-architecture.md` when modules or layers are involved.
- `../instructions/testing.md`

## Workflow

1. Identify the owning layer.
2. Add or update unit tests first when behavior is clear.
3. Implement with small block-body functions.
4. Use named arguments for Kotlin calls where helpful.
5. Keep domain logic in domain services.
6. Keep REST/event layers free of business logic.
7. Run or request tests and Detekt.

## Output expectations

- Production code is readable, focused, and immutable by default.
- Tests use `kotlin.test`.
- Public behavior is documented by tests.
