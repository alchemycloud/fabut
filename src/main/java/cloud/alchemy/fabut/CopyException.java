package cloud.alchemy.fabut;

import java.util.Objects;

/**
 * Exception thrown when an object copy operation fails.
 * This is typically used when Fabut attempts to create a copy of an object
 * for snapshot comparison but encounters an error.
 *
 * @since 1.0
 */
public class CopyException extends Exception {

    private static final long serialVersionUID = -3892289189753962585L;

    /**
     * The name of the entity that failed to copy.
     */
    private final String copyFailName;

    /**
     * Constructs a new CopyException with the specified entity name.
     *
     * @param copyFailName the name of the entity that failed to copy
     * @throws NullPointerException if copyFailName is null
     */
    public CopyException(final String copyFailName) {
        super(String.format("Failed to copy entity: %s", copyFailName));
        this.copyFailName = Objects.requireNonNull(copyFailName, "Entity name cannot be null");
    }

    /**
     * Constructs a new CopyException with the specified entity name and cause.
     *
     * @param copyFailName the name of the entity that failed to copy
     * @param cause the cause of the copy failure
     * @throws NullPointerException if copyFailName is null
     */
    public CopyException(final String copyFailName, final Throwable cause) {
        super(String.format("Failed to copy entity: %s", copyFailName), cause);
        this.copyFailName = Objects.requireNonNull(copyFailName, "Entity name cannot be null");
    }

    /**
     * Returns the name of the entity that failed to copy.
     *
     * @return the name of the entity that failed to copy
     */
    public String getCopyFailName() {
        return copyFailName;
    }
}
