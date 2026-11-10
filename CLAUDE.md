# Fabut - AI Reference

Type-safe assertion library with generated builders and database snapshot testing.

## Rules for Claude

| Rule | Description |
|------|-------------|
| **Never commit without explicit request** | Do NOT run `git commit` unless the user explicitly asks. Prepare changes, run tests, but wait for explicit "commit" instruction. |
| Never use `--no-verify` | Git commits must always run hooks |
| **Run `mvn install` before commit** | Before every commit, run `mvn install` to ensure the build passes and artifacts are installed locally |
| **Increment version before deploy** | Before running `mvn deploy`, increment the version in `pom.xml` (patch version for fixes, minor for features) |

## Build Commands

```bash
mvn compile                    # Compile
mvn test                       # Run tests
mvn clean install              # Full build
mvn package -DskipTests        # Build JAR
```

## Core Concepts

| Concept | Description |
|---------|-------------|
| `@Assertable` | Annotation on domain classes, generates `XxxAssert` builder |
| `@AssertDefault` | Field annotation — auto-asserts default value in `created().verify()` unless explicitly overridden |
| `ignoredFields` | Fields auto-skipped in assertions (audit, version, timestamps) |
| `takeSnapshot()` | Captures DB state BEFORE action + activates usage tracking |
| `assertEntityWithSnapshot()` | Asserts only changed fields against snapshot |
| `trackedTypes` | Additional types to track for usage analysis (entities + complex types are auto-tracked) |
| `pauseTracking()` | Stops recording field access after API call, before assertions |
| `trackUsage` | Set to `false` to disable usage tracking entirely (default: `true`) |
| Usage Tracking | Automatic field-level usage analysis via ByteBuddy instrumentation |
| `FabutUi` | Base class for Playwright UI tests: `screen("name")...verify()` compares screenshots with baselines |

## Generated Builder API

```java
// Assert new/created object - must cover ALL fields (unless @AssertDefault handles them)
OrderAssert.created(order)
    .id_is_not_null()
    .status_is("PENDING")
    .customerId_is(customerId)
    .total_is(new BigDecimal("99.99"))
    .notes_is_empty()          // Optional.empty()
    .verify();                 // @AssertDefault fields auto-asserted here

// Assert entity changes against snapshot - only specify CHANGED fields
OrderAssert.updated(order)
    .status_is("SHIPPED")
    .notes_is("Tracking: 123")   // Optional<String> convenience overload
    .verify();

// Assert entity was deleted
OrderAssert.deleted(order).verify();

// Shortcuts
OrderAssert.created(order, id, status, total).verify();   // all non-Optional fields, declaration order
OrderAssert.createdKey(order, 1L, "x").verify();           // @Assertable(create = @AssertGroup(name = "Key", fields = {...}))
OrderAssert.updatedStatus(order, "SHIPPED").verify();      // @Assertable(update = @AssertGroup(name = "Status", ...))
Diff<Order> diff = OrderDiff.compare(before, after);       // generated, reflection-free diff
```

## Generated Methods Per Field

| Field Type | Methods |
|------------|---------|
| `T field` | `field_is(T)`, `field_is_null()`, `field_is_not_null()`, `field_is_ignored()` |
| `Optional<T>` | Above + `field_is_empty()`, `field_is_not_empty()`, `field_is(InnerT)` |
| `@AssertDefault` field | Same as above; default auto-asserted in `verify()` unless explicitly set |

## Test Class Setup

```java
class MyServiceTest extends Fabut {

    public MyServiceTest() {
        entityTypes.add(Order.class);      // DB entities for snapshot + usage tracked
        entityTypes.add(Customer.class);
        complexTypes.add(OrderDto.class);  // Non-entity complex types + usage tracked
        trackedTypes.add(OrderTuple.class); // Usage-tracked only (no snapshot/assertion)
        ignoredTypes.add(AuditInfo.class); // Skip in deep comparison
    }

    @Override
    protected List<?> findAll(Class<?> entityClass) {
        return entityManager.createQuery("FROM " + entityClass.getSimpleName()).getResultList();
    }

    @Override
    protected Object findById(Class<?> entityClass, Object id) {
        return entityManager.find(entityClass, id);
    }
}
```

## Test Patterns

### Create Entity
```java
@Test
void createOrder_success() {
    takeSnapshot();  // BEFORE action

    Order order = orderService.create(customerId, items);

    OrderAssert.created(order)
        .id_is_not_null()
        .status_is("PENDING")
        .verify();
}
```

