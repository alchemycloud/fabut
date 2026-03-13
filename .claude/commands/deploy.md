# Deploy Release

Deploy a new version of Fabut to Maven Central.

## Instructions

### 1. Documentation Audit (BLOCKING)

Before anything else, verify ALL documentation is 100% up to date with the current code.

Read every markdown file and cross-check against the actual generated API:

| File | Check |
|------|-------|
| `README.md` | All code examples use current factory methods (`created`/`updated`/`deleted`) and snake_case field methods (`field_is`, `field_is_null`, etc.) |
| `CLAUDE.md` | Generated Builder API section, Methods Per Field table, Test Patterns, Anti-Patterns all match current code |
| `MIGRATION.md` | Target version matches, "NEW" examples use current API, regex patterns are correct |

To verify, read `AssertableProcessor.java` `generateFieldMethods()` and `generateBuilderClass()` to confirm what the processor actually generates, then compare against every code example in the docs.

**If ANY doc is stale, fix it before proceeding. Do NOT deploy with outdated documentation.**

### 2. Version Increment

Increment the version in `pom.xml`:
- **Patch** (5.1.0 → 5.1.1): bug fixes only
- **Minor** (5.1.0 → 5.2.0): new features, non-breaking API changes
- **Major** (5.1.0 → 6.0.0): breaking API changes

Ask the user which version bump is appropriate if unclear.

### 3. Full Build

```bash
mvn clean install
```

All tests must pass. Zero tolerance for failures.

### 4. Deploy

```bash
mvn deploy
```

### 5. Post-Deploy

- Verify the version in `pom.xml` matches what was deployed
- Confirm the artifact is available in the remote repository

### 6. Commit and Push

After successful deploy, commit and push both projects:

#### 6a. Commit fabut project

Stage all changed files in the fabut project and commit with message format:
```
Deploy X.Y.Z-RELEASE: <brief description of changes>
```

Then push to remote:
```bash
git push
```

#### 6b. Commit and push repo project

The `../repo` project (gh-pages branch) contains the deployed Maven artifacts. After `mvn deploy`, it will have new/modified files under `repo/cloud/alchemy/fabut/`.

```bash
cd ../repo
git add repo/cloud/alchemy/fabut/
git commit -m "Deploy fabut X.Y.Z-RELEASE"
git push
cd ../fabut
```

**Both pushes must succeed. If either push fails, report the error — do not continue.**

## Documentation Checklist

These files MUST be checked on every deploy:

- [ ] `README.md` - version number, all code examples, methods reference table
- [ ] `CLAUDE.md` - Generated Builder API, Methods Per Field, Test Patterns, Anti-Patterns, Rules table
- [ ] `MIGRATION.md` - target version, all "NEW" code examples, regex patterns, checklist

## Important

- NEVER deploy with stale documentation
- NEVER use `--no-verify` on any git command
- ALWAYS run `mvn clean install` (not just `mvn test`) before deploy
- ALWAYS increment version before deploy
