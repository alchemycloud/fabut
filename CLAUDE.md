# Fabut

A Java testing library that helps developers write better tests with fluent assertion APIs.

## Build Commands

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Build JAR (skip tests)
mvn package -DskipTests

# Full build with tests
mvn clean install

# Generate Javadoc
mvn javadoc:jar
```

## Technology Stack

- Java 25
- Maven 3.8.0+
- JUnit 6 (Jupiter)
- Apache Commons Lang3

## Project Structure

```
src/
├── main/java/cloud/alchemy/fabut/
│   ├── property/     # Property assertion types (Property, NullProperty, etc.)
│   ├── enums/        # Assertion enums (AssertType, AssertableType, etc.)
│   └── graph/        # Graph utilities for object comparison
└── test/java/cloud/alchemy/fabut/
    ├── model/        # Test model classes
    └── *Test.java    # Unit tests
```

## Development Rules

- Follow existing code style and patterns
- All new functionality must have corresponding unit tests
- Use JUnit 6 assertions and test annotations
- Package namespace: `cloud.alchemy.fabut`