### Update Entity
```java
@Test
void shipOrder_updatesStatus() {
    Order order = createTestOrder();
    takeSnapshot();  // AFTER setup, BEFORE action

    orderService.ship(order.getId());

    OrderAssert.updated(order)
        .status_is("SHIPPED")  // Only changed fields
        .verify();
}
```

### Delete Entity
```java
@Test
void cancelOrder_deletesOrder() {
    Order order = createTestOrder();
    takeSnapshot();

    orderService.cancel(order.getId());

    assertEntityAsDeleted(order);
}
```

### Ignore Entity
```java
@Test
void processOrder_createsAuditLog() {
    takeSnapshot();

    orderService.process(orderId);
    AuditLog log = auditRepository.findLatest();

    ignoreEntity(log);  // Exclude from snapshot verification
}
```

## Manual Assertion Methods

```java
// Exact value
assertObject(order, value("status", "PENDING"), value("total", new BigDecimal("99.99")));

// Null checks
assertObject(order, isNull("cancelledAt"), notNull("id"));

// Ignore fields
assertObject(order, ignored("createdAt"), ignored("updatedAt"));

// Nested properties
assertObject(order, value("customer.name", "John"));

// Varargs
assertObject(order, ignored("createdAt", "updatedAt", "version"));
```

## Domain Class Setup

```java
@Assertable(ignoredFields = {"version", "createdAt", "updatedAt"})
public class Order {
    private Long id;
    private String status;
    private BigDecimal total;
    private Optional<String> notes;
    @AssertDefault("false")
    private Boolean archived;      // Auto-asserted as false in created().verify()
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters required
}
```

## Rules

| Rule | Description |
|------|-------------|
| `takeSnapshot()` | Always AFTER setup, BEFORE action; activates usage tracking |
| `usageThreshold` | Set to 0-100 to fail tests if avg field usage drops below threshold (default: -1, disabled) |
| `assertEntityWithSnapshot()` | Must have ≥1 changed field |
| `created().verify()` | Must cover ALL non-ignored fields; forgotten verify() caught automatically |
| Extends `Fabut` | Test class must extend Fabut |
| Getters required | All fields need getter methods |

## Anti-Patterns

```java
// WRONG - snapshot after action
orderService.create(customerId);
takeSnapshot();  // Too late!

// WRONG - empty snapshot assertion
assertEntityWithSnapshot(order);  // No changes specified

// WRONG - missing verify() (now caught automatically in @AfterEach)
OrderAssert.created(order).status_is("PENDING");  // Fails with UNVERIFIED BUILDER error!

// AVOID - passing Fabut explicitly when not needed
OrderAssert.created(this, order).status_is("PENDING").verify();  // Works but verbose

// CORRECT - Fabut is resolved automatically via ThreadLocal
takeSnapshot();
Order order = orderService.create(customerId);
OrderAssert.created(order).status_is("PENDING").verify();
```

> **Avoid explicit `Fabut` parameter:** Factory methods like `created(fabut, obj)` exist for edge cases where automatic `ThreadLocal` resolution is unavailable (e.g., assertions outside the test class). In normal tests, always use the simpler `created(obj)` form.

## Usage Tracking

Fabut automatically tracks which fields of fetched entities, DTOs, and tuples are actually accessed during a test. This helps identify suboptimal data fetching (e.g., loading 18 fields when only 2 are used).

### How It Works

1. `takeSnapshot()` activates tracking and instruments all registered types via ByteBuddy
2. ByteBuddy adds advice to **constructors** (registers objects) and **getters** (records field access)
3. Objects created after `takeSnapshot()` are automatically tracked — no manual registration
4. In `@AfterEach`, a usage report is printed to stdout showing which fields were accessed

### Configuration

Types registered in `entityTypes`, `complexTypes`, and `trackedTypes` are all instrumented:

```java
entityTypes.add(Order.class);           // Snapshot + usage tracked
complexTypes.add(OrderDto.class);       // Assertion + usage tracked
trackedTypes.add(OrderFindTuple.class); // Usage tracked ONLY

// Optional: exclude fields from usage tracking (e.g., audit fields)
ignoredFields.put(Order.class, List.of("version", "createdAt", "updatedAt"));

// Optional: fail tests if usage drops below threshold
usageThreshold = 50; // Fail if any class avg usage < 50%

// Optional: disable usage tracking entirely
trackUsage = false; // No instrumentation, no report
```

### Pausing Tracking

Call `pauseTracking()` after the API under test returns, so field access during assertions is not recorded:

