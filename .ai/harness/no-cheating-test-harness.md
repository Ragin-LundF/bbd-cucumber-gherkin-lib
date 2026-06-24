# No-Cheating Test Harness

Use this harness to review tests for dishonest or low-value coverage.

## Red flags

Reject or rewrite tests that:

- Mock the class under test.
- Assert only `not null` for known outputs.
- Assert implementation details instead of behavior.
- Copy production logic into the expected-value calculation.
- Depend on execution order without a contract.
- Hide failing behavior with `@Disabled` or `@Ignore`, assumptions, early returns, environment checks, or swallowed exceptions.
- Remove meaningful assertions from existing tests.
- Change expected values without explaining the behavior change.

## Review questions

For each test, answer:

1. What production regression would this catch?
2. Would the test fail if the implementation returned a plausible wrong value?
3. Does the test verify the contract or only the implementation shape?
4. Are negative and edge cases covered?
5. Is the test deterministic?

## Mandatory action

If a test fails this harness, do not keep it for coverage. Rewrite it or explain why it is intentionally shallow, for example generated-code smoke testing.
