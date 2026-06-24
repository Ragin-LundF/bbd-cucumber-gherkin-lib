# Module Architecture

Use this file for module boundaries, package layout, adapters, REST/event input layers, and cross-module changes.

## Goal

Projects should have a consistent layered architecture so developers can switch between modules without relearning structure. Keep technical domains separated as their own Gradle/Maven subprojects with clear dependencies.


## Module Structure

| Module | Purpose |
|---|---|
| `bdd-cucumber-gherkin-lib-core` | Shared state (`ScenarioStateContext`), matchers, utils, hooks |
| `bdd-cucumber-gherkin-lib-rest` | REST glue steps (Given/When/Then), HTTP client, URL utils |
| `bdd-cucumber-gherkin-lib-db` | Database glue steps, Liquibase integration, CSV support |
| `bdd-cucumber-gherkin-lib-bom` | Bill of Materials for dependency management |
| `bdd-cucumber-gherkin-lib` | Integration test module (runs Cucumber scenarios) |

Dependencies between modules: `rest` → `core`, `db` → `core`.
