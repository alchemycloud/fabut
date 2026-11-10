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
- **Default assertions** - Declare field defaults with `@AssertDefault` — no need to assert them in every test
- **Auto-ignore fields** - Mark audit fields as ignored once, never think about them again
- **Snapshot testing** - Track database changes automatically
- **Usage tracking** - Detect suboptimal data fetching with automatic field-level access analysis
- **UI snapshots** - Pixel-perfect Playwright screenshot steps with ignored areas, tolerances and agent-readable diffs
- **IDE friendly** - Full autocomplete for all assertion methods

## Quick Start

### 1. Add dependency

```xml
<dependency>
    <groupId>cloud.alchemy</groupId>
    <artifactId>fabut</artifactId>
    <version>5.7.0-RELEASE</version>
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

### Default Assertions

Fields annotated with `@AssertDefault` are automatically asserted when `verify()` is called on a `created()` builder, unless you explicitly assert them:

```java
@Assertable(ignoredFields = {"version"})
public class Order {
    private Long id;
    private String status;
    @AssertDefault("false")
    private Boolean archived;
    @AssertDefault("empty")
    private Optional<String> notes;
    private Long version;
    // getters...
}

@Test
void createOrder_defaultFieldsAutoAsserted() {
    Order order = orderService.create(customerId);

    // archived=false and notes=empty are auto-asserted by @AssertDefault
    OrderAssert.created(order)
        .id_is_not_null()
        .status_is("PENDING")
        .verify();  // archived and notes defaults checked automatically
}

@Test
void createOrder_overrideDefault() {
    Order order = orderService.createArchived(customerId);

    // Explicit assertion overrides the default
    OrderAssert.created(order)
        .id_is_not_null()
        .status_is("PENDING")
        .archived_is(true)       // Override @AssertDefault("false")
        .notes_is("Auto-archived")
        .verify();
}
```

Supported `@AssertDefault` values:

| Value | Valid on | Behavior |
|-------|----------|----------|
| `"true"` / `"false"` | `Boolean`, `boolean`, `Optional<Boolean>` | Asserts boolean value |
| `"empty"` | `Optional<?>` | Asserts `Optional.empty()` |
| `"null"` | Any reference type | Asserts `null` |
| Numeric string | Numeric types, `Optional<NumericType>` | Asserts numeric value |

Invalid combinations (e.g. `@AssertDefault("false")` on a `String` field) produce compile-time errors.

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
| `XxxAssert.created(obj, f1, f2, ...)` | Shortcut: assert all mandatory (non-`Optional`) fields by position |
| `XxxAssert.updated(entity, f1, f2, ...)` | Same shortcut against the snapshot |
| `XxxAssert.created<Group>(obj, ...)` | Field group declared with `@Assertable(create = @AssertGroup(...))` |
| `XxxAssert.updated<Group>(entity, ...)` | Field group declared with `@Assertable(update = @AssertGroup(...))` |
| `XxxDiff.compare(before, after)` | Reflection-free field-by-field diff of two instances |

```java
@Assertable(
    create = @AssertGroup(name = "Key", fields = {"id", "name"}),
    update = @AssertGroup(name = "Status", fields = {"status"})
)
public class Order { ... }

OrderAssert.createdKey(order, 1L, "First order").status_is("PENDING").verify();
OrderAssert.updatedStatus(order, "SHIPPED").verify();
```

> **Note:** Each factory method also has an overload that takes `Fabut` as the first parameter (e.g., `created(fabut, obj)`). Prefer the simpler form without `Fabut` -- it resolves the instance automatically via `ThreadLocal`. Only use the explicit `Fabut` parameter when the automatic resolution is not available (e.g., assertions outside the test class).

## Configuration

```java
public class BaseTest extends Fabut {

