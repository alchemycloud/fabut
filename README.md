# Fabut

Fabut is a Java testing library that simplifies object assertion and database snapshot testing. It automatically detects changes in entities and provides detailed failure reports with code suggestions to fix failing tests.

## Features

- **Object Assertion**: Deep comparison of complex objects with property-level failure messages
- **Database Snapshot Testing**: Automatically detect created, updated, and deleted entities
- **Code Generation**: When tests fail, Fabut generates code snippets to help fix the test
- **Flexible Property Matching**: Support for null checks, value matching, and ignored properties

## Installation

Add Fabut to your Maven project:

```xml
<dependency>
    <groupId>cloud.alchemy</groupId>
    <artifactId>fabut</artifactId>
    <version>4.3.0-RELEASE</version>
    <scope>test</scope>
</dependency>
```

## Quick Start

Extend `Fabut` in your test class:

```java
import cloud.alchemy.fabut.Fabut;

public class MyTest extends Fabut {

    @Test
    public void testObjectAssertion() {
        User expected = new User("John", 25);
        User actual = userService.findById(1);

        assertObject(actual,
            value(User.NAME, "John"),
            value(User.AGE, 25)
        );
    }
}
```

## Object Assertion

Assert individual properties of an object:

```java
assertObject(user,
    value(User.NAME, "John"),
    value(User.AGE, 25),
    isNull(User.EMAIL),
    notNull(User.CREATED_AT)
);
```

## Database Snapshot Testing

Fabut can track database changes between snapshots:

```java
@Test
public void testEntityCreation() {
    takeSnapshot();

    // Create a new entity
    User user = userService.create("John", 25);

    // Assert the new entity
    assertObject(user,
        value(User.NAME, "John"),
        value(User.AGE, 25)
    );

    // Verify database state
    assertDbSnapshot();
}
```

### Handling Entity Changes

When entities are modified:

```java
@Test
public void testEntityUpdate() {
    takeSnapshot();

    User user = userService.findById(1);
    user.setName("Jane");
    userService.save(user);

    // Assert the changes
    assertEntityWithSnapshot(user,
        value(User.NAME, "Jane")
    );

    assertDbSnapshot();
}
```

When entities are deleted:

```java
@Test
public void testEntityDeletion() {
    takeSnapshot();

    User user = userService.findById(1);
    userService.delete(user);

    // Mark entity as expected to be deleted
    assertEntityAsDeleted(user);

    assertDbSnapshot();
}
```

## Failure Reports

When a test fails, Fabut provides detailed reports showing exactly what changed:

```
DELETED: User[id=1]
  -> assertEntityAsDeleted(user);
============================================================
CREATED: User[id=2]
CODE:
assertObject(object,
value(User.NAME, "John"),
value(User.AGE, 25));
============================================================
UPDATED: User[id=3]
--■>name: expected: John
--■>name: but was: Jane
CODE:
assertEntityWithSnapshot(object,
value(User.NAME, "John"),
value(User.ID, 3));
```

## Configuration

### Ignoring Types

Some types can be ignored during assertion:

```java
@Override
protected List<Class<?>> getIgnoredTypes() {
    return List.of(Timestamp.class, Date.class);
}
```

### Entity Types

Define which classes are database entities:

```java
@Override
protected List<Class<?>> getEntityTypes() {
    return List.of(User.class, Order.class);
}
```

## Requirements

- Java 25+
- JUnit 6.0+

## License

```
Copyright [2013] [www.execom.eu]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Links

- [GitHub Repository](https://github.com/alchemycloud/fabut)
