# Fabut

> Type-safe, fluent assertion library for Java that makes testing a breeze.

Fabut eliminates boilerplate in your tests by generating type-safe assertion builders from your domain classes. No more string-based property names, no more forgetting to assert fields.

## Why Fabut?

**Before Fabut:**
```java
@Test
void testCreateOrder() {
    Order order = orderService.create(customer, items);

    assertNotNull(order.getId());
    assertEquals("PENDING", order.getStatus());
    assertEquals(customer.getId(), order.getCustomerId());
    assertEquals(new BigDecimal("99.99"), order.getTotal());
    assertNotNull(order.getCreatedAt());
    // Did I forget any fields? 🤔
}
```

**With Fabut:**
```java
@Test
void testCreateOrder() {
    Order order = orderService.create(customer, items);

    OrderAssert.created(order)
        .id_is_not_null()
        .status_is("PENDING")
        .customerId_is(customer.getId())
        .total_is(new BigDecimal("99.99"))
        .createdAt_is_not_null()
        .verify();  // Fails if any field is not covered ✓
}
```

## Features

- **Compile-time safety** - Typos in field names? Impossible.
- **Complete coverage** - Fabut fails if you forget to assert a field
- **Minimal boilerplate** - Just `created(object)` - no need to pass `this`
- **Optional support** - First-class support for `Optional<T>` fields
- **Auto-ignore fields** - Mark audit fields as ignored once, never think about them again
- **Snapshot testing** - Track database changes automatically
- **IDE friendly** - Full autocomplete for all assertion methods

## Quick Start

### 1. Add dependency

```xml
<dependency>
    <groupId>cloud.alchemy</groupId>
    <artifactId>fabut</artifactId>
    <version>5.2.2-RELEASE</version>
    <scope>test</scope>
</dependency>
```

### 2. Annotate your class

```java
@Assertable(ignoredFields = {"version", "createdAt", "updatedAt"})
public class Order {
    private Long id;
    private String status;
    private Long customerId;
    private BigDecimal total;
    private Optional<String> notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    // getters...
}
```

### 3. Configure annotation processor

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.14.0</version>
    <configuration>
        <annotationProcessors>
            <annotationProcessor>cloud.alchemy.fabut.processor.AssertableProcessor</annotationProcessor>
        </annotationProcessors>
    </configuration>
</plugin>
```

### 4. Write clean tests

```java
class OrderServiceTest extends Fabut {

    @Test
    void createOrder_withValidData_createsOrder() {
        Order order = orderService.create(customerId, items);

        OrderAssert.created(order)
            .id_is_not_null()
            .status_is("PENDING")
            .customerId_is(customerId)
            .total_is(new BigDecimal("149.99"))
            .notes_is_empty()
            .verify();
    }
}
```

## Real-World Examples

### Testing CRUD Operations

```java
@Test
void updateOrder_changesStatusAndAddsNote() {
    // Arrange
    Order order = createTestOrder();
    takeSnapshot();

    // Act
    orderService.ship(order.getId(), "Shipped via FedEx");

    // Assert - only specify what changed
    OrderAssert.updated(order)
        .status_is("SHIPPED")
        .notes_is("Shipped via FedEx")
        .verify();
}

@Test
void cancelOrder_deletesOrder() {
    Order order = createTestOrder();
    takeSnapshot();

    orderService.cancel(order.getId());

    OrderAssert.deleted(order).verify();
}
```

### Testing Optional Fields

```java
@Assertable
public class UserProfile {
    private Long id;
    private String username;
    private Optional<String> bio;
    private Optional<String> avatarUrl;
    // getters...
}

@Test
void createProfile_withoutOptionalFields() {
    UserProfile profile = profileService.create("john_doe");

    UserProfileAssert.created(profile)
        .id_is_not_null()
        .username_is("john_doe")
        .bio_is_empty()           // Optional.empty()
        .avatarUrl_is_empty()
        .verify();
}

@Test
void updateProfile_addsBio() {
    UserProfile profile = createTestProfile();
    takeSnapshot();

    profileService.updateBio(profile.getId(), "Hello, world!");

    UserProfileAssert.updated(profile)
        .bio_is("Hello, world!")  // Unwraps Optional for you
        .verify();
}
```

### Testing Complex Objects

```java
@Assertable(ignoredFields = {"audit"})
public class Invoice {
    private Long id;
    private String invoiceNumber;
    private InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Optional<LocalDate> dueDate;
    private AuditInfo audit;
    // getters...
}

@Test
void generateInvoice_calculatesCorrectTotals() {
    Invoice invoice = invoiceService.generate(orderId);

    InvoiceAssert.created(invoice)
        .id_is_not_null()
        .invoiceNumber_is_not_null()
        .status_is(InvoiceStatus.DRAFT)
        .subtotal_is(new BigDecimal("100.00"))
        .tax_is(new BigDecimal("10.00"))
        .total_is(new BigDecimal("110.00"))
        .dueDate_is_empty()
        .verify();
}
```

### Testing with Ignored Fields

Fields in `ignoredFields` are automatically skipped - perfect for audit columns, versions, and timestamps:

```java
@Assertable(ignoredFields = {"id", "version", "createdAt", "createdBy", "updatedAt", "updatedBy"})
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long version;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

@Test
void createProduct_setsNameAndPrice() {
    Product product = productService.create("Widget", new BigDecimal("29.99"));

    // No need to handle id, version, or audit fields!
    ProductAssert.created(product)
        .name_is("Widget")
        .price_is(new BigDecimal("29.99"))
        .verify();
}
```

## Generated Methods Reference

For each field, Fabut generates intuitive assertion methods:

| Field Type | Methods |
|------------|---------|
| `T field` | `field_is(T)`, `field_is_null()`, `field_is_not_null()`, `field_is_ignored()` |
| `Optional<T> field` | All above + `field_is_empty()`, `field_is_not_empty()`, `field_is(InnerT)` |

### Static Factory Methods

| Method | Purpose |
|--------|---------|
| `XxxAssert.created(obj)` | Assert a newly created object (all fields must be covered) |
| `XxxAssert.updated(entity)` | Assert changed fields against snapshot |
| `XxxAssert.deleted(entity)` | Assert entity was deleted |

> **Note:** Each factory method also has an overload that takes `Fabut` as the first parameter (e.g., `created(fabut, obj)`). Prefer the simpler form without `Fabut` -- it resolves the instance automatically via `ThreadLocal`. Only use the explicit `Fabut` parameter when the automatic resolution is not available (e.g., assertions outside the test class).

## Configuration

```java
public class BaseTest extends Fabut {

    public BaseTest() {
        // Classes tracked for database snapshot testing
        entityTypes.add(Order.class);
        entityTypes.add(Customer.class);
        entityTypes.add(Product.class);

        // Types to skip during deep comparison
        ignoredTypes.add(Timestamp.class);
        ignoredTypes.add(Instant.class);
    }

    @Override
    protected List<?> findAll(Class<?> entityClass) {
        // Hook into your persistence layer
        return entityManager.createQuery("FROM " + entityClass.getSimpleName()).getResultList();
    }

    @Override
    protected Object findById(Class<?> entityClass, Object id) {
        return entityManager.find(entityClass, id);
    }
}
```

## Requirements

- Java 25+
- JUnit 6.0+

## License

Apache License 2.0