```java
@Test
void fetchOrder_usesOnlyRequiredFields() {
    takeSnapshot();
    Order order = orderService.findById(orderId);
    pauseTracking();
    OrderAssert.created(order).id_is_not_null().status_is("PENDING").verify();
}
```

### Report Output

```
USAGE REPORT:
  OrderDto: 12 instances fetched
    Avg usage: 17%
    Commonly unused: cellId, indexInRow, isEdited, valueBoolean, ...
  OrderFindTuple: 5 instances fetched
    Accessed: all fields ✓
  Order: 1 instance fetched
    Avg usage: 0%
  WARNING: 1 object fetched but never accessed:
    Order[id=42]
```

### Excluding Side-Effect Objects

Use `UsageTracker.unregisterIfActive()` in repositories to remove objects created as side effects:

```java
public OrderDto createDto(Order order) {
    OrderDto dto = new OrderDto(order);
    UsageTracker.unregisterIfActive(dto);
    return dto;
}
```

### Filtering Tracked Objects

Override `shouldTrackObject()` to exclude objects (e.g., uninitialized Hibernate proxies):

```java
@Override
protected boolean shouldTrackObject(Object obj) {
    if (obj instanceof HibernateProxy) {
        return Hibernate.isInitialized(obj);
    }
    return true;
}
```

### JVM Configuration

Add to Maven Surefire plugin for ByteBuddy self-attach:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading</argLine>
    </configuration>
</plugin>
```

### Tracking Package Structure

```
src/main/java/cloud/alchemy/fabut/tracking/
├── UsageTracker.java           # ThreadLocal registry, activate/deactivate
├── TrackedObject.java          # Per-object: class, all fields, accessed fields
├── UsageReport.java            # Generates formatted report with class summaries
└── UsageInstrumentation.java   # ByteBuddy agent installation + class instrumentation
```

## UI Snapshot Testing (`cloud.alchemy.fabut.ui`)

Playwright-based pixel-perfect screen assertions. Playwright is an **optional** dependency of fabut — consumers add `com.microsoft.playwright:playwright` themselves.

```java
class CheckoutUiTest extends FabutUi {          // extends Fabut: DB snapshot + UI in one test
    public CheckoutUiTest() {
        screenSettings.ignore("[data-testid=clock]");   // ignored on every screen
    }
    @Override protected String baseUrl() { return "http://localhost:8080"; }

    @Test
    void checkout() {
        page.navigate("/cart");
        screen("cart").verify();                         // pixel perfect step
        page.click("text=Checkout");
        screen("payment")
            .ignore(".promo-banner")                     // mask + exclude
            .tolerate(".map", 0.05)                      // region budget: 5% of its pixels
            .maxDiffPixels(10)                           // global budget outside tolerated regions
            .verify();
        screen(page.locator(".summary"), "summary").verify();   // element screenshot
    }
}
```

| API | Description |
|-----|-------------|
| `screen(name)` / `screen(locator, name)` / `screen()` | Start screen assertion (viewport / element / auto `step-NN`) |
| `.ignore(selector\|locator)`, `.ignoreArea(x,y,w,h)` | Excluded from comparison (selectors are also masked) |
| `.tolerate(selector, ratio)`, `.tolerateArea(x,y,w,h,ratio)` | Own deviation budget (0..1) |
| `.maxDiffPixels(n)`, `.maxDiffRatio(r)` | Global budget; default 0 (pixel perfect) |
| `.colorThreshold(t)` | Per-pixel YIQ sensitivity 0..1 (default 0.1) |
| `.fullPage()`, `.strictAntialiasing()`, `.style(css)` | Capture options |
| `.verify()` | Required; forgotten verify() → UNVERIFIED BUILDER |
| `screenSettings` | Class-wide defaults (dirs, budgets, global ignores, platform suffix) |
| `failFast` | `false` (default) = collect mismatches, fail in `@AfterEach` |
| `contextOptions()` / `launchOptions()` / `browserType()` / `baseUrl()` | Overridable browser setup |

| Path / Property | Meaning |
|-----------------|---------|
| `src/test/fabut-ui/<Class>/<method>/<name>-<browser>-<os>.png` | Baseline (commit it) |
| `src/test/fabut-ui/<Class>/<method>/<name>.aria.yml` | ARIA snapshot, used for diff in failure messages |
| `target/fabut-ui/<Class>/<method>/<name>.{actual,diff,compare}.png`, `.json` | Failure artifacts |
| `-Dfabut.ui.update=missing` | Default locally: record missing baselines |
| `-Dfabut.ui.update=none` | Default if `CI` env set: missing baseline fails |
| `-Dfabut.ui.update=all` | Accept current UI: overwrite mismatching baselines |
| `-Dfabut.ui.headed=true` | Show the browser |

**Agent workflow on `SCREEN MISMATCH`:** read the message — it lists changed areas with the DOM element at each (`-> button#pay "Pay now"`) and accessibility-tree line changes. If the change is a bug, fix the UI. If intended, rerun with `-Dfabut.ui.update=all`. If the area is dynamic (time, random ids), add `.ignore(selector)`. View `*.compare.png` (expected | actual | diff) when visual inspection is needed.