    public BaseTest() {
        // Classes tracked for database snapshot testing + usage tracking
        entityTypes.add(Order.class);
        entityTypes.add(Customer.class);
        entityTypes.add(Product.class);

        // Complex types for deep comparison + usage tracking
        complexTypes.add(OrderDto.class);

        // Types tracked for usage analysis only (no snapshot/assertion)
        trackedTypes.add(OrderFindTuple.class);
        trackedTypes.add(CustomerSearchTuple.class);

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

## Usage Tracking

Fabut automatically tracks which fields of fetched objects are actually used during your tests. This helps detect suboptimal data fetching — for example, loading an entire DTO with 18 fields when only 2 are needed.

### How It Works

When you call `takeSnapshot()`, Fabut activates usage tracking:

1. **ByteBuddy instruments** all registered types (`entityTypes`, `complexTypes`, `trackedTypes`)
2. **Constructors** are instrumented to register new objects automatically
3. **Getters** are instrumented to record which fields are accessed
4. At test end, a **usage report** is printed to stdout

No manual registration is needed. Any object of a registered type created after `takeSnapshot()` is automatically tracked.

### Excluding Fields from Tracking

Exclude audit/framework fields that inflate the "unused" count:

```java
public class BaseTest extends Fabut {
    public BaseTest() {
        complexTypes.add(OrderDto.class);
        ignoredFields.put(OrderDto.class, List.of("version", "createdAt", "updatedAt"));
    }
}
```

Fields in `ignoredFields` are excluded from both usage percentage calculation and the report.

### Disabling Usage Tracking

Set `trackUsage = false` to disable tracking entirely while keeping snapshot assertions:

```java
public class BaseTest extends Fabut {
    public BaseTest() {
        trackUsage = false; // No instrumentation, no usage report
        entityTypes.add(Order.class);
    }
}
```

### Pausing Tracking During Assertions

Call `pauseTracking()` after your API call returns so that field access during assertions is not recorded as real usage:

```java
@Test
void fetchOrder_usesOnlyRequiredFields() {
    takeSnapshot();

    Order order = orderService.findById(orderId);
    pauseTracking(); // Access below won't count as usage

    OrderAssert.created(order)
        .id_is_not_null()
        .status_is("PENDING")
        .verify();
}
```

### Enforcing Usage Threshold

Set `usageThreshold` to make tests fail when field usage is too low:

```java
public class BaseTest extends Fabut {
    public BaseTest() {
        complexTypes.add(OrderDto.class);
        trackedTypes.add(OrderTuple.class);
        usageThreshold = 50; // Fail if any class avg usage < 50%
    }
}
```

When a violation occurs, the test fails with:
```
USAGE THRESHOLD VIOLATION: minimum 50% required
  OrderDto: 25% avg usage (12 instances) — unused: cellId, indexInRow, isEdited, ...
```

### Example Output

```
USAGE REPORT:
  OrderDto: 12 instances fetched
    Avg usage: 17%
    Commonly unused: cellId, indexInRow, isEdited, valueBoolean, valueDecimal, ...
  OrderFindTuple: 5 instances fetched
    Accessed: all fields ✓
  Order: 1 instance fetched
    Avg usage: 0%
  WARNING: 1 object fetched but never accessed:
    Order[id=42]
```

### Excluding Side-Effect Objects

When your service layer creates objects internally (e.g., DTOs built during `postSave` or `createDto`), these pollute the tracking report. Use `UsageTracker.unregisterIfActive()` in your repository to remove them immediately:

```java
public OrderDto createDto(Order order) {
    OrderDto dto = new OrderDto(order);
    UsageTracker.unregisterIfActive(dto); // Not fetched by test, exclude from tracking
    return dto;
}
```

### Filtering Tracked Objects

Override `shouldTrackObject()` to exclude certain objects from tracking (e.g., uninitialized Hibernate proxies):

```java
@Override
protected boolean shouldTrackObject(Object obj) {
    if (obj instanceof HibernateProxy) {
        return Hibernate.isInitialized(obj);
    }
    return true;
}
```

### Type Registration

| Queue | Purpose | Snapshot | Assertion | Usage Tracked |
|-------|---------|----------|-----------|---------------|
| `entityTypes` | JPA entities | Yes | Yes | Yes |
| `complexTypes` | DTOs, value objects | No | Yes | Yes |
| `trackedTypes` | Tuples, projections | No | No | Yes |

### Maven Surefire Configuration

ByteBuddy requires dynamic agent loading. Add to your `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading</argLine>
    </configuration>
</plugin>
```

## UI Snapshot Testing (Playwright)

`FabutUi` brings Fabut's snapshot approach to the browser: every step of a UI test can take a screenshot and compare it pixel by pixel with the baseline from the previous run. Dynamic parts can be ignored and each screen (or region) can have its own accepted deviation. Failures explain *what* changed — which area, which DOM element, which accessibility-tree lines — so an agent can fix the UI or the test without looking at images.

### Setup

Playwright is an optional dependency — add it to projects that write UI tests:

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.64.0</version>
    <scope>test</scope>
</dependency>
```

### Writing UI Tests

```java
class CheckoutUiTest extends FabutUi {

    public CheckoutUiTest() {
        screenSettings.ignore("[data-testid=clock]");   // ignored on every screen
    }

    @Override
    protected String baseUrl() { return "http://localhost:8080"; }

    @Test
    void checkout() {
        page.navigate("/cart");
        screen("cart").verify();                        // step 1: pixel perfect

        page.click("text=Checkout");
        screen("payment")
            .ignore(".promo-banner")                    // masked + excluded
            .tolerate(".map", 0.05)                     // up to 5% of the map may differ
            .maxDiffPixels(10)                          // rest of the screen: at most 10 px
            .verify();

        screen(page.locator(".order-summary"), "summary").verify();   // single element
    }
}
```

`FabutUi` extends `Fabut`, so the same test can also `takeSnapshot()` and assert the database.

| Method | Description |
|--------|-------------|
| `screen("name")` | Viewport screenshot step (`.fullPage()` for the whole page) |
| `screen(locator, "name")` | Screenshot of one element |
| `screen()` | Auto-numbered step (`step-01`, `step-02`, ...) |
| `.ignore(selector \| locator)` | Mask matching elements and exclude them from comparison |
| `.ignoreArea(x, y, w, h)` | Exclude a fixed area (screenshot px) |
| `.tolerate(selector, ratio)` | Allow `ratio` (0..1) of the element's pixels to differ |
| `.tolerateArea(x, y, w, h, ratio)` | Same for a fixed area |
| `.maxDiffPixels(n)` / `.maxDiffRatio(r)` | Screen-wide budget outside tolerated areas (default: 0) |
| `.colorThreshold(t)` | Per-pixel color sensitivity 0..1 (default 0.1, 0 = exact) |
| `.strictAntialiasing()` | Count anti-aliasing differences too (ignored by default) |
| `.style(css)` | CSS injected while taking the screenshot |
| `.verify()` | Capture, compare, report — forgetting it fails the test |

### Determinism

Each test gets a fresh browser context with fixed viewport (1280×720), device scale factor 1, `en-US` locale, `UTC` timezone, light color scheme and reduced motion. Screenshots are taken with CSS animations disabled and the caret hidden, and are retaken until two consecutive shots are identical (warning if the page never settles). Override `contextOptions()`, `launchOptions()` or `browserType()` to change this.

### Baselines and Update Modes

Baselines are stored per test method, with a browser/OS suffix because font rendering differs between platforms:

```
src/test/fabut-ui/CheckoutUiTest/checkout/payment-chromium-linux.png
src/test/fabut-ui/CheckoutUiTest/checkout/payment.aria.yml        # accessibility tree, used in failure reports
```

| `-Dfabut.ui.update=` | Behavior |
|----------------------|----------|
| `missing` (local default) | Record missing baselines, compare existing ones |
| `none` (default when `CI` env is set) | Missing baseline fails |
| `all` | Overwrite mismatching baselines (accept the current UI) |

Other properties: `fabut.ui.baselineDir`, `fabut.ui.outputDir`, `fabut.ui.headed=true`.

### Failure Report

All mismatches of a test are collected and reported together after the test (`failFast = true` to stop at the first):

```
SCREEN MISMATCH: CheckoutUiTest/checkout/payment
  1234 px (0.134%) differ, allowed 10 px
  changed areas (screenshot px):
    #1 x=540 y=212 w=120 h=40 (1180 px)  -> button#pay.primary "Pay now"
    #2 x=20 y=680 w=60 h=12 (54 px)  -> span.version "v2.4.1"
  expected: src/test/fabut-ui/CheckoutUiTest/checkout/payment-chromium-linux.png
  actual:   target/fabut-ui/CheckoutUiTest/checkout/payment.actual.png
  diff:     target/fabut-ui/CheckoutUiTest/checkout/payment.diff.png
  compare:  target/fabut-ui/CheckoutUiTest/checkout/payment.compare.png  (expected | actual | diff)
  accessibility tree changes:
    - - button "Pay"
    + - button "Pay now"
  if the change is intended, accept it with: -Dfabut.ui.update=all
```

A machine-readable `<name>.json` with the same data is written next to the images.

### Without FabutUi

The engine works with any Playwright `Page`, and the comparator with any two images:

```java
ScreenSnapshotter screens = new ScreenSnapshotter(ScreenSettings.defaults());
screens.screen(page, "checkout", "payment").ignore("#clock").verify();   // throws on mismatch

ImageDiff diff = ImageComparator.compare(expectedImage, actualImage, CompareOptions.pixelPerfect());
```

## Requirements

- Java 25+
- JUnit 6.0+
- ByteBuddy 1.17+ (included transitively)
- Playwright 1.64+ (optional, only for `FabutUi`)

## License

Apache License 2.0
