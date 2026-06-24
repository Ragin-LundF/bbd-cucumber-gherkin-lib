# Testing Instructions

Use this file when adding, modifying, or reviewing tests.

## Mandatory testing principles

- High test coverage is mandatory for new and changed code.
- Tests must verify behavior, not merely execute lines.
- Every behavior change requires new or updated tests.
- Do not weaken, delete, skip, or rewrite tests just to make a change pass.
- Do not change production code solely to satisfy a brittle or artificial test unless the behavior change is required.
- Do not test implementation details when public behavior can be tested.
- Prefer small focused tests with clear names.

## Unit tests

- Use `kotlin.test` annotations and assertions when possible.
- Tests and the test class should always be `internal`.
- Helper functions which are no tests should be `private`. The only exception are `@BeforeTest` and `@AfterTest` functions.
- Use named arguments for assertions, for example `assertEquals(expected = expectedValue, actual = actualValue)`.
- Unit-test functions and parameter behavior directly.
- Cover positive paths, negative paths, edge cases, boundary values, nullability where valid, and error paths.
- Validate exact exception types and important messages only when those messages are part of the contract.
- Use test fixtures/builders to reduce noise, but keep inputs visible enough to understand the scenario.

## Gherkin Sentence Testing

- All Gherkin sentences must be tested with the Demo-Application under the `bdd-cucumber-gherkin-lib` module in `src/test/kotlin/com/ragin/bdd`.
- It is intended that this application and its REST-Controllers are located under `test`.
- No new sentences should be introduced. The library tests itself with its own public sentences only.
- The sentences in the tests are also used as examples for the library users. They should be clear and concise.

## Coverage expectations

- New logic should have near-complete branch coverage.
- Tests require explicit positive and negative tests.
- Do not chase coverage with meaningless tests. Add meaningful assertions or leave generated/trivial code excluded according to project policy.

## Anti-cheating rules

Never do the following:

- Assert only that a value is non-null when exact behavior is known.
- Test a copy of the implementation logic in the test.
- Use broad `any()` matchers for values that matter to behavior.
- Use `Thread.sleep` to make timing pass.
- Add `@Disabled`, `ignore`, assumptions, or conditional returns to avoid failing scenarios.
- Delete negative tests for validation/security/error cases.
- Replace integration tests with mocked unit tests without equivalent coverage.
- Reduce assertions after implementation changes.

## Unit Test naming

Use behavior-focused names. Good examples:

- `URI should return correct path without double slashes`
- `Placeholder resourceId was replaced with correct value`

Avoid names that only repeat method names.

## Completion criteria

A change is not complete until:

1. Unit tests cover changed function behavior and parameters.
2. REST API behavior is covered by Cucumber scenarios when relevant.
3. Negative, edge, and error paths are covered.
4. Tests fail without the production change or with a realistic regression.
5. Static analysis and formatting checks pass.
