# Fabut - AI Reference

Type-safe assertion library with generated builders and database snapshot testing.

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
OrderAssert.assertThat(order)
    .idIsNotNull()
    .statusIs("PENDING")
    .customerIdIs(customerId)
    .totalIs(new BigDecimal("99.99"))
    .notesIsEmpty()          // Optional.empty()
    .verify();

// Assert entity changes against snapshot - only specify CHANGED fields
OrderAssert.assertSnapshot(order)
    .statusIs("SHIPPED")
    .notesHasValue("Tracking: 123")
    .verify();
```

## Generated Methods Per Field

| Field Type | Methods |
|------------|---------|
| `T field` | `fieldIs(T)`, `fieldIsNull()`, `fieldIsNotNull()`, `fieldIgnored()` |
| `Optional<T>` | Above + `fieldIsEmpty()`, `fieldIsNotEmpty()`, `fieldHasValue(T)` |

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

    OrderAssert.assertThat(order)
        .idIsNotNull()
        .statusIs("PENDING")
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

    OrderAssert.assertSnapshot(order)
        .statusIs("SHIPPED")  // Only changed fields
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
| `assertThat().verify()` | Must cover ALL non-ignored fields |
| Extends `Fabut` | Test class must extend Fabut |
| Getters required | All fields need getter methods |

## Anti-Patterns

```java
// WRONG - snapshot after action
orderService.create(customerId);
takeSnapshot();  // Too late!

// WRONG - empty snapshot assertion
assertEntityWithSnapshot(order);  // No changes specified

// WRONG - missing verify()
OrderAssert.assertThat(order).statusIs("PENDING");  // Never executed!

// CORRECT
takeSnapshot();
Order order = orderService.create(customerId);
OrderAssert.assertThat(order).statusIs("PENDING").verify();
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
