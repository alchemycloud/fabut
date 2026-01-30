# Pre-commit Assessment

Run before committing to check code quality and documentation freshness.

## Checks to Perform

1. **Documentation freshness**: If core files changed (Fabut.java, AssertableProcessor.java, Assertable.java), verify README.md and CLAUDE.md are updated

2. **Test coverage**: For each changed source file in `src/main/java`, verify corresponding test exists in `src/test/java`

3. **Run tests**: Execute `mvn test` and verify all pass

4. **Public API check**: Identify new public methods that may need documentation

## Instructions

1. Run `git diff --name-only HEAD` to see changed files
2. Check if any core files were modified
3. For each changed source file, verify test coverage
4. Run `mvn test`
5. Report results in this format:

```
📚 Documentation: ✓ or ⚠️ (list files needing update)
🧪 Test Coverage: ✓ or ⚠️ (list untested files)
🏃 Tests: ✓ PASSED (N tests) or ✗ FAILED
📋 New Public APIs: ✓ or ℹ️ (list new methods)
```
