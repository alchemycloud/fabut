# Migrate from Fabut 4.x to 5.0

Follow the MIGRATION.md instructions to migrate a project.

## Steps

1. Update dependency version to 5.0.0-RELEASE
2. Delete `PropertyPath` imports
3. Replace `Entity.CONSTANT` → `"camelCase"` in assertions
4. Remove `this` from `assertThat(this, obj)` → `assertThat(obj)`
5. Delete PropertyPath constants from model classes
6. (Optional) Add `@Assertable` and use generated builders
7. Run tests to verify

## Instructions

Read MIGRATION.md for detailed guidance and help the user migrate their codebase step by step.
