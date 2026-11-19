import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Helpers for the T3 tests. Nothing here uses the classes under test
 * ({@code BallSortLayout}, {@code BestFirst}), so it can be trusted as an oracle.
 *
 * <p>Board text format = the problem's input format without the "H T" line:
 * H rows (top to bottom) of T letters separated by spaces, each row ending in '\n'.
 */
final class TestSupport {

    private TestSupport() {}

    // ---------------------------------------------------------------
    // Reference solver: plain breadth-first search (all moves cost 1, so
    // BFS = uniform cost = optimal). Independent of the code under test.
    // A state is a list of tubes; each tube is a String written bottom -> top.
    // Tubes are sorted to build the key, so tube order never matters here.
    // ---------------------------------------------------------------

    /** Minimum number of moves to solve {@code board}, or -1 if it has no solution. */
    static int optimal(String board) {
        List<String> start = parse(board);
        int H = height(board);

        Set<String> seen = new HashSet<>();
        ArrayDeque<List<String>> level = new ArrayDeque<>();
        level.add(start);
        seen.add(key(start));

        int depth = 0;
        while (!level.isEmpty()) {
            ArrayDeque<List<String>> next = new ArrayDeque<>();
            for (List<String> s : level) {
                if (isGoal(s, H)) return depth;
                for (int i = 0; i < s.size(); i++) {
                    String from = s.get(i);
                    if (from.isEmpty()) continue;
                    char ball = from.charAt(from.length() - 1);
                    for (int j = 0; j < s.size(); j++) {
                        if (i == j) continue;
                        String to = s.get(j);
                        if (to.length() == H) continue;
                        if (!to.isEmpty() && to.charAt(to.length() - 1) != ball) continue;
                        List<String> child = new ArrayList<>(s);
                        child.set(i, from.substring(0, from.length() - 1));
                        child.set(j, to + ball);
                        if (seen.add(key(child))) next.add(child);
                    }
                }
            }
            level = next;
            depth++;
        }
        return -1;
    }

    private static boolean isGoal(List<String> s, int H) {
        for (String t : s) {
            if (t.isEmpty()) continue;
            if (t.length() != H) return false;
            for (int k = 1; k < t.length(); k++) if (t.charAt(k) != t.charAt(0)) return false;
        }
        return true;
    }

    private static String key(List<String> s) {
        List<String> sorted = new ArrayList<>(s);
        Collections.sort(sorted);
        return String.join(",", sorted);
    }

    private static int height(String board) {
        return board.split("\n").length;
    }

    /** Board text -> list of tubes, each written bottom -> top without the 'E's. */
    static List<String> parse(String board) {
        String[] rows = board.split("\n");
        int H = rows.length;
        int T = rows[0].split(" ").length;
        List<String> tubes = new ArrayList<>();
        for (int j = 0; j < T; j++) {
            StringBuilder sb = new StringBuilder();
            for (int i = H - 1; i >= 0; i--) {
                char c = rows[i].split(" ")[j].charAt(0);
                if (c != 'E') sb.append(c);
            }
            tubes.add(sb.toString());
        }
        return tubes;
    }

