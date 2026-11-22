import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MCTS}.
 *
 * <p>The tests do NOT use the project's {@code Board}: they run on {@link RefBoard}, a tiny,
 * independent Tic-Tac-Toe that implements {@code Ilayout}. This isolates the algorithm from
 * any bug (or unfinished code) in {@code Board}, and also gives an independent oracle
 * (full minimax) to judge the quality of the moves.
 *
 * <p>Sections:
 * <ol>
 *   <li>Contract: construction, legality, no mutation of the argument, determinism.</li>
 *   <li>Tactics: win in one, block in one, X/O symmetry.</li>
 *   <li>Tree policy: Fig. 2 of the assignment, unvisited first, ties, exploration.</li>
 *   <li>Phases in isolation (private methods, through reflection).</li>
 *   <li>Whole games: random opponent, perfect minimax, self-play, move quality.</li>
 * </ol>
 *
 * <p>Tests tagged {@code slow} play many games. The phase tests use reflection on the private
 * members of {@code MCTS}, so they must be updated if those names are renamed.
 */
class MCTSTests {

    private static final Ilayout.ID X = Ilayout.ID.X;
    private static final Ilayout.ID O = Ilayout.ID.O;
    private static final Ilayout.ID BLANK = Ilayout.ID.Blank;

    private static final int[][] LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
        {0, 4, 8}, {2, 4, 6}
    };

    // =====================================================================
    // Reference game, independent from Board
    // =====================================================================

    /** Minimal, independent Tic-Tac-Toe implementing the {@code Ilayout} contract. */
    static final class RefBoard implements Ilayout {
        private final Ilayout.ID[] cells = new Ilayout.ID[N * M];
        private Ilayout.ID turn;
        private Ilayout.ID winner = Ilayout.ID.Blank;
        private boolean over;

        /** @param s nine chars (X, O or any other = blank), row by row; @param turn who plays next */
        RefBoard(String s, Ilayout.ID turn) {
            for (int i = 0; i < cells.length; i++) {
                char ch = s.charAt(i);
                cells[i] = ch == 'X' ? Ilayout.ID.X : ch == 'O' ? Ilayout.ID.O : Ilayout.ID.Blank;
            }
            this.turn = turn;
            refresh();
        }

        private RefBoard(RefBoard o) {
            System.arraycopy(o.cells, 0, cells, 0, cells.length);
            turn = o.turn;
            winner = o.winner;
            over = o.over;
        }

        static RefBoard empty() {
            return new RefBoard(".........", Ilayout.ID.X);
        }

        private void refresh() {
            winner = Ilayout.ID.Blank;
            over = false;
            for (int[] l : LINES) {
                Ilayout.ID a = cells[l[0]];
                if (a != Ilayout.ID.Blank && a == cells[l[1]] && a == cells[l[2]]) {
                    winner = a;
                    over = true;
                }
            }
            if (!over) {
                over = true;
                for (Ilayout.ID c : cells) if (c == Ilayout.ID.Blank) over = false;
            }
        }

        RefBoard copy() {
            return new RefBoard(this);
        }

        Ilayout.ID cell(int i) {
            return cells[i];
        }

        /** Same position with X and O swapped (and the turn swapped). */
        RefBoard swapped() {
            StringBuilder sb = new StringBuilder();
            for (Ilayout.ID c : cells) sb.append(c == Ilayout.ID.X ? 'O' : c == Ilayout.ID.O ? 'X' : '.');
            return new RefBoard(sb.toString(), turn == Ilayout.ID.X ? Ilayout.ID.O : Ilayout.ID.X);
        }

        String key() {
            StringBuilder sb = new StringBuilder();
            for (Ilayout.ID c : cells) sb.append(c == Ilayout.ID.X ? 'X' : c == Ilayout.ID.O ? 'O' : '.');
            return sb.append('|').append(turn.name()).toString();
        }

        @Override
        public boolean move(int index) {
            if (over) throw new IllegalStateException("game over");
            if (index < 0 || index >= cells.length) throw new IllegalArgumentException("bad index " + index);
            if (cells[index] != Ilayout.ID.Blank) return false;
            cells[index] = turn;
            refresh();
            if (!over) turn = (turn == Ilayout.ID.X) ? Ilayout.ID.O : Ilayout.ID.X;
            return true;
        }

        @Override
        public boolean isGameOver() {
            return over;
        }

        @Override
        public Ilayout.ID getTurn() {
            return turn;
        }

        @Override
        public Ilayout.ID getWinner() {
            if (!over) throw new IllegalStateException("not over yet");
            return winner;
        }

        @Override
        public HashSet<Integer> getAvailableMoves() {
            HashSet<Integer> s = new HashSet<>();
            for (int i = 0; i < cells.length; i++) if (cells[i] == Ilayout.ID.Blank) s.add(i);
            return s;
        }

        @Override
        public List<Ilayout> children() {
            List<Ilayout> r = new ArrayList<>();
            if (over) return r;
            for (int m : sorted(getAvailableMoves())) {
                RefBoard c = copy();
                c.move(m);
                r.add(c);
            }
            return r;
        }

        @Override
        public String toString() {
            return key();
        }
    }

    // =====================================================================
    // Helpers: positions, oracle, agents, games
    // =====================================================================

    private static List<Integer> sorted(Set<Integer> s) {
        List<Integer> l = new ArrayList<>(s);
        Collections.sort(l);
        return l;
    }

    private static Ilayout.ID opponent(Ilayout.ID p) {
        return p == X ? O : X;
    }

    private static char ch(Ilayout.ID p) {
        return p == X ? 'X' : 'O';
    }

    /** Cells where {@code p}, moving now, wins immediately. */
    private static List<Integer> winningMoves(RefBoard b, Ilayout.ID p) {
        List<Integer> r = new ArrayList<>();
        for (int m : sorted(b.getAvailableMoves())) {
            RefBoard c = new RefBoard(b.key().substring(0, 9), p);
            c.move(m);
            if (c.isGameOver() && c.getWinner() == p) r.add(m);
        }
        return r;
    }

    /** A random legal, non-terminal position (0 to 7 plies from the empty board). */
    private static RefBoard randomPosition(Random r) {
        while (true) {
            RefBoard b = RefBoard.empty();
            int plies = r.nextInt(8);
            for (int k = 0; k < plies && !b.isGameOver(); k++) {
                List<Integer> free = sorted(b.getAvailableMoves());
                b.move(free.get(r.nextInt(free.size())));
            }
            if (!b.isGameOver()) return b;
        }
    }

    private static boolean inLine(int[] line, int cell) {
        return line[0] == cell || line[1] == cell || line[2] == cell;
    }

    /** {@code p} owns line[0] and line[1]; the winning cell is line[2]. The opponent has two other marks. */
    private static RefBoard winInOne(int[] line, Ilayout.ID p) {
        char[] c = ".........".toCharArray();
        c[line[0]] = ch(p);
        c[line[1]] = ch(p);
        int placed = 0;
        for (int i = 0; i < 9 && placed < 2; i++) {
            if (!inLine(line, i)) {
                c[i] = ch(opponent(p));
                placed++;
            }
        }
        return new RefBoard(new String(c), p);
    }

    /**
     * The opponent owns line[0] and line[1] and threatens line[2]. The player to move
     * ({@code p}) has two marks elsewhere and no immediate win, so the only move is to block.
     */
    private static RefBoard blockInOne(int[] line, Ilayout.ID p) {
        List<Integer> outside = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (!inLine(line, i)) outside.add(i);
        for (int a = 0; a < outside.size(); a++) {
            for (int b = a + 1; b < outside.size(); b++) {
                char[] c = ".........".toCharArray();
                c[line[0]] = ch(opponent(p));
                c[line[1]] = ch(opponent(p));
                c[outside.get(a)] = ch(p);
                c[outside.get(b)] = ch(p);
                RefBoard board = new RefBoard(new String(c), p);
                if (winningMoves(board, p).isEmpty()) return board;
            }
        }
        throw new AssertionError("no suitable position for line " + line[0] + line[1] + line[2]);
    }

    // ---- full minimax (oracle), value for the player to move: +1 win, 0 draw, -1 loss ----

    private static final Map<String, Integer> MEMO = new HashMap<>();

    private static int value(RefBoard b) {
        String k = b.key();
        Integer cached = MEMO.get(k);
        if (cached != null) return cached;
        int best = -2;
        for (int m : sorted(b.getAvailableMoves())) {
            RefBoard c = b.copy();
            c.move(m);
            int v = c.isGameOver() ? (c.getWinner() == b.getTurn() ? 1 : 0) : -value(c);
            best = Math.max(best, v);
        }
        MEMO.put(k, best);
        return best;
    }

    private static List<Integer> optimalMoves(RefBoard b) {
        int best = value(b);
        List<Integer> r = new ArrayList<>();
        for (int m : sorted(b.getAvailableMoves())) {
            RefBoard c = b.copy();
            c.move(m);
            int v = c.isGameOver() ? (c.getWinner() == b.getTurn() ? 1 : 0) : -value(c);
            if (v == best) r.add(m);
        }
        return r;
    }

    // ---- agents and games ----

    private static Function<RefBoard, Integer> randomAgent(Random r) {
        return b -> {
            List<Integer> free = sorted(b.getAvailableMoves());
            return free.get(r.nextInt(free.size()));
        };
    }

    private static Function<RefBoard, Integer> minimaxAgent(Random r) {
        return b -> {
            List<Integer> best = optimalMoves(b);
            return best.get(r.nextInt(best.size()));
        };
    }

    private static Function<RefBoard, Integer> mctsAgent(int iterations, double c, long seed) {
        return b -> new MCTS(iterations, c, seed + b.getAvailableMoves().size()).chooseMove(b);
    }

    private static Ilayout.ID playGame(Function<RefBoard, Integer> xAgent, Function<RefBoard, Integer> oAgent) {
        RefBoard b = RefBoard.empty();
        while (!b.isGameOver()) {
            Function<RefBoard, Integer> agent = (b.getTurn() == X) ? xAgent : oAgent;
            int m = agent.apply(b.copy());
            assertTrue(b.getAvailableMoves().contains(m), "illegal move " + m + " in " + b.key());
            b.move(m);
        }
        return b.getWinner();
    }

    // ---- reflection helpers for the private Node class and private methods ----

    private static final Class<?> NODE = nodeClass();

    private static Class<?> nodeClass() {
        try {
            return Class.forName("MCTS$Node");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("MCTS.Node not found; update MCTSTests if it was renamed", e);
        }
    }

    private static Object newNode(Ilayout layout, Object parent, boolean maxToPlay, int move) {
        try {
            Constructor<?> c = NODE.getDeclaredConstructors()[0];
            c.setAccessible(true);
            return c.newInstance(layout, parent, maxToPlay, move);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Field field(Class<?> k, String name) {
        try {
            Field f = k.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("field " + name + " not found in " + k.getName(), e);
        }
    }

    private static void set(Object o, String name, Object v) {
        try {
            field(o.getClass(), name).set(o, v);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static Object get(Object o, String name) {
        try {
            return field(o.getClass(), name).get(o);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static int n(Object node) {
        return (Integer) get(node, "n");
    }

    private static double w(Object node) {
        return (Double) get(node, "w");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> kids(Object node) {
        return (List<Object>) get(node, "children");
    }

    private static Object invoke(Object target, String name, Object... args) {
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                try {
                    m.setAccessible(true);
                    return m.invoke(target, args);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException re) throw re;
                    throw new AssertionError(e.getCause());
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }
        throw new AssertionError("method " + name + "/" + args.length + " not found in MCTS");
    }

    /** Adds a child with the given statistics to {@code parent}. */
    private static Object addChild(Object parent, int n, double w, boolean maxToPlay, int move) {
        Object child = newNode(null, parent, maxToPlay, move);
        set(child, "n", n);
        set(child, "w", w);
        kids(parent).add(child);
        return child;
    }

    /** Runs the tree policy at a node and returns the index of the chosen child. */
    private static int pick(double c, boolean parentIsMax, int parentN, double[][] stats) {
        Object parent = newNode(null, null, parentIsMax, -1);
        set(parent, "n", parentN);
        for (int i = 0; i < stats.length; i++) addChild(parent, (int) stats[i][0], stats[i][1], !parentIsMax, i);
        Object chosen = invoke(new MCTS(1, c, 0), "bestChild", parent);
        return kids(parent).indexOf(chosen);
    }

    // =====================================================================
    // 1. Contract
    // =====================================================================

    @Test
    void constructorRejectsNonPositiveIterations() {
        assertThrows(IllegalArgumentException.class, () -> new MCTS(0, 0.2, 1));
        assertThrows(IllegalArgumentException.class, () -> new MCTS(-5, 0.2, 1));
    }

    @Test
    void constructorRejectsNegativeExploration() {
        assertThrows(IllegalArgumentException.class, () -> new MCTS(10, -0.1, 1));
    }

    @Test
    void zeroExplorationStillGivesALegalMove() {
        RefBoard b = RefBoard.empty();
        assertTrue(b.getAvailableMoves().contains(new MCTS(200, 0.0, 1).chooseMove(b)));
    }

    @Test
    void terminalRootIsRejected() {
        RefBoard xWins = new RefBoard("XXXOO....", O);
        RefBoard draw = new RefBoard("XOXXOOOXX", X);
        assertTrue(xWins.isGameOver());
        assertTrue(draw.isGameOver());
        assertThrows(IllegalStateException.class, () -> new MCTS(100, 0.2, 1).chooseMove(xWins));
        assertThrows(IllegalStateException.class, () -> new MCTS(100, 0.2, 1).chooseMove(draw));
    }

    @Test
    void forcedMoveIsReturnedWithoutSearching() {
        // only cell 8 is free (position not terminal): X O X / X O O / O X .
        RefBoard b = new RefBoard("XOXXOOOX.", X);
        assertEquals(8, new MCTS(1, 0.2, 1).chooseMove(b));
        // only cell 8 is free, O to play
        RefBoard c = new RefBoard("XOXOXOOX.", O);
        assertEquals(1, c.getAvailableMoves().size());
        assertEquals(8, new MCTS(1, 0.2, 1).chooseMove(c));
    }

    @Test
    void chosenMoveIsAlwaysLegal() {
        Random r = new Random(11);
        for (int i = 0; i < 150; i++) {
            RefBoard b = randomPosition(r);
            int m = new MCTS(150, 0.2, i).chooseMove(b);
            assertTrue(b.getAvailableMoves().contains(m), "illegal " + m + " in " + b.key());
        }
    }

    @Test
    void argumentIsNeverModified() {
        Random r = new Random(12);
        for (int i = 0; i < 60; i++) {
            RefBoard b = randomPosition(r);
            String before = b.key();
            Set<Integer> freeBefore = b.getAvailableMoves();
            new MCTS(300, 0.2, i).chooseMove(b);
            assertEquals(before, b.key());
            assertEquals(freeBefore, b.getAvailableMoves());
            assertFalse(b.isGameOver());
        }
    }

    @Test
    void sameSeedAndIterationsGiveTheSameMove() {
        Random r = new Random(13);
        for (int i = 0; i < 25; i++) {
            RefBoard b = randomPosition(r);
            int a = new MCTS(400, 0.2, 99).chooseMove(b);
            int c = new MCTS(400, 0.2, 99).chooseMove(b);
            assertEquals(a, c, "not reproducible in " + b.key());
        }
    }

    @Test
    void reusingTheSameInstanceIsReproducible() {
        MCTS ai = new MCTS(400, 0.2, 5);
        Random r = new Random(14);
        for (int i = 0; i < 15; i++) {
            RefBoard b = randomPosition(r);
            assertEquals(ai.chooseMove(b), ai.chooseMove(b), "state leaks between calls in " + b.key());
        }
    }

    @Test
    void differentSeedsAreAllLegalOnTheEmptyBoard() {
        RefBoard b = RefBoard.empty();
        for (long seed = 0; seed < 20; seed++) {
            assertTrue(b.getAvailableMoves().contains(new MCTS(100, 0.2, seed).chooseMove(b)));
        }
    }

    // =====================================================================
    // 2. Tactics
    // =====================================================================

    @Test
    void takesTheWinningMoveOnEveryLineForBothPlayers() {
        for (Ilayout.ID p : new Ilayout.ID[] {X, O}) {
            for (int[] line : LINES) {
                RefBoard b = winInOne(line, p);
                assertEquals(List.of(line[2]), winningMoves(b, p), "bad fixture " + b.key());
                assertEquals(line[2], new MCTS(1500, 0.2, 3).chooseMove(b),
                    p + " should win at " + line[2] + " in " + b.key());
            }
        }
    }

    @Test
    void blocksTheOpponentOnEveryLineForBothPlayers() {
        for (Ilayout.ID p : new Ilayout.ID[] {X, O}) {
            for (int[] line : LINES) {
                RefBoard b = blockInOne(line, p);
                assertEquals(line[2], new MCTS(3000, 0.2, 3).chooseMove(b),
                    p + " should block at " + line[2] + " in " + b.key());
            }
        }
    }

    @Test
    void prefersWinningNowOverBlocking() {
        // X: 0,1 (wins at 2); O: 3,4 (wins at 5)
        assertEquals(2, new MCTS(2000, 0.2, 1).chooseMove(new RefBoard("XX.OO....", X)));
        assertEquals(5, new MCTS(2000, 0.2, 1).chooseMove(new RefBoard("XX.OO....", O)));
    }

    @Test
    void choosesAMinimaxOptimalMoveInAWinningPosition() {
        // X has 0 and 4 against O's 1 and 8; the chosen move must be minimax-optimal
        RefBoard b = new RefBoard("XO..X...O", X);
        int m = new MCTS(4000, 0.2, 1).chooseMove(b);
        assertTrue(optimalMoves(b).contains(m), "chose " + m + ", optimal " + optimalMoves(b));
    }

    @Test
    void playingAsOGivesTheMirroredDecisionOfPlayingAsX() {
        // The algorithm must not depend on X/O: swapping the marks must give the same move.
        Random r = new Random(15);
        for (int i = 0; i < 40; i++) {
            RefBoard b = randomPosition(r);
            int asIs = new MCTS(300, 0.2, i).chooseMove(b);
            int swapped = new MCTS(300, 0.2, i).chooseMove(b.swapped());
            assertEquals(asIs, swapped, "asymmetry between X and O in " + b.key());
        }
    }

    // =====================================================================
    // 3. Tree policy (Fig. 2 of the assignment, c = 0.2)
    // =====================================================================

    @Test
    void figure2RootMaxPicksTheHighestUcb() {
        // values 0.65, 0.31, 0.74 -> third
        assertEquals(2, pick(0.2, true, 11, new double[][] {{4, 2}, {1, 0}, {5, 3}}));
    }

    @Test
    void figure2MinNodePicksTheLowestLcb() {
        // values 0.19 and 0.75 -> first
        assertEquals(0, pick(0.2, false, 5, new double[][] {{3, 1}, {1, 1}}));
    }

    @Test
    void figure2MaxNodeAtLevelThreePicksTheHighestUcb() {
        // values 1.21 and 0.21 -> first
        assertEquals(0, pick(0.2, true, 3, new double[][] {{1, 1}, {1, 0}}));
    }

    @Test
    void sameStatisticsWithOppositeRolesPickOppositeChildren() {
        double[][] stats = {{1, 1}, {1, 0}};
        assertEquals(0, pick(0.2, true, 3, stats));   // MAX likes the winning child
        assertEquals(1, pick(0.2, false, 3, stats));  // MIN likes the losing child
    }

    @Test
    void minimizingTheUcbItselfWouldBeWrong() {
        // MIN with a very unvisited, good-for-MIN child: if MIN minimized (merit + bonus)
        // it would avoid it. With (merit - bonus) it is attracted to it.
        // children: (20, 12) merit 0.6  vs  (2, 0) merit 0 ; parent n = 23
        assertEquals(1, pick(0.2, false, 23, new double[][] {{20, 12}, {2, 0}}));
    }

    @Test
    void unvisitedChildIsChosenBeforeTheFormula() {
        // (10, 10) has the best merit but the n = 0 child must still go first (MAX and MIN)
        assertEquals(1, pick(0.2, true, 10, new double[][] {{10, 10}, {0, 0}}));
        assertEquals(1, pick(0.2, false, 10, new double[][] {{10, 0}, {0, 0}}));
    }

    @Test
    void firstUnvisitedChildWinsAndNothingIsNaN() {
        // parent n = 0 would give ln(0); must not be evaluated while a child is unvisited
        assertEquals(0, pick(0.2, true, 0, new double[][] {{0, 0}, {0, 0}, {0, 0}}));
        assertEquals(1, pick(0.2, true, 5, new double[][] {{5, 3}, {0, 0}, {0, 0}}));
    }

    @Test
    void tiesKeepTheFirstChild() {
        assertEquals(0, pick(0.2, true, 9, new double[][] {{4, 2}, {4, 2}, {4, 2}}));
        assertEquals(0, pick(0.2, false, 9, new double[][] {{4, 2}, {4, 2}, {4, 2}}));
    }

    @Test
    void zeroExplorationIsPureExploitation() {
        assertEquals(1, pick(0.0, true, 51, new double[][] {{50, 25}, {1, 1}}));   // best mean for MAX
        assertEquals(0, pick(0.0, false, 51, new double[][] {{50, 5}, {1, 1}}));   // lowest mean for MIN
    }

    @Test
    void largeExplorationFavoursTheLessVisitedChild() {
        // c = 2: the child tried once beats the one with a perfect record over 50 visits
        assertEquals(1, pick(2.0, true, 51, new double[][] {{50, 50}, {1, 0}}));
        // with the small c of the assignment it is the other way round
        assertEquals(0, pick(0.2, true, 51, new double[][] {{50, 50}, {1, 0}}));
    }

    // =====================================================================
    // 4. Phases in isolation (private methods through reflection)
    // =====================================================================

    @Test
    void scoreIsAlwaysFromTheViewpointOfMax() {
        RefBoard xWins = new RefBoard("XXXOO....", O);
        RefBoard oWins = new RefBoard("XX.OOO..X", X);
        RefBoard draw = new RefBoard("XOXXOOOXX", X);
        MCTS ai = new MCTS(1, 0.2, 0);

        set(ai, "maxPlayer", X);
        assertEquals(1.0, (Double) invoke(ai, "scoreMax", xWins));
        assertEquals(0.0, (Double) invoke(ai, "scoreMax", oWins));
        assertEquals(0.5, (Double) invoke(ai, "scoreMax", draw));

        set(ai, "maxPlayer", O);
        assertEquals(0.0, (Double) invoke(ai, "scoreMax", xWins));
        assertEquals(1.0, (Double) invoke(ai, "scoreMax", oWins));
        assertEquals(0.5, (Double) invoke(ai, "scoreMax", draw));
    }

    @Test
    void backPropagationUpdatesTheWholePathAndOnlyThePath() {
        Object root = newNode(null, null, true, -1);
        set(root, "n", 10);
        set(root, "w", 4.0);
        Object a = addChild(root, 6, 3.0, false, 0);
        Object sibling = addChild(root, 2, 1.0, false, 1);
        Object b = addChild(a, 3, 2.0, true, 2);
        Object cousin = addChild(a, 1, 0.0, true, 3);

        invoke(new MCTS(1, 0.2, 0), "backPropagation", b, 0.5);

        assertEquals(4, n(b));      assertEquals(2.5, w(b), 1e-9);
        assertEquals(7, n(a));      assertEquals(3.5, w(a), 1e-9);
        assertEquals(11, n(root));  assertEquals(4.5, w(root), 1e-9);
        assertEquals(2, n(sibling)); assertEquals(1.0, w(sibling), 1e-9);
        assertEquals(1, n(cousin));  assertEquals(0.0, w(cousin), 1e-9);
    }

    @Test
    void backPropagationAddsTheSameRewardAtMaxAndMinLevels() {
        // path root(MAX) -> a(MIN) -> b(MAX) -> c(MIN): the reward is never inverted
        Object root = newNode(null, null, true, -1);
        Object a = addChild(root, 0, 0.0, false, 0);
        Object b = addChild(a, 0, 0.0, true, 1);
        Object c = addChild(b, 0, 0.0, false, 2);

        MCTS ai = new MCTS(1, 0.2, 0);
        invoke(ai, "backPropagation", c, 1.0);
        invoke(ai, "backPropagation", c, 0.0);
        invoke(ai, "backPropagation", c, 0.5);

        for (Object node : new Object[] {root, a, b, c}) {
            assertEquals(3, n(node));
            assertEquals(1.5, w(node), 1e-9);
        }
    }

    @Test
    void backPropagationFromTheRootOnlyTouchesTheRoot() {
        Object root = newNode(null, null, true, -1);
        Object child = addChild(root, 1, 1.0, false, 0);
        invoke(new MCTS(1, 0.2, 0), "backPropagation", root, 1.0);
        assertEquals(1, n(root));
        assertEquals(1, n(child));
    }

    @Test
    void expansionCreatesOneChildPerFreeCellWithTheRightMove() {
        // X O X / O X . / . . O, X to play, free cells 5, 6, 7
        RefBoard layout = new RefBoard("XOXOX...O", X);
        String before = layout.key();
        MCTS ai = new MCTS(1, 0.2, 0);
        set(ai, "maxPlayer", X);
        Object leaf = newNode(layout, null, true, -1);

        invoke(ai, "expandLeaf", leaf);

        List<Object> children = kids(leaf);
        assertEquals(3, children.size());
        int[] expectedMoves = {5, 6, 7};
        for (int i = 0; i < 3; i++) {
            Object child = children.get(i);
            assertEquals(expectedMoves[i], get(child, "move"));
            assertSame(leaf, get(child, "parent"));
            assertEquals(0, n(child));
            assertEquals(0.0, w(child), 1e-9);
            assertNotSame(layout, get(child, "layout"));
        }
        assertEquals(before, layout.key(), "expansion changed the parent layout");
    }

    @Test
    void expansionMarksWhoChoosesBelowEachChild() {
        // X O X / O X . / . . O ; cell 6 gives X the diagonal (terminal), cells 5 and 7 do not
        RefBoard layout = new RefBoard("XOXOX...O", X);
        MCTS ai = new MCTS(1, 0.2, 0);

        set(ai, "maxPlayer", X);
        Object leafX = newNode(layout, null, true, -1);
        invoke(ai, "expandLeaf", leafX);
        // after X moves it is O's turn: the non-terminal children are MIN nodes
        assertEquals(false, get(kids(leafX).get(0), "maxToPlay"));
        assertEquals(false, get(kids(leafX).get(2), "maxToPlay"));

        // same board, but the agent plays as O: those children are now MAX nodes
        set(ai, "maxPlayer", O);
        Object leafO = newNode(layout, null, false, -1);
        invoke(ai, "expandLeaf", leafO);
        assertEquals(true, get(kids(leafO).get(0), "maxToPlay"));
        assertEquals(true, get(kids(leafO).get(2), "maxToPlay"));
    }

    @Test
    void simulationEndsInATerminalStateWithoutTouchingTheStart() {
        Random r = new Random(16);
        MCTS ai = new MCTS(1, 0.2, 0);
        for (int i = 0; i < 50; i++) {
            RefBoard start = (i == 0) ? RefBoard.empty() : randomPosition(r);
            String before = start.key();
            Ilayout end = (Ilayout) invoke(ai, "simulation", start, new Random(i));
            assertTrue(end.isGameOver());
            assertNotSame(start, end);
            assertEquals(before, start.key(), "simulation changed its input");
        }
    }

    @Test
    void simulationFromATerminalStateReturnsItself() {
        RefBoard done = new RefBoard("XXXOO....", O);
        Ilayout end = (Ilayout) invoke(new MCTS(1, 0.2, 0), "simulation", done, new Random(1));
        assertEquals(X, end.getWinner());
    }

    @Test
    void simulationIsReproducibleWithTheSameRandom() {
        MCTS ai = new MCTS(1, 0.2, 0);
        RefBoard start = RefBoard.empty();
        for (long seed = 0; seed < 20; seed++) {
            String a = ((RefBoard) invoke(ai, "simulation", start, new Random(seed))).key();
            String b = ((RefBoard) invoke(ai, "simulation", start, new Random(seed))).key();
            assertEquals(a, b);
        }
    }

    @Test
    void simulationOutcomesAreVariedAndOnlyFromLegalGames() {
        // from the empty board random games give X wins, O wins and draws
        MCTS ai = new MCTS(1, 0.2, 0);
        Set<Ilayout.ID> seen = new HashSet<>();
        Random r = new Random(17);
        for (int i = 0; i < 300; i++) {
            seen.add(((Ilayout) invoke(ai, "simulation", RefBoard.empty(), r)).getWinner());
        }
        assertEquals(Set.of(X, O, BLANK), seen);
    }

    @Test
    void finalMoveIsTheMostVisitedChildNotTheBestMean() {
        Object root = newNode(null, null, true, -1);
        addChild(root, 3, 3.0, false, 10);    // mean 1.0 but few visits
        addChild(root, 40, 24.0, false, 20);  // mean 0.6, most visits
        addChild(root, 10, 6.0, false, 30);
        Object chosen = invoke(new MCTS(1, 0.2, 0), "mostVisitedNode", root);
        assertEquals(20, get(chosen, "move"));
    }

    @Test
    void finalTieOnVisitsGoesToTheHigherWinRateThenTheFirst() {
        Object root = newNode(null, null, true, -1);
        addChild(root, 5, 2.0, false, 10);
        addChild(root, 5, 3.0, false, 20);   // same visits, better rate
        addChild(root, 5, 3.0, false, 30);   // same visits and rate, later -> loses the tie
        addChild(root, 4, 4.0, false, 40);
        Object chosen = invoke(new MCTS(1, 0.2, 0), "mostVisitedNode", root);
        assertEquals(20, get(chosen, "move"));
    }

    @Test
    void selectionDescendsWithMaxThenMinAndStopsAtALeaf() {
        MCTS ai = new MCTS(1, 0.2, 0);
        Object root = newNode(null, null, true, -1);
        set(root, "n", 6);
        Object a = addChild(root, 5, 4.0, false, 0);      // UCB 0.92 beats B (0.27)
        addChild(root, 1, 0.0, false, 1);
        addChild(a, 2, 2.0, true, 2);                     // MIN: -1 + bonus
        Object a2 = addChild(a, 2, 0.0, true, 3);         // MIN: 0 + bonus -> chosen
        set(a, "n", 5);

        assertSame(a2, invoke(ai, "selectMove", root));
    }

    @Test
    void selectionOnALeafReturnsThatLeaf() {
        Object root = newNode(null, null, true, -1);
        assertSame(root, invoke(new MCTS(1, 0.2, 0), "selectMove", root));
    }

    // =====================================================================
    // 5. Whole games
    // =====================================================================

    @Test
    @Tag("slow")
    void neverLosesToARandomPlayerAsXNorAsO() {
        int games = 100;
        int[] asX = new int[3];
        int[] asO = new int[3];
        for (int g = 0; g < games; g++) {
            Ilayout.ID r1 = playGame(mctsAgent(1000, 0.2, g), randomAgent(new Random(g)));
            asX[r1 == X ? 0 : r1 == BLANK ? 1 : 2]++;
            Ilayout.ID r2 = playGame(randomAgent(new Random(1000 + g)), mctsAgent(1000, 0.2, g));
            asO[r2 == O ? 0 : r2 == BLANK ? 1 : 2]++;
        }
        assertEquals(0, asX[2], "lost as X");
        assertEquals(0, asO[2], "lost as O");
        assertTrue(asX[0] >= 90, "too few wins as X: " + asX[0]);
        assertTrue(asO[0] >= 80, "too few wins as O: " + asO[0]);
    }

    @Test
    @Tag("slow")
    void neverLosesToAPerfectMinimaxPlayerAsXNorAsO() {
        // c = 1.0: with the very small c = 0.2 of Fig. 2 the search sometimes locks onto a bad
        // first reply (see smallExplorationLocksOntoBadMovesMoreOften), so strength is checked here.
        for (int g = 0; g < 20; g++) {
            Ilayout.ID asX = playGame(mctsAgent(2000, 1.0, g), minimaxAgent(new Random(g)));
            assertNotEquals(O, asX, "MCTS (X) lost to minimax, game " + g);
            Ilayout.ID asO = playGame(minimaxAgent(new Random(100 + g)), mctsAgent(2000, 1.0, g));
            assertNotEquals(X, asO, "MCTS (O) lost to minimax, game " + g);
        }
    }

    @Test
    @Tag("slow")
    void selfPlayEndsInADraw() {
        for (int g = 0; g < 10; g++) {
            Ilayout.ID result = playGame(mctsAgent(2000, 1.0, g), mctsAgent(2000, 1.0, 500 + g));
            assertEquals(BLANK, result, "self-play game " + g + " was not a draw");
        }
    }

    @Test
    @Tag("slow")
    void beatsRandomForDifferentExplorationConstants() {
        for (double c : new double[] {0.1, 0.2, 0.5, Math.sqrt(2)}) {
            for (int g = 0; g < 30; g++) {
                assertNotEquals(O, playGame(mctsAgent(1000, c, g), randomAgent(new Random(g))),
                    "lost as X with c=" + c);
                assertNotEquals(X, playGame(randomAgent(new Random(g)), mctsAgent(1000, c, g)),
                    "lost as O with c=" + c);
            }
        }
    }

    @Test
    @Tag("slow")
    void chosenMoveIsMinimaxOptimalOnMostRandomPositions() {
        Random r = new Random(18);
        int positions = 60;
        int optimal = 0;
        for (int i = 0; i < positions; i++) {
            RefBoard b = randomPosition(r);
            int m = new MCTS(4000, 0.2, i).chooseMove(b);
            if (optimalMoves(b).contains(m)) optimal++;
        }
        assertTrue(optimal >= 54, "only " + optimal + "/" + positions + " moves were optimal");
    }

    @Test
    @Tag("slow")
    void moreIterationsGiveABetterRecordAgainstMinimax() {
        // with very few iterations the agent is noticeably weaker than with many
        int weak = 0;
        int strong = 0;
        Random r = new Random(19);
        for (int i = 0; i < 40; i++) {
            RefBoard b = randomPosition(r);
            if (optimalMoves(b).contains(new MCTS(20, 0.2, i).chooseMove(b))) weak++;
            if (optimalMoves(b).contains(new MCTS(3000, 0.2, i).chooseMove(b))) strong++;
        }
        assertTrue(strong >= weak, "3000 iterations (" + strong + ") should not be worse than 20 (" + weak + ")");
    }

    /** Number of random positions (out of {@code positions}) where MCTS plays a minimax-optimal move. */
    private static int optimalCount(double c, int iterations, int positions) {
        Random r = new Random(18);
        int optimal = 0;
        for (int i = 0; i < positions; i++) {
            RefBoard b = randomPosition(r);
            if (optimalMoves(b).contains(new MCTS(iterations, c, i).chooseMove(b))) optimal++;
        }
        return optimal;
    }

    @Test
    @Tag("slow")
    void largerExplorationFindsTheOptimalMoveEveryTime() {
        for (double c : new double[] {0.5, 1.0, Math.sqrt(2)}) {
            assertEquals(60, optimalCount(c, 3000, 60), "c = " + c);
        }
    }

    @Test
    @Tag("slow")
    void smallExplorationLocksOntoBadMovesMoreOften() {
        // Documents the effect of c: the tiny c of Fig. 2 is not better than a larger one.
        assertTrue(optimalCount(0.1, 3000, 60) <= optimalCount(1.0, 3000, 60));
    }

    @Test
    void constructorRejectsInfiniteExploration() {
        assertThrows(IllegalArgumentException.class, () -> new MCTS(10, Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class, () -> new MCTS(10, Double.NEGATIVE_INFINITY, 1));
    }

    @Test
    void chooseMoveRejectsANullState() {
        assertThrows(NullPointerException.class, () -> new MCTS(10, 0.5, 1).chooseMove(null));
    }
}
