package cloud.alchemy.fabut.pair;

/**
 * Represents the pair expected/actual during snapshot assert with information if pair has been already asserted.
 * 
 * @author nolah
 * 
 */
public class SnapshotPair extends Pair {

    private boolean asserted;

    public SnapshotPair(final Object expected, final Object actual) {
        super(expected, actual);
        asserted = false;
    }

    public boolean isAsserted() {
        return asserted;
    }

    public void setAsserted(final boolean asserted) {
        this.asserted = asserted;
    }

}
