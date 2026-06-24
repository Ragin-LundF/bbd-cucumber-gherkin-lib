# Review Harness

Use this harness for code review, self-review, or PR preparation.

## Review order

1. Architecture: layer ownership and dependency direction.
2. Behavior: correctness of logic.
3. Tests: coverage, meaningful assertions, no cheating.
4. Static analysis: Detekt, SonarQube, formatting.
5. Maintainability: names, function size, duplication, comments.

## Findings format

For each finding, provide:

- Severity: blocker, major, minor, nit.
- Location.
- Issue.
- Why it matters.
- Concrete fix.

## Approval criteria

Approve only when:

- Behavior matches the requested change.
- Relevant tests exist and are meaningful.
- Static analysis and formatting are addressed.
- No security or compatibility regression is visible.