```
src/main/java/cloud/alchemy/fabut/ui/
├── FabutUi.java            # Base test class: browser lifecycle, screen(), soft failures
├── ScreenAssert.java       # Fluent builder (ignore/tolerate/budgets/verify)
├── ScreenSnapshotter.java  # Stable capture, region mapping, baseline IO, diagnostics
├── ScreenSettings.java     # Defaults + system properties
├── ScreenResult.java       # Status, agent-readable message(), toJson()
├── ImageComparator.java    # Pure-Java YIQ diff, anti-aliasing detection, clustering
├── CompareOptions.java / Region.java / ImageDiff.java / DiffCluster.java
├── UpdateMode.java         # none | missing | all
└── TextDiff.java           # Line diff for ARIA snapshots
```

## Project Structure

```
src/main/java/cloud/alchemy/fabut/
├── Fabut.java              # Base test class
├── annotation/Assertable   # @Assertable annotation
├── processor/              # Annotation processor generates XxxAssert
├── property/               # Property types (Property, NullProperty, etc.)
├── tracking/               # Usage tracking (ByteBuddy instrumentation)
├── ui/                     # Playwright UI snapshot testing (FabutUi)
├── diff/                   # Compile-time diff reporting
├── graph/                  # Cycle detection in object graphs
└── enums/                  # AssertType, AssertableType
```

## Technology

- Java 25
- JUnit 6 (Jupiter)
- Maven
- Apache Commons Lang3
- ByteBuddy (usage tracking instrumentation)
- Playwright (optional, UI snapshot testing)

## Commands

### `/migrate`

Migrate a project from Fabut 4.x to 5.x. Follow [MIGRATION.md](MIGRATION.md) instructions:

1. Update dependency version to latest RELEASE
2. Delete `PropertyPath` imports
3. Replace `Entity.CONSTANT` → `"camelCase"` strings in assertions
4. Replace `assertThat()` with `created()`, `assertSnapshot()` with `updated()`
5. Convert camelCase field methods to snake_case (`statusIs` → `status_is`)
6. Delete PropertyPath constants from model classes
7. (Optional) Add `@Assertable` and use generated builders
8. Run tests to verify

### `/deploy`

Deploy a new version to Maven Central. Enforces documentation audit before deploy:

1. Verify ALL documentation (README.md, CLAUDE.md, MIGRATION.md) matches current code
2. Increment version in `pom.xml`
3. Run `mvn clean install`
4. Run `mvn deploy`
5. Commit and push fabut project
6. Commit and push `../repo` project (gh-pages branch with Maven artifacts)

### `/assess`

Pre-commit assessment with JaCoCo coverage analysis. Run before committing to check:

1. **Test coverage**: Run `mvn test` with JaCoCo, parse `target/site/jacoco/jacoco.csv`
2. **Documentation freshness**: If core files changed (Fabut.java, AssertableProcessor.java, Assertable.java), verify README.md and CLAUDE.md are updated
3. **Test file mapping**: Verify test files exist for changed source files
4. **Public API check**: Identify new public methods that may need documentation

Output format:
```
📚 Documentation: ✓ or ⚠️ (list files needing update)
🧪 Test Coverage:
   Lines: XX.X% (covered/total)
   Branches: XX.X% (covered/total)
   ⚠️ Low coverage: ClassName (XX%) - if any class < 70%
🏃 Tests: ✓ PASSED (206 tests) or ✗ FAILED
📋 New Public APIs: ✓ or ℹ️ (list new methods)
```

### `/commit`

Run `/assess` first, then if passed:
1. Show `git status` and `git diff --stat`
2. Generate commit message based on changes
3. Stage and commit with Co-Authored-By

### `/assess` Checklist

| Check | Core Files | Action |
|-------|-----------|--------|
| Docs | `Fabut.java`, `AssertableProcessor.java`, `Assertable.java` | Update README.md, CLAUDE.md |
| Tests | Any `src/main/java/**/*.java` | Ensure `src/test/java/**/*Test.java` exists |
| Build | Any `.java` | Run `mvn test` |
