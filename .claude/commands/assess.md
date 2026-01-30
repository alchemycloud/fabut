# Pre-commit Assessment

Run before committing to check code quality, test coverage, and documentation freshness.

## Checks to Perform

1. **Run tests with coverage**: Execute `mvn test` (generates JaCoCo report)

2. **Parse coverage report**: Read `target/site/jacoco/jacoco.csv` and calculate:
   - Overall line coverage percentage
   - Overall branch coverage percentage
   - Per-class coverage for changed files

3. **Documentation freshness**: If core files changed (Fabut.java, AssertableProcessor.java, Assertable.java), verify README.md and CLAUDE.md are updated

4. **Test file mapping**: For each changed source file in `src/main/java`, verify corresponding test exists in `src/test/java`

5. **Public API check**: Identify new public methods that may need documentation

## Instructions

1. Run `git diff --name-only HEAD` to see changed files
2. Run `mvn test` to execute tests and generate coverage
3. Parse coverage from `target/site/jacoco/jacoco.csv`:
   ```bash
   # Calculate overall coverage from CSV
   cat target/site/jacoco/jacoco.csv | tail -n +2 | awk -F',' '{
     im+=$4; ic+=$5; bm+=$6; bc+=$7; lm+=$8; lc+=$9
   } END {
     printf "Lines: %.1f%% (%d/%d)\n", (lc/(lm+lc))*100, lc, lm+lc
     printf "Branches: %.1f%% (%d/%d)\n", (bc/(bm+bc))*100, bc, bm+bc
   }'
   ```
4. For changed files, show individual class coverage
5. Check if any core files were modified
6. Report results in this format:

```
📚 Documentation: ✓ or ⚠️ (list files needing update)
🧪 Test Coverage:
   Lines: XX.X% (covered/total)
   Branches: XX.X% (covered/total)
   ⚠️ Low coverage: ClassName (XX%) - if any class < 70%
🏃 Tests: ✓ PASSED (N tests) or ✗ FAILED
📋 New Public APIs: ✓ or ℹ️ (list new methods)
```

## Coverage Thresholds

| Level | Line Coverage | Status |
|-------|---------------|--------|
| Good  | ≥ 80%         | ✓      |
| OK    | 70-79%        | ⚠️      |
| Low   | < 70%         | ✗      |

## Example Output

```
📚 Documentation: ✓ No core file changes
🧪 Test Coverage:
   Lines: 85.2% (834/979)
   Branches: 77.8% (355/456)
   ✓ Fabut.java: 84.3%
   ✓ ReflectionUtil.java: 90.0%
   ⚠️ AssertableProcessor.java: 0% (not tested - annotation processor)
🏃 Tests: ✓ PASSED (206 tests)
📋 New Public APIs: ✓ None detected
```
