package cloud.alchemy.fabut;

import java.util.Objects;

/**
 * Types of comments used in Fabut reports to indicate different statuses.
 * <p>
 * Each comment type has a distinct visual marker that appears in the report output
 * to help differentiate between success, failure, and collection entries.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @since 1.0
 */
public enum CommentType {

    /**
     * Indicates a test failure or assertion error.
     * Represented by a solid square marker.  
     */
    FAIL("■"),

    /**
     * Indicates a successful test or assertion.
     * Represented by a greater-than symbol. 
     */
    SUCCESS(">"),

    /**
     * Indicates a collection element being processed.
     * Represented by a hash/pound symbol.
     */
    COLLECTION("#");

    /**
     * The visual marker displayed in the report output for this comment type.
     */
    private final String mark;

    /**
     * Creates a new comment type with the specified visual marker.
     *
     * @param mark The visual marker for this comment type
     * @throws NullPointerException if mark is null
     */
    CommentType(final String mark) {
        this.mark = Objects.requireNonNull(mark, "Mark cannot be null");
    }

    /**
     * Returns the visual marker for this comment type.
     *
     * @return The visual marker string
     */
    public String getMark() {
        return mark;
    }
    
    /**
     * Returns whether this comment type indicates a failure.
     *
     * @return true if this is a failure comment type, false otherwise
     */
    public boolean isFailure() {
        return this == FAIL;
    }
    
    /**
     * Returns whether this comment type indicates a success.
     *
     * @return true if this is a success comment type, false otherwise
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * Returns whether this comment type is related to a collection.
     *
     * @return true if this is a collection comment type, false otherwise
     */
    public boolean isCollection() {
        return this == COLLECTION;
    }
}
