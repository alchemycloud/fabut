package cloud.alchemy.fabut.enums;

/**
 * Context for assertion code generation.
 */
public enum AssertionContext {
    /** Asserting a new object */
    NEW_OBJECT("assertObject"),
    /** Asserting an existing entity against snapshot */
    ENTITY_WITH_SNAPSHOT("assertEntityWithSnapshot");

    private final String methodName;

    AssertionContext(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
