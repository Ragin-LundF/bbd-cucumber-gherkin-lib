# Code Change Harness

Use this harness before and during any non-trivial code change.

## Intake

1. Restate the requested behavior change in one sentence.
2. Identify touched modules and load the matching instruction files from `.ai/README.md`.
3. Identify public contracts that may be affected: REST API, DTOs, events, database schema, configuration, CLI, or libraries.
4. Identify required tests before implementation.

## Planning checklist

- What behavior exists today?
- What behavior should change?
- Which layer owns the change?
- Does a generated source need regeneration?
- Which unit tests must be added or updated?
- Which REST Cucumber scenarios must be added or updated?
- Which static analysis findings are likely?

## Implementation rules

- Make the smallest coherent change.
- Keep business logic in domain services.
- Avoid unrelated refactoring.
- Add tests close to the changed behavior.
- Run formatting/static analysis/test checks where possible.

## Completion response template

When reporting completion, include:

- Changed behavior.
- Tests added/updated.
- Verification commands run.
- Commands not run and why.
- Any remaining risk or follow-up.
