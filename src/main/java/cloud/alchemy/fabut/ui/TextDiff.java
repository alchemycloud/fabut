package cloud.alchemy.fabut.ui;

import java.util.ArrayList;
import java.util.List;

/** Minimal line diff (LCS) used to show ARIA snapshot changes in failure messages. */
final class TextDiff {

    private static final int MAX_LINES = 3000;

    private TextDiff() {
    }

    /** Returns changed lines prefixed with {@code "- "} (expected only) or {@code "+ "} (actual only). */
    static List<String> diff(String expected, String actual) {
        String[] a = expected.split("\n", -1);
        String[] b = actual.split("\n", -1);
        if (a.length > MAX_LINES || b.length > MAX_LINES) {
            return List.of("(snapshot too large to diff)");
        }
        int[][] lcs = new int[a.length + 1][b.length + 1];
        for (int i = a.length - 1; i >= 0; i--) {
            for (int j = b.length - 1; j >= 0; j--) {
                lcs[i][j] = a[i].equals(b[j]) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        List<String> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.length || j < b.length) {
            if (i < a.length && j < b.length && a[i].equals(b[j])) {
                i++;
                j++;
            } else if (i < a.length && (j == b.length || lcs[i + 1][j] >= lcs[i][j + 1])) {
                out.add("- " + a[i++]);
            } else {
                out.add("+ " + b[j++]);
            }
        }
        return out;
    }
}
