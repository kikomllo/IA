import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import classes.BallSortLayout;
import classes.BestFirst;
import interfaces.Ilayout;

/**
 * T3 tests: heuristic values, admissibility/consistency, A* optimality against an
 * independent reference solver ({@link TestSupport#optimal}), symmetry, edge cases
 * and the 1 second time limit.
 *
 * <p>Tests tagged {@code perf} are the slow ones (they check the time limit).
 * Run from the T3_IA folder (the provided-input tests read {@code test/inputs/}).
 */
class AStarHeuristicTests {

    private static final String SAMPLE1 = readSample1();

    // ---------------------------------------------------------------
    // 1. Heuristic values, computed by hand.
    //    h(s) = N - sum over colors of the largest "base" of that color,
    //    base = run of equal colors from the bottom of a tube.
    // ---------------------------------------------------------------

    @Test
    public void hIsZeroOnGoal() {
        assertEquals(0.0, h("R B E\nR B E\n"), 0.0001);
    }

    @Test
    public void hOneBallOutOfPlace() {
        // tubes (bottom->top): R | R R | B B B | empty. N=6, bases R=2 B=3 -> 6-5 = 1
        assertEquals(1.0, h("E E B E\nE R B E\nR R B E\n"), 0.0001);
    }

    @Test
    public void hCountsOnlyOneBasePerColor() {
        // tubes: R B | R B | empty. Both tubes have an R base of size 1 but only ONE can stay.
        // N=4, maxBase R=1, B=0 -> 4-1 = 3 (a plain "balls above the base" count would say 2).
        assertEquals(3.0, h("B B E\nR R E\n"), 0.0001);
    }

    @Test
    public void hMixedBoard() {
        // tubes: R B R | R B B | empty. N=6, maxBase R=1, B=0 -> 5
        assertEquals(5.0, h("R B E\nB B E\nR R E\n"), 0.0001);
    }

    @Test
    public void hSample1() {
        // see IMPLEMENTATION_GUIDE.md, section 5.4: N=36, maxBase P=2 B=1 Y=1 R=0 -> 32
        assertEquals(32.0, h(SAMPLE1), 0.0001);
    }

    @Test
    public void hDoesNotDependOnTubeOrder() {
        List<String> tubes = TestSupport.parse(SAMPLE1);
        double expected = h(SAMPLE1);
        Random rnd = new Random(7);
        for (int k = 0; k < 10; k++) {
            Collections.shuffle(tubes, rnd);
            assertEquals(expected, h(TestSupport.format(tubes, 9)), 0.0001);
        }
    }

    // ---------------------------------------------------------------
    // 2. Admissible + consistent + zero only at goals,
    //    checked on EVERY reachable state of small random boards.
    //    (consistent + h(goal)=0 implies admissible for all states)
    // ---------------------------------------------------------------

    @Test
    public void hConsistentAndZeroOnlyAtGoalsOnWholeStateSpace() {
        Random rnd = new Random(2026);
        for (int k = 0; k < 40; k++) {
            int H = 2 + rnd.nextInt(2);        // 2..3
            int colors = 2 + rnd.nextInt(2);   // 2..3
            int extra = 1 + rnd.nextInt(2);    // 1..2
            String board = TestSupport.scrambled(H, colors, extra, 5 + rnd.nextInt(40), rnd);

            Set<String> seen = new HashSet<>();
            ArrayDeque<Ilayout> queue = new ArrayDeque<>();
            Ilayout start = new BallSortLayout(board);
            queue.add(start);
            seen.add(start.toString());
            while (!queue.isEmpty()) {
                Ilayout s = queue.poll();
                double hs = s.getH(null);
                assertTrue(hs >= 0, "negative h on\n" + s);
                assertEquals(s.isGoal(null), hs == 0.0, "h==0 must hold exactly at goals:\n" + s);
                for (Ilayout c : s.children()) {
                    assertTrue(hs - c.getH(null) <= 1.0 + 1e-9,
                            "h dropped by more than 1 in one move:\n" + s + "->\n" + c);
                    if (seen.add(c.toString())) queue.add(c);
                }
            }
        }
    }

    @Test
    public void hNeverAboveOptimalOnRandomBoards() {
        Random rnd = new Random(11);
        for (int k = 0; k < 100; k++) {
            String board = randomBoard(rnd);
            int opt = TestSupport.optimal(board);
            assertTrue(opt >= 0);
            assertTrue(h(board) <= opt, "h=" + h(board) + " > optimal=" + opt + " on\n" + board);
        }
    }

    // ---------------------------------------------------------------
    // 3. A* returns the optimal cost (compared with the reference solver)
    // ---------------------------------------------------------------

    @Test
    public void astarMatchesReferenceOnRandomBoards() {
        Random rnd = new Random(42);
        for (int k = 0; k < 120; k++) {
            String board = randomBoard(rnd);
            assertEquals(TestSupport.optimal(board), astar(board), "wrong cost on\n" + board);
        }
    }

    @Test
    public void astarMatchesReferenceOnMediumBoards() {
        Random rnd = new Random(99);
        for (int k = 0; k < 10; k++) {
            String board = TestSupport.scrambled(4, 4, 2, 60, rnd);
            assertEquals(TestSupport.optimal(board), astar(board), "wrong cost on\n" + board);
        }
    }

