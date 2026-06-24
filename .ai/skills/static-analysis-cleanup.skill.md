# Skill: Static Analysis Cleanup

Use this skill when fixing Detekt, SonarQube, ktlint, or formatting findings.

## Load first

- `../instructions/static-analysis.md`
- `../instructions/editorconfig-style.md`
- `../harness/static-analysis-remediation-harness.md`

## Workflow

1. Fix correctness and security findings before style findings.
2. Prefer code improvements over suppressions.
3. Keep suppressions narrow and justified.
4. Preserve behavior and tests.
5. Do not change global thresholds for local issues.
6. Run the relevant static analysis command when possible.
