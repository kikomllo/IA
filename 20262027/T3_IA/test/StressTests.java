import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import classes.BallSortLayout;
import classes.BestFirst;

/**
 * Stress tests: many random boards, always with a fixed seed so a failure can be repeated.
 * The answers are checked against the reference solver ({@link TestSupport#optimal}) when the
 * board is small enough, and against properties that must always hold when it is not
 * (valid solution, h never above the cost that is left, time under one second).
 *
 * <p>The tests tagged {@code perf} are the slow ones. Run from the T3_IA folder.
 */
class StressTests {

    // ---------------------------------------------------------------
    // Small boards: A* must give exactly the same answer as the reference solver
    // ---------------------------------------------------------------

    @Test
    public void testManySmallScrambledBoardsMatchReference() {
        Random rnd = new Random(1001);
        for (int k = 0; k < 600; k++) {
            String board = smallShape(rnd, true);
            assertEquals(TestSupport.optimal(board), cost(board), "wrong cost on\n" + board);
        }
    }

    @Test
    public void testManySmallShuffledBoardsMatchReference() {
        // Fully shuffled boards: some have no solution, and A* has to say so
        Random rnd = new Random(1002);
        int unsolvable = 0;
        for (int k = 0; k < 500; k++) {
            String board = smallShape(rnd, false);
            int expected = TestSupport.optimal(board);
            if (expected < 0) unsolvable++;
            assertEquals(expected, cost(board), "wrong cost on\n" + board);
        }
        assertTrue(unsolvable > 0, "the seed should give at least one unsolvable board");
    }

    @Test
    public void testTubeOrderDoesNotChangeTheCost() {
        Random rnd = new Random(1003);
        for (int k = 0; k < 150; k++) {
            String board = smallShape(rnd, false);
            List<String> tubes = TestSupport.parse(board);
            int H = board.split("\n").length;
            int expected = cost(board);
            for (int s = 0; s < 3; s++) {
                Collections.shuffle(tubes, rnd);
                assertEquals(expected, cost(TestSupport.format(tubes, H)), "tube order changed the cost of\n" + board);
            }
        }
    }

    @Test
    public void testSameBoardTwiceGivesTheSameCost() {
        Random rnd = new Random(1004);
        for (int k = 0; k < 50; k++) {
            String board = TestSupport.shuffled(5, 4, 2, rnd);
            assertEquals(cost(board), cost(board));
        }
    }

    @Test
    public void testSearchDoesNotChangeTheInitialLayout() {
        Random rnd = new Random(1005);
        for (int k = 0; k < 50; k++) {
            String board = TestSupport.shuffled(5, 4, 2, rnd);
            BallSortLayout start = new BallSortLayout(board);
            new BestFirst().solve(start, null);
            assertEquals(board, start.toString());
        }
    }

    // ---------------------------------------------------------------
    // The solution that A* returns: every step is a legal move
    // ---------------------------------------------------------------

    @Test
    public void testSolutionPathIsValidAndOptimal() {
        Random rnd = new Random(1006);
        for (int k = 0; k < 200; k++) {
            String board = smallShape(rnd, true);
            List<String> path = path(board);
            int expected = TestSupport.optimal(board);

            assertEquals(expected + 1, path.size(), "path length on\n" + board);
            assertEquals(board, path.get(0), "the path must start at the initial board");
            assertTrue(TestSupport.isSolved(path.get(path.size() - 1)), "the path must end solved:\n" + board);
            for (int i = 0; i + 1 < path.size(); i++) {
                assertTrue(TestSupport.isLegalMove(path.get(i), path.get(i + 1)),
                        "illegal step " + i + " on\n" + path.get(i) + "->\n" + path.get(i + 1));
            }
        }
    }

    @Test
    @Tag("perf")
    public void testSolutionPathIsValidOnBigBoards() {
        // Too big for the reference solver, but the path can still be checked step by step
        Random rnd = new Random(1007);
        for (int k = 0; k < 40; k++) {
            String board = TestSupport.shuffled(7 + rnd.nextInt(3), 4 + rnd.nextInt(2), 2, rnd);
            List<String> path = path(board);
            if (path.isEmpty()) continue; // no solution, nothing to check
            assertEquals(board, path.get(0));
            assertTrue(TestSupport.isSolved(path.get(path.size() - 1)));
            for (int i = 0; i + 1 < path.size(); i++) {
                assertTrue(TestSupport.isLegalMove(path.get(i), path.get(i + 1)),
                        "illegal step " + i + " on\n" + path.get(i) + "->\n" + path.get(i + 1));
            }
        }
    }

    // ---------------------------------------------------------------
    // Admissible on big boards: along the optimal path, h never goes above the moves that are left
    // (if h was ever bigger, it would be a wrong "never more than the real number")
    // ---------------------------------------------------------------

    @Test
    @Tag("perf")
    public void testHeuristicNeverAboveTheMovesLeftOnBigBoards() {
        Random rnd = new Random(1008);
        for (int k = 0; k < 60; k++) {
            String board = TestSupport.shuffled(6 + rnd.nextInt(5), 4 + rnd.nextInt(2), 2, rnd);
            List<String> path = path(board);
            if (path.isEmpty()) continue;
            int total = path.size() - 1;
            for (int i = 0; i < path.size(); i++) {
                double h = new BallSortLayout(path.get(i)).getH(null);
                assertTrue(h <= total - i, "h=" + h + " but only " + (total - i) + " moves left on\n" + path.get(i));
            }
        }
    }