    /** List of tubes (bottom -> top, no 'E') -> board text with H rows. */
    static String format(List<String> tubes, int H) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < H; row++) {
            int fromBottom = H - 1 - row;
            for (int j = 0; j < tubes.size(); j++) {
                String t = tubes.get(j);
                sb.append(fromBottom < t.length() ? t.charAt(fromBottom) : 'E');
                sb.append(j == tubes.size() - 1 ? '\n' : ' ');
            }
        }
        return sb.toString();
    }

    /** Same board prefixed with the "H T" line, as Main reads it. */
    static String withHeader(String board) {
        return height(board) + " " + board.split("\n")[0].split(" ").length + "\n" + board;
    }

    // ---------------------------------------------------------------
    // Random solvable boards: start from the solved board and apply random
    // REVERSE moves. A reverse move takes the top ball x of tube j and puts it
    // on any non-full tube i, as long as the ball under x in j is also x (or x
    // is alone): that is exactly when the forward move i -> j would be legal.
    // So the result can always be solved in at most {@code steps} moves.
    // ---------------------------------------------------------------

    static String scrambled(int H, int colors, int extraTubes, int steps, Random rnd) {
        String letters = "RBPYG";
        List<StringBuilder> tubes = new ArrayList<>();
        for (int c = 0; c < colors; c++) {
            tubes.add(new StringBuilder(String.valueOf(letters.charAt(c)).repeat(H)));
        }
        for (int e = 0; e < extraTubes; e++) tubes.add(new StringBuilder());

        for (int step = 0; step < steps; step++) {
            List<int[]> options = new ArrayList<>();
            for (int j = 0; j < tubes.size(); j++) {
                StringBuilder from = tubes.get(j);
                int n = from.length();
                if (n == 0) continue;
                if (n > 1 && from.charAt(n - 2) != from.charAt(n - 1)) continue;
                for (int i = 0; i < tubes.size(); i++) {
                    if (i != j && tubes.get(i).length() < H) options.add(new int[] { j, i });
                }
            }
            if (options.isEmpty()) break;
            int[] m = options.get(rnd.nextInt(options.size()));
            StringBuilder from = tubes.get(m[0]);
            char ball = from.charAt(from.length() - 1);
            from.setLength(from.length() - 1);
            tubes.get(m[1]).append(ball);
        }

        List<String> result = new ArrayList<>();
        for (StringBuilder sb : tubes) result.add(sb.toString());
        Collections.shuffle(result, rnd); // tube order must not matter
        return format(result, H);
    }

    // ---------------------------------------------------------------
    // Fully shuffled boards: all the balls are mixed at random and dealt into
    // the full tubes, the extra tubes stay empty. Unlike scrambled(), these are
    // as hard as the real inputs, but they may have NO solution.
    // ---------------------------------------------------------------

    static String shuffled(int H, int colors, int extraTubes, Random rnd) {
        String letters = "RBPYG";
        List<Character> balls = new ArrayList<>();
        for (int c = 0; c < colors; c++) {
            for (int k = 0; k < H; k++) balls.add(letters.charAt(c));
        }
        Collections.shuffle(balls, rnd);

        List<String> tubes = new ArrayList<>();
        for (int c = 0; c < colors; c++) {
            StringBuilder tube = new StringBuilder();
            for (int k = 0; k < H; k++) tube.append(balls.get(c * H + k));
            tubes.add(tube.toString());
        }
        for (int e = 0; e < extraTubes; e++) tubes.add("");
        Collections.shuffle(tubes, rnd);
        return format(tubes, H);
    }

    // ---------------------------------------------------------------
    // Checks one step of a solution, with its own rules (not the code under test):
    // exactly one tube loses its top ball and another one gains that same ball,
    // and the destination was empty or had the same color on top and was not full.
    // ---------------------------------------------------------------

    static boolean isLegalMove(String before, String after) {
        List<String> b = parse(before);
        List<String> a = parse(after);
        int H = height(before);
        if (b.size() != a.size()) return false;

        int from = -1, to = -1;
        for (int j = 0; j < b.size(); j++) {
            if (b.get(j).equals(a.get(j))) continue;
            if (a.get(j).length() == b.get(j).length() - 1) {
                if (from != -1) return false;
                from = j;
            } else if (a.get(j).length() == b.get(j).length() + 1) {
                if (to != -1) return false;
                to = j;
            } else {
                return false;
            }
        }
        if (from == -1 || to == -1) return false;

        String source = b.get(from);
        char ball = source.charAt(source.length() - 1);
        if (!a.get(from).equals(source.substring(0, source.length() - 1))) return false;
        if (!a.get(to).equals(b.get(to) + ball)) return false;
        if (b.get(to).length() >= H) return false;
        return b.get(to).isEmpty() || b.get(to).charAt(b.get(to).length() - 1) == ball;
    }

    /** True if every non-empty tube is full and has a single color. */
    static boolean isSolved(String board) {
        int H = height(board);
        return isGoal(parse(board), H);
    }
}
