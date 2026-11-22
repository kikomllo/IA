import java.util.*;

/**
 * Full minimax over Tic-Tac-Toe, used ONLY as a test oracle (it is not part of the solution).
 * Values are from X's point of view: +1 X wins, 0 draw, -1 O wins.
 */
final class Minimax {
    private final Map<String, Integer> memo = new HashMap<>();

    /** Game-theoretic value of the layout with perfect play from both sides. */
    int value(Ilayout state) {
        if (state.isGameOver()) {
            Ilayout.ID w = state.getWinner();
            return w == Ilayout.ID.X ? 1 : w == Ilayout.ID.O ? -1 : 0;
        }
        String key = state + "|" + state.getTurn();
        Integer known = memo.get(key);
        if (known != null) return known;

        boolean xToMove = state.getTurn() == Ilayout.ID.X;
        int best = xToMove ? -2 : 2;
        for (Ilayout child : state.children()) {
            int v = value(child);
            best = xToMove ? Math.max(best, v) : Math.min(best, v);
        }
        memo.put(key, best);
        return best;
    }

    /** The cells that keep the game-theoretic value of the position (all the optimal moves). */
    Set<Integer> optimalMoves(Ilayout state) {
        int target = value(state);
        Set<Integer> optimal = new TreeSet<>();
        for (int cell : state.getAvailableMoves()) {
            Ilayout child = copyAndMove(state, cell);
            if (value(child) == target) optimal.add(cell);
        }
        return optimal;
    }

    /** One optimal move chosen at random among the optimal ones (so minimax games are not all equal). */
    int randomOptimalMove(Ilayout state, Random random) {
        List<Integer> optimal = new ArrayList<>(optimalMoves(state));
        return optimal.get(random.nextInt(optimal.size()));
    }

    /** The child reached by playing {@code cell}, found among {@code children()}. */
    private static Ilayout copyAndMove(Ilayout state, int cell) {
        for (Ilayout child : state.children()) {
            Set<Integer> played = state.getAvailableMoves();
            played.removeAll(child.getAvailableMoves());
            if (played.contains(cell)) return child;
        }
        throw new IllegalArgumentException("cell not available: " + cell);
    }
}