    // ---------------------------------------------------------------
    // Time: under one second for every board (Mooshak limit)
    // ---------------------------------------------------------------

    @Test
    @Tag("perf")
    public void testManyBoardsLikeSample1AreFast() {
        // Same shape as the real inputs: tall tubes, 4 or 5 colors, 2 empty tubes
        Random rnd = new Random(1009);
        long worst = 0;
        for (int k = 0; k < 150; k++) {
            int H = 6 + rnd.nextInt(5);
            String board = TestSupport.shuffled(H, 4 + rnd.nextInt(2), 2, rnd);
            long ms = timed(board);
            worst = Math.max(worst, ms);
            assertTrue(ms < 1000, "took " + ms + " ms on\n" + board);
        }
        System.out.println("[stress] slowest of 150 sample-like boards: " + worst + " ms");
    }

    @Test
    @Tag("perf")
    public void testBoardsWithLessEmptySpaceAreFast() {
        // One empty tube, or none: most of these have no solution, and A* must find that out quickly
        Random rnd = new Random(1010);
        long worst = 0;
        for (int k = 0; k < 100; k++) {
            int H = 4 + rnd.nextInt(7);
            String board = TestSupport.shuffled(H, 3 + rnd.nextInt(3), rnd.nextInt(2), rnd);
            long ms = timed(board);
            worst = Math.max(worst, ms);
            assertTrue(ms < 1000, "took " + ms + " ms on\n" + board);
        }
        System.out.println("[stress] slowest of 100 boards with 0 or 1 empty tube: " + worst + " ms");
    }

    @Test
    @Tag("perf")
    public void testMoreEmptyTubesAreFast() {
        Random rnd = new Random(1011);
        long worst = 0;
        for (int k = 0; k < 60; k++) {
            int H = 5 + rnd.nextInt(5);
            String board = TestSupport.shuffled(H, 4 + rnd.nextInt(2), 3 + rnd.nextInt(2), rnd);
            long ms = timed(board);
            worst = Math.max(worst, ms);
            assertTrue(ms < 1000, "took " + ms + " ms on\n" + board);
        }
        System.out.println("[stress] slowest of 60 boards with 3 or 4 empty tubes: " + worst + " ms");
    }

    @Test
    @Tag("perf")
    public void testTallTubesAreFast() {
        Random rnd = new Random(1012);
        long worst = 0;
        for (int H : new int[] { 12, 15, 18, 22 }) {
            for (int k = 0; k < 5; k++) {
                String board = TestSupport.shuffled(H, 4, 2, rnd);
                long ms = timed(board);
                worst = Math.max(worst, ms);
                assertTrue(ms < 1000, "H=" + H + " took " + ms + " ms on\n" + board);
            }
        }
        System.out.println("[stress] slowest tall board (H up to 22): " + worst + " ms");
    }

    @Test
    @Tag("perf")
    public void testVeryTallTubesStillFinish() {
        // H = 30 is far above the sample (H = 9). Measured at about 1.5 s on one board, so only
        // correctness is checked here (valid solution), not the one second limit.
        Random rnd = new Random(1012);
        for (int k = 0; k < 3; k++) {
            String board = TestSupport.shuffled(30, 4, 2, rnd);
            List<String> path = path(board);
            assertFalse(path.isEmpty(), "this board should have a solution:\n" + board);
            assertTrue(TestSupport.isSolved(path.get(path.size() - 1)));
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * A board small enough for the reference solver. {@code scrambled} gives boards that always
     * have a solution; otherwise the balls are fully shuffled (it may have no solution).
     */
    private String smallShape(Random rnd, boolean scrambled) {
        int H = 2 + rnd.nextInt(3);                    // 2..4
        int colors = 2 + rnd.nextInt(H == 4 ? 2 : 3);  // 2..3 (2..4 when H <= 3)
        int extra = rnd.nextInt(3);                    // 0..2 (scrambled boards need at least 1)
        if (scrambled) {
            return TestSupport.scrambled(H, colors, Math.max(extra, 1), 10 + rnd.nextInt(60), rnd);
        }
        return TestSupport.shuffled(H, colors, extra, rnd);
    }

    /** Cost printed by the search: g of the last state, or -1 when there is no solution. */
    private int cost(String board) {
        Iterator<BestFirst.State> it = new BestFirst().solve(new BallSortLayout(board), null);
        if (it == null) return -1;
        BestFirst.State last = null;
        while (it.hasNext()) last = it.next();
        return (int) last.getG();
    }

    /** The boards of the solution, from the initial one to the goal (empty list if no solution). */
    private List<String> path(String board) {
        Iterator<BestFirst.State> it = new BestFirst().solve(new BallSortLayout(board), null);
        List<String> result = new ArrayList<>();
        if (it == null) return result;
        while (it.hasNext()) result.add(it.next().toString());
        return result;
    }

    /** Milliseconds the search takes. Gives up (and fails) after 20 seconds. */
    private long timed(String board) {
        long[] ms = new long[1];
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            long t0 = System.nanoTime();
            cost(board);
            ms[0] = (System.nanoTime() - t0) / 1_000_000;
        }, "more than 20 s on\n" + board);
        return ms[0];
    }
}
