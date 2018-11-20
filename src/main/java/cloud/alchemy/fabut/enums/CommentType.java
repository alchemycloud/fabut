package cloud.alchemy.fabut.enums;

/**
 * Comment types.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public enum CommentType {

    /**
     * Fail type.
     */
    FAIL("■"),

    /**
     * Success type.
     */
    SUCCESS("∞"),

    /**
     * List type.
     */
    COLLECTION("#");

    private final String mark;

    /**
     * Comment type constructor with mark parameter.
     * 
     * @param mark
     *            mark of comment type.
     */
    private CommentType(final String mark) {
        this.mark = mark;
    }

    /**
     * Get comment type mark.
     * 
     * @return mark of comment type.
     */
    public String getMark() {
        return mark;
    }
}
