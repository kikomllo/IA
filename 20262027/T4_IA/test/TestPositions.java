import java.util.*;

/** Builds real {@link Board} positions for the tests: lines, tactical positions, random positions. */
final class TestPositions {
    private TestPositions() {}

    static final int[][] LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},      // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},      // columns
        {0, 4, 8}, {2, 4, 6}                  // diagonals
    };

    /** A position, the single optimal cell, and a readable name for assertion messages. */
    record Position(Board board, int target, String name) {}

    /** Plays the cells in order on a new board (X first). */
    static Board play(int... cells) {
        Board b = new Board();
        for (int cell : cells) b.move(cell);
        return b;
    }

    /** True if {@code cells} contains a complete line. */
    static boolean hasLine(Collection<Integer> cells) {
        for (int[] l : LINES)
            if (cells.contains(l[0]) && cells.contains(l[1]) && cells.contains(l[2])) return true;
        return false;
    }

    /** The cells that are not in {@code line}. */
    static List<Integer> outside(int[] line) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < 9; i++)
            if (line[0] != i && line[1] != i && line[2] != i) out.add(i);
        return out;
    }

    /** All the k-element subsets of {@code pool}, in a fixed order. */
    static List<List<Integer>> combos(List<Integer> pool, int k) {
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << pool.size()); mask++) {
            if (Integer.bitCount(mask) != k) continue;
            List<Integer> c = new ArrayList<>();
            for (int i = 0; i < pool.size(); i++) if ((mask & (1 << i)) != 0) c.add(pool.get(i));
            out.add(c);
        }
        return out;
    }

    /** First k cells outside the line that do not form a line (a harmless set of filler pieces). */
    static List<Integer> fillers(int[] line, int k) {
        for (List<Integer> c : combos(outside(line), k)) if (!hasLine(c)) return c;
        throw new IllegalStateException("no fillers");
    }

    /**
     * Builds a board where X owns {@code xs} and O owns {@code os}, alternating X, O, X, O...
     * Requires {@code xs.size() == os.size()} (X to move) or {@code xs.size() == os.size() + 1} (O to move).
     */
    static Board build(List<Integer> xs, List<Integer> os) {
        Board b = new Board();
        int i = 0, j = 0;
        boolean xTurn = true;
        while (i < xs.size() || j < os.size()) {
            b.move(xTurn ? xs.get(i++) : os.get(j++));
            xTurn = !xTurn;
        }
        return b;
    }

    /** True if {@code mine} has two cells of some line and the third one is free. */
    private static boolean hasImmediateWin(Collection<Integer> mine, Collection<Integer> theirs) {
        for (int[] l : LINES) {
            int m = 0, free = -1;
            for (int c : l) { if (mine.contains(c)) m++; else if (!theirs.contains(c)) free = c; }
            if (m == 2 && free >= 0) return true;
        }
        return false;
    }

    /**
     * Positions with a win in one move for the player to move, where that win is the ONLY optimal move
     * (when other moves also win, a Monte Carlo agent may legitimately pick another winning move).
     */
    static List<Position> winInOnePositions(Minimax oracle) {
        List<Position> out = new ArrayList<>();
        for (int[] line : LINES)
            for (int target : line) {
                List<Integer> two = new ArrayList<>();
                for (int cell : line) if (cell != target) two.add(cell);
                for (boolean moverIsX : new boolean[]{true, false}) {
                    int taken = 0;
                    for (List<Integer> f : combos(outside(line), moverIsX ? 2 : 3)) {
                        if (hasLine(f)) continue;
                        Board b = moverIsX ? build(two, f) : build(f, two);
                        if (!oracle.optimalMoves(b).equals(Set.of(target))) continue;
                        out.add(new Position(b, target, "win in 1 " + (moverIsX ? "X" : "O") + " line "
                                + Arrays.toString(line) + " cell " + target));
                        if (++taken == 3) break;
                    }
                }
            }
        return out;
    }

    /**
     * Positions where the opponent threatens to win and the player to move must block, and blocking is
     * the ONLY optimal move.
     */
    static List<Position> blockPositions(Minimax oracle) {
        List<Position> out = new ArrayList<>();
        for (int[] line : LINES)
            for (int target : line) {
                List<Integer> two = new ArrayList<>();
                for (int cell : line) if (cell != target) two.add(cell);
                for (boolean moverIsX : new boolean[]{true, false}) {
                    int taken = 0;
                    for (List<Integer> f : combos(outside(line), 2)) {
                        if (hasLine(f)) continue;
                        List<Integer> threat = new ArrayList<>(two);
                        if (!moverIsX) {                         // O to move: X has one more piece
                            for (int o : outside(line)) if (!f.contains(o)) { threat.add(o); break; }
                        }
                        if (hasLine(threat) || hasImmediateWin(f, threat)) continue;
                        Board b = moverIsX ? build(f, threat) : build(threat, f);
                        if (!oracle.optimalMoves(b).equals(Set.of(target))) continue;
                        out.add(new Position(b, target, "block " + (moverIsX ? "X" : "O") + " line "
                                + Arrays.toString(line) + " cell " + target));
                        if (++taken == 3) break;
                    }
                }
            }
        return out;
    }

    /** A non-terminal position reached with 1 to 5 random moves (reproducible with the seed). */
    static Board randomPosition(Random random) {
        while (true) {
            Board b = new Board();
            int plies = 1 + random.nextInt(5);
            for (int k = 0; k < plies && !b.isGameOver(); k++) {
                List<Integer> free = new ArrayList<>(b.getAvailableMoves());
                Collections.sort(free);
                b.move(free.get(random.nextInt(free.size())));
            }
            if (!b.isGameOver()) return b;
        }
    }
}
