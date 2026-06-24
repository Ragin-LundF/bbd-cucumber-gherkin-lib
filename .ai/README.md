# AI Instruction Index

This folder contains the canonical AI instructions for Kotlin/JVM projects.
The goal is progressive loading: read only what is needed for the current task instead of loading every instruction file.

## Always read first

- `instructions/coding-guidelines.md` when creating, editing, reviewing, or testing Kotlin code.
- `instructions/testing.md` when tests are created, modified, reviewed, or when behavior changes.

## Load by task scope

### Repository architecture, modules, package layout, cross-module behavior
Read:

- `instructions/module-architecture.md`

Use this for changes that affect module boundaries or Gradle/Maven module structure.

### REST API / Gherkin sentence tests
Read:

- `instructions/testing.md`

Gherkin sentences must be used when new sentences are introduced. If required write new RestController in `bdd-cucumber-gherkin-lib` in the `test` module.

### Unit tests and test coverage
Read:

- `instructions/testing.md`
- `harness/test-generation-harness.md`
- `harness/no-cheating-test-harness.md`

Use `kotlin.test` for function and parameter tests. High coverage is mandatory, but meaningful assertions matter more than line execution.

### Static analysis, Detekt, SonarQube, ktlint, formatting
Read:

- `instructions/static-analysis.md`
- `instructions/editorconfig-style.md`
- `harness/static-analysis-remediation-harness.md`

Use `config/detekt/detekt.yml` and `.editorconfig` from this package as the baseline unless the host repository already has stricter settings.

### Code review or refactoring
Read:

- `harness/code-change-harness.md`
- `harness/review-harness.md`

Preserve behavior unless the task explicitly asks for behavior changes. Refactor only with tests or with a clear, minimal safety net.

## Conflict resolution

1. Prefer explicit user instructions in the current task.
2. Prefer repository-local instructions over this reusable package.
3. Prefer the most specific `.ai/instructions/*.md` file over a general one.
4. Prefer module-specific rules over repository-wide rules.
5. Preserve behavior before refactoring style.
6. Never weaken tests, security, static analysis, or compatibility to make implementation easier.
7. Explain unresolved conflicts before changing architecture, behavior, or public contracts.

## Token-saving rule

Do not load every file in `.ai/` by default. Start with this index, then load only the files named above that match the current task.
