# Static Analysis Remediation Harness

Use this harness when fixing Detekt, SonarQube, formatting, or ktlint findings.

## Steps

1. Read the finding and locate the affected source.
2. Classify it as correctness, security, maintainability, style, or generated-code noise.
3. Prefer a minimal design/readability fix over suppression.
4. Ensure tests still cover behavior after refactoring.
5. Run the relevant static analysis command if available.
6. If suppressing, make suppression local and justify it.

## Do not

- Reformat unrelated files.
- Change behavior while claiming a style-only fix.
- Suppress entire files for one finding.
- Add global config relaxations for a local issue.
- Lower thresholds unless there is a documented project-wide reason.
