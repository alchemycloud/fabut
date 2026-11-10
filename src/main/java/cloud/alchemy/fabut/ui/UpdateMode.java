package cloud.alchemy.fabut.ui;

import java.util.Locale;

/**
 * What to do with baselines. Controlled with {@code -Dfabut.ui.update=none|missing|all}.
 */
public enum UpdateMode {
    /** Never write baselines; a missing baseline fails the test. Default when the {@code CI} env variable is set. */
    NONE,
    /** Record baselines that don't exist yet, compare the rest. Default for local runs. */
    MISSING,
    /** Record missing baselines and overwrite mismatching ones (accept the current UI). */
    ALL;

    public static final String PROPERTY = "fabut.ui.update";

    public static UpdateMode fromEnvironment() {
        String value = System.getProperty(PROPERTY);
        if (value != null && !value.isBlank()) {
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "none", "false" -> NONE;
                case "missing" -> MISSING;
                case "all", "true" -> ALL;
                default -> throw new IllegalArgumentException(
                        "Invalid -D" + PROPERTY + "=" + value + " (expected none, missing or all)");
            };
        }
        String ci = System.getenv("CI");
        return ci != null && !ci.isBlank() && !"false".equalsIgnoreCase(ci) ? NONE : MISSING;
    }
}
