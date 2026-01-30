# Fabut Migration Guide: 4.x → 5.0

AI-actionable instructions for migrating from Fabut 4.x to 5.0.

## Breaking Changes Summary

| 4.x | 5.0 | Action |
|-----|-----|--------|
| `PropertyPath<T>` | Removed | Use String literals |
| `value(Entity.PROPERTY, x)` | `value("property", x)` | Replace constant with string |
| `assertThat(this, obj)` | `assertThat(obj)` | Remove `this` parameter |
| String constants in models | Removed | Delete constants, use `@Assertable` |

## Step-by-Step Migration

### Step 1: Update Dependency

```xml
<!-- Old -->
<version>4.x.x</version>

<!-- New -->
<version>5.0.0-RELEASE</version>
```

### Step 2: Remove PropertyPath Imports

Search and delete:
```java
// DELETE these imports
import cloud.alchemy.fabut.property.PropertyPath;
```

### Step 3: Replace PropertyPath Constants with Strings

**Pattern:**
```java
// OLD
value(EntityClass.PROPERTY_NAME, expectedValue)
isNull(EntityClass.PROPERTY_NAME)
notNull(EntityClass.PROPERTY_NAME)
ignored(EntityClass.PROPERTY_NAME)

// NEW
value("propertyName", expectedValue)
isNull("propertyName")
notNull("propertyName")
ignored("propertyName")
```

**Conversion rule:** `UPPER_SNAKE_CASE` → `camelCase`
- `PROPERTY_NAME` → `"propertyName"`
- `CUSTOMER_ID` → `"customerId"`
- `CREATED_AT` → `"createdAt"`

### Step 4: Remove String Constants from Model Classes

Delete PropertyPath constants from entity/DTO classes:

```java
// DELETE these from model classes
public static final PropertyPath<String> PROPERTY = new PropertyPath<>("property");
public static final PropertyPath<Long> ID = new PropertyPath<>("id");

// Also delete the Javadoc comments
/** The Constant PROPERTY. */
```

### Step 5: Add @Assertable Annotation (Optional but Recommended)

For type-safe generated builders:

```java
// Add to domain classes
@Assertable(ignoredFields = {"version", "createdAt", "updatedAt"})
public class Order {
    // fields and getters...
}
```

Then use generated builder:
```java
// OLD
assertObject(order, 
    value("id", 1L),
    value("status", "PENDING"),
    ignored("createdAt"));

// NEW (with generated builder)
OrderAssert.assertThat(order)
    .idIs(1L)
    .statusIs("PENDING")
    .verify();  // createdAt auto-ignored via annotation
```

### Step 6: Simplify assertThat Calls

```java
// OLD
OrderAssert.assertThat(this, order)

// NEW
OrderAssert.assertThat(order)
```

### Step 7: Update Snapshot Assertions

```java
// OLD
OrderAssert.assertSnapshot(this, order)

// NEW
OrderAssert.assertSnapshot(order)
```

## Regex Patterns for Bulk Migration

### Replace PropertyPath value() calls
```regex
Find:    value\((\w+)\.([A-Z_]+),\s*
Replace: value("$2", 

Then manually convert UPPER_SNAKE to camelCase
```

### Remove PropertyPath imports
```regex
Find:    import cloud\.alchemy\.fabut\.property\.PropertyPath;\n
Replace: (empty)
```

### Remove this from assertThat
```regex
Find:    \.assertThat\(this,\s*
Replace: .assertThat(
```

### Remove this from assertSnapshot
```regex
Find:    \.assertSnapshot\(this,\s*
Replace: .assertSnapshot(
```

## Common Patterns

### Entity Test Migration

```java
// OLD (4.x)
@Test
void testCreateUser() {
    User user = userService.create("john@example.com");
    
    assertObject(user,
        value(User.ID, notNull()),
        value(User.EMAIL, "john@example.com"),
        value(User.STATUS, UserStatus.ACTIVE),
        ignored(User.CREATED_AT),
        ignored(User.VERSION));
}

// NEW (5.0)
@Test
void testCreateUser() {
    User user = userService.create("john@example.com");
    
    UserAssert.assertThat(user)
        .idIsNotNull()
        .emailIs("john@example.com")
        .statusIs(UserStatus.ACTIVE)
        .verify();
}
```

### Snapshot Test Migration

```java
// OLD (4.x)
@Test
void testUpdateUser() {
    User user = createTestUser();
    takeSnapshot();
    
    userService.deactivate(user.getId());
    
    assertEntityWithSnapshot(user,
        value(User.STATUS, UserStatus.INACTIVE));
}

// NEW (5.0)
@Test
void testUpdateUser() {
    User user = createTestUser();
    takeSnapshot();
    
    userService.deactivate(user.getId());
    
    UserAssert.assertSnapshot(user)
        .statusIs(UserStatus.INACTIVE)
        .verify();
}
```

## Checklist

- [ ] Update pom.xml dependency to 5.0.0-RELEASE
- [ ] Delete `PropertyPath` imports from all files
- [ ] Replace `Entity.CONSTANT` with `"camelCase"` strings in test assertions
- [ ] Remove `this` parameter from `assertThat()` and `assertSnapshot()` calls
- [ ] Delete PropertyPath constant declarations from model classes
- [ ] Add `@Assertable` annotation to domain classes (optional)
- [ ] Replace manual assertions with generated builders (optional)
- [ ] Run all tests to verify migration

## Verification

After migration, run:
```bash
mvn clean test
```

All tests should pass with no compilation errors.
