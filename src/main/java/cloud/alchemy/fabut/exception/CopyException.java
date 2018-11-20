package cloud.alchemy.fabut.exception;

/**
 * Exception thrown when object copy fails.
 */
public class CopyException extends Exception {

    private static final long serialVersionUID = -3892289189753962585L;

    private final String copyFailName;

    public CopyException(final String copyFailName) {
        this.copyFailName = copyFailName;
    }

    public String getCopyFailName() {
        return copyFailName;
    }
}
