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
| `ignoredFields` | Fields auto-skipped in assertions (audit, version, timestamps) |
| `takeSnapshot()` | Captures DB state BEFORE action |
| `assertEntityWithSnapshot()` | Asserts only changed fields against snapshot |

## Generated Builder API

```java
// Assert new/created object - must cover ALL fields
OrderAssert.created(order)
    .id_is_not_null()
    .status_is("PENDING")
    .customerId_is(customerId)
    .total_is(new BigDecimal("99.99"))
    .notes_is_empty()          // Optional.empty()
    .verify();

// Assert entity changes against snapshot - only specify CHANGED fields
OrderAssert.updated(order)
    .status_is("SHIPPED")
    .notes_is("Tracking: 123")   // Optional<String> convenience overload
    .verify();

// Assert entity was deleted
OrderAssert.deleted(order).verify();
```

## Generated Methods Per Field

| Field Type | Methods |
|------------|---------|
| `T field` | `field_is(T)`, `field_is_null()`, `field_is_not_null()`, `field_is_ignored()` |
| `Optional<T>` | Above + `field_is_empty()`, `field_is_not_empty()`, `field_is(InnerT)` |

## Test Class Setup

```java
class MyServiceTest extends Fabut {

    public MyServiceTest() {
        entityTypes.add(Order.class);      // DB entities for snapshot
        entityTypes.add(Customer.class);
        complexTypes.add(OrderDto.class);  // Non-entity complex types
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
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters required
}
```

## Rules

| Rule | Description |
|------|-------------|
| `takeSnapshot()` | Always AFTER setup, BEFORE action |
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

// CORRECT
takeSnapshot();
Order order = orderService.create(customerId);
OrderAssert.created(order).status_is("PENDING").verify();
```

## Project Structure

```
src/main/java/cloud/alchemy/fabut/
├── Fabut.java              # Base test class
├── annotation/Assertable   # @Assertable annotation
├── processor/              # Annotation processor generates XxxAssert
├── property/               # Property types (Property, NullProperty, etc.)
└── enums/                  # AssertType, AssertableType
```

## Technology

- Java 25
- JUnit 6 (Jupiter)
- Maven
- Apache Commons Lang3

## Commands

### `/migrate`

Migrate a project from Fabut 4.x to 5.0. Follow [MIGRATION.md](MIGRATION.md) instructions:

1. Update dependency version to 5.0.0-RELEASE
2. Delete `PropertyPath` imports
3. Replace `Entity.CONSTANT` → `"camelCase"` in assertions
4. Remove `this` from `assertThat(this, obj)` → `assertThat(obj)`
5. Delete PropertyPath constants from model classes
6. (Optional) Add `@Assertable` and use generated builders
7. Run tests to verify

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
