package cloud.alchemy.fabut.enums;

/**
 * Assert type.
 */
public enum AssertType {

    /**
     * Using repository functionality.
     */
    REPOSITORY_ASSERT,

    /**
     * Using regular object assert.
     */
    OBJECT_ASSERT,

    /**
     * When test does not meet necessary prerequisites.
     */
    UNSUPPORTED_ASSERT;
}
