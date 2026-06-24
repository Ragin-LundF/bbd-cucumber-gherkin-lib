# Test Generation Harness

Use this harness when generating or updating tests.

## Steps

1. Identify the unit under test and its public behavior.
2. List inputs, outputs, side effects, and error modes.
3. Create at least one positive path test.
4. Create negative tests for invalid inputs and rejected states.
5. Create edge/boundary tests for parameter limits, empty values, min/max values, and nullability where valid.
6. Use `kotlin.test` assertions with named arguments.
7. Ensure each test would fail for a realistic regression.
8. Remove duplicate or meaningless assertions.

## Required assertions

- Assert exact values when the contract defines them.
- Assert collection sizes and relevant contents.
- Assert exact status codes and error codes for REST tests.
- Assert both positive and negative authorization/security paths where applicable.

## Test data

- Keep fixtures minimal.
- Prefer builders only when repeated setup hides irrelevant noise.
- Avoid random data unless the seed is fixed or randomness is explicitly under test.
- Do not reuse one giant fixture for unrelated behavior.

## Quality gate

A generated test suite is unacceptable if it only increases line coverage without checking behavior.