    @Test
    public void astarMatchesReferenceOnProvidedInputs() throws Exception {
        for (int n = 1; n <= 10; n++) {
            String name = String.format("T%02d.txt", n);
            String text = Files.readString(Path.of("test", "inputs", name));
            String board = text.substring(text.indexOf('\n') + 1);
            int expected = TestSupport.optimal(board);
            String out = runMain(text);
            String want = expected < 0 ? "no solution found\n" : expected + "\n";
            assertEquals(want, out, "wrong Main output for " + name);
        }
    }

    // ---------------------------------------------------------------
    // 4. Symmetry: tube order must not matter for equals/hashCode
    //    (needed so the search treats equivalent boards as ONE state)
    // ---------------------------------------------------------------

    @Test
    public void equalsIgnoresTubeOrder() {
        BallSortLayout a = new BallSortLayout("B E E\nR B E\n");
        BallSortLayout b = new BallSortLayout("E B E\nB R E\n"); // same tubes, first two swapped
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equalsStillSeesDifferentContent() {
        BallSortLayout a = new BallSortLayout("B E E\nR B E\n");
        BallSortLayout b = new BallSortLayout("E E E\nR B B\n");
        assertNotEquals(a, b);
    }

    @Test
    public void equalsAndHashConsistentOnShuffledSample() {
        List<String> tubes = TestSupport.parse(SAMPLE1);
        BallSortLayout original = new BallSortLayout(SAMPLE1);
        Random rnd = new Random(5);
        for (int k = 0; k < 10; k++) {
            Collections.shuffle(tubes, rnd);
            BallSortLayout shuffled = new BallSortLayout(TestSupport.format(tubes, 9));
            assertEquals(original, shuffled);
            assertEquals(original.hashCode(), shuffled.hashCode());
        }
    }

    // ---------------------------------------------------------------
    // 5. Edge cases (through Main, i.e. what Mooshak sees)
    // ---------------------------------------------------------------

    @Test
    public void alreadySolvedIsZero() throws Exception {
        assertEquals("0\n", runMain("2 3\nR B E\nR B E\n"));
    }

    @Test
    public void heightOneAlreadySolvedIsZero() throws Exception {
        assertEquals("0\n", runMain("1 3\nR B E\n"));
    }

    @Test
    public void singleColorNeedsOneMove() throws Exception {
        // tubes: R | R, capacity 2 -> move one R onto the other
        assertEquals("1\n", runMain("2 2\nE E\nR R\n"));
    }

    @Test
    public void unsolvableStillReported() throws Exception {
        assertEquals("no solution found\n", runMain("2 2\nB R\nR B\n"));
    }

    @Test
    public void sample1ThroughMain() throws Exception {
        assertEquals("37\n", runMain(TestSupport.withHeader(SAMPLE1)));
    }

    // ---------------------------------------------------------------
    // 6. Time limit: < 1 second (Mooshak). Tagged "perf" so they can be skipped.
    //    NB: inside JUnit the JVM is already warm, but Mooshak's machine is slower
    //    and pays JVM start-up, so aim well below the limit.
    // ---------------------------------------------------------------

    @Test
    @Tag("perf")
    public void sample1IsFast() {
        long t0 = System.nanoTime();
        int cost = astar(SAMPLE1);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertEquals(37, cost);
        assertTrue(ms < 1000, "Sample 1 took " + ms + " ms");
    }

    @Test
    @Tag("perf")
    public void bigRandomBoardsAreFast() {
        Random rnd = new Random(1234);
        int[][] shapes = { {9, 4, 2}, {8, 5, 1}, {7, 5, 2}, {9, 5, 1}, {6, 4, 2}, {8, 4, 2} }; // H, colors, extra tubes
        for (int[] sh : shapes) {
            String board = TestSupport.scrambled(sh[0], sh[1], sh[2], 70, rnd);
            long t0 = System.nanoTime();
            int cost = astar(board);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            assertTrue(cost >= h(board), "cost below h on\n" + board);
            assertTrue(cost <= 70, "cost above the scramble length on\n" + board);
            assertTrue(ms < 1000, "took " + ms + " ms on\n" + board);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static double h(String board) {
        return new BallSortLayout(board).getH(null);
    }

    /** Cost printed by the search: g of the last state, or -1 when there is no solution. */
    private static int astar(String board) {
        Iterator<BestFirst.State> it = new BestFirst().solve(new BallSortLayout(board), null);
        if (it == null) return -1;
        BestFirst.State last = null;
        while (it.hasNext()) last = it.next();
        return (int) last.getG();
    }

    private static String randomBoard(Random rnd) {
        int H = 2 + rnd.nextInt(3);        // 2..4
        int colors = 2 + rnd.nextInt(2);   // 2..3
        int extra = 1 + rnd.nextInt(2);    // 1..2
        return TestSupport.scrambled(H, colors, extra, 5 + rnd.nextInt(50), rnd);
    }

    private static String readSample1() {
        try {
            String text = Files.readString(Path.of("test", "inputs", "sample1.txt"));
            return text.substring(text.indexOf('\n') + 1);
        } catch (Exception e) {
            throw new RuntimeException("run the tests from the T3_IA folder", e);
        }
    }

    private String runMain(String input) throws Exception {
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(bytes, true, "UTF-8");
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captured);
            new Main().main();
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
            captured.close();
        }
        return bytes.toString("UTF-8").replace("\r\n", "\n");
    }
}
