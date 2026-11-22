import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests of the rules in {@link Board}: winner detection, draw, move validation and children(). */
class BoardTests {

    private static final int[][] LINES = TestPositions.LINES;

    private static Board play(int... cells) {
        return TestPositions.play(cells);
    }

    private static List<Integer> list(int... cells) {
        List<Integer> l = new ArrayList<>();
        for (int c : cells) l.add(c);
        return l;
    }

    /** The cell played to reach {@code child} from {@code parent}. */
    private static int movePlayed(Board parent, Ilayout child) {
        Set<Integer> played = parent.getAvailableMoves();
        played.removeAll(child.getAvailableMoves());
        assertEquals(1, played.size());
        return played.iterator().next();
    }

    /* ---------- start of the game ---------- */

    @Test
    void newBoardIsEmptyAndXStarts() {
        Board b = new Board();
        assertFalse(b.isGameOver());
        assertEquals(Ilayout.ID.X, b.getTurn());
        assertEquals(9, b.getAvailableMoves().size());
        assertEquals("- - - \n- - - \n- - - ", b.toString());
    }

    @Test
    void resetRestoresTheStartingPosition() {
        Board b = play(0, 3, 1, 4, 2);          // X wins
        assertTrue(b.isGameOver());
        b.reset();
        assertFalse(b.isGameOver());
        assertEquals(Ilayout.ID.X, b.getTurn());
        assertEquals(9, b.getAvailableMoves().size());
        assertEquals(new Board(), b);
    }

    @Test
    void playersAlternate() {
        Board b = new Board();
        assertEquals(Ilayout.ID.X, b.getTurn());
        b.move(0);
        assertEquals(Ilayout.ID.O, b.getTurn());
        b.move(1);
        assertEquals(Ilayout.ID.X, b.getTurn());
    }

    @Test
    void moveByIndexIsRowMajor() {
        Board b = new Board();
        b.move(5);                              // row 1, column 2
        assertEquals("- - - \n- - X \n- - - ", b.toString());
        b.move(6);                              // row 2, column 0
        assertEquals("- - - \n- - X \nO - - ", b.toString());
        assertFalse(b.getAvailableMoves().contains(5));
        assertFalse(b.getAvailableMoves().contains(6));
    }

    /* ---------- winner detection ---------- */

    @Test
    void xWinsOnEveryLineWhicheverCellClosesIt() {
        for (int[] line : LINES)
            for (int last : line) {
                List<Integer> xs = new ArrayList<>();
                for (int c : line) if (c != last) xs.add(c);
                xs.add(last);                   // the cell that closes the line is played last
                List<Integer> os = TestPositions.fillers(line, 2);
                Board b = TestPositions.build(xs, os);
                String msg = "line " + Arrays.toString(line) + " closed by " + last;
                assertTrue(b.isGameOver(), msg);
                assertEquals(Ilayout.ID.X, b.getWinner(), msg);
            }
    }

    @Test
    void oWinsOnEveryLineWhicheverCellClosesIt() {
        for (int[] line : LINES)
            for (int last : line) {
                List<Integer> os = new ArrayList<>();
                for (int c : line) if (c != last) os.add(c);
                os.add(last);
                List<Integer> xs = TestPositions.fillers(line, 3);
                Board b = TestPositions.build(xs, os);
                String msg = "line " + Arrays.toString(line) + " closed by " + last;
                assertTrue(b.isGameOver(), msg);
                assertEquals(Ilayout.ID.O, b.getWinner(), msg);
            }
    }

    @Test
    void twoInARowIsNotAWin() {
        for (int[] line : LINES) {
            Board b = TestPositions.build(list(line[0], line[1]), TestPositions.fillers(line, 2));
            assertFalse(b.isGameOver(), "two cells of " + Arrays.toString(line));
        }
    }

    @Test
    void threeCellsOfDifferentPlayersAreNotAWin() {
        Board b = play(0, 1, 2);                // X O X on the top row
        assertFalse(b.isGameOver());
    }

    @Test
    void threeNonAlignedCellsAreNotAWin() {
        // X: 0, 5, 7 (no line)   O: 1, 4
        Board b = play(0, 1, 5, 4, 7);
        assertFalse(b.isGameOver());
    }

    @Test
    void theGameStopsAtTheWinningMove() {
        Board b = play(0, 3, 1, 4, 2);          // X completes the top row with 5 cells still free
        assertTrue(b.isGameOver());
        assertEquals(4, b.getAvailableMoves().size());
    }

    /* ---------- draw ---------- */

    @Test
    void fullBoardWithoutLineIsADraw() {
        // X O X
        // X O O
        // O X X
        Board b = play(0, 1, 2, 4, 3, 5, 7, 6, 8);
        assertTrue(b.isGameOver());
        assertEquals(Ilayout.ID.Blank, b.getWinner());
        assertTrue(b.getAvailableMoves().isEmpty());
    }

    @Test
    void lastMoveThatMakesALineIsAWinNotADraw() {
        // X O X
        // O X O
        // O X X     X closes the diagonal 0-4-8 with the 9th move
        Board b = play(0, 1, 2, 3, 7, 5, 8, 6, 4);
        assertTrue(b.isGameOver());
        assertEquals(Ilayout.ID.X, b.getWinner());
    }

    /* ---------- invalid moves ---------- */

    @Test
    void occupiedCellReturnsFalseAndChangesNothing() {
        Board b = play(4);
        Board before = (Board) b.clone();
        assertFalse(b.move(4));
        assertEquals(before, b);
        assertEquals(before.toString(), b.toString());
        assertEquals(Ilayout.ID.O, b.getTurn());                // still O's turn
        assertEquals(8, b.getAvailableMoves().size());
        assertFalse(b.isGameOver());
    }

    @Test
    void validMoveReturnsTrue() {
        assertTrue(new Board().move(0));
    }

    @Test
    void movingAfterTheGameEndedThrows() {
        Board b = play(0, 3, 1, 4, 2);
        assertThrows(IllegalStateException.class, () -> b.move(5));
    }

    @Test
    void gettingTheWinnerBeforeTheEndThrows() {
        assertThrows(IllegalStateException.class, () -> new Board().getWinner());
        assertThrows(IllegalStateException.class, () -> play(0, 1).getWinner());
    }

    /* ---------- clone ---------- */

    @Test
    void cloneIsAnIndependentCopy() {
        Board b = play(0, 4);
        Board copy = (Board) b.clone();
        assertEquals(b, copy);
        assertNotSame(b, copy);
        copy.move(8);
        assertEquals(7, b.getAvailableMoves().size());          // the original did not change
        assertEquals(6, copy.getAvailableMoves().size());
        assertEquals(Ilayout.ID.X, b.getTurn());
        assertEquals(Ilayout.ID.O, copy.getTurn());
    }

    @Test
    void equalBoardsHaveEqualHashCodes() {
        Board a = play(0, 4, 8);                // X: 0, 8   O: 4
        Board b = play(8, 4, 0);                // different order, same layout
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, play(0, 4, 1));      // a different layout
    }

    /* ---------- children() ---------- */

    @Test
    void childrenOfTheEmptyBoardAreNine() {
        assertEquals(9, new Board().children().size());
    }

    @Test
    void childrenOfAPositionHaveOneCopyPerFreeCell() {
        Board b = play(4, 0);
        List<Ilayout> children = b.children();
        assertEquals(7, children.size());
        for (Ilayout child : children) {
            assertNotSame(b, child);
            assertEquals(6, child.getAvailableMoves().size());
            assertEquals(Ilayout.ID.O, child.getTurn());        // b is X's turn (X: 4, O: 0), so children are O's
        }
    }

    @Test
    void childrenAreInAscendingOrderOfTheCellPlayed() {
        Board b = play(4, 0);
        List<Integer> cells = new ArrayList<>();
        for (Ilayout child : b.children()) cells.add(movePlayed(b, child));
        assertEquals(list(1, 2, 3, 5, 6, 7, 8), cells);
    }

    @Test
    void childrenDoNotModifyTheOriginal() {
        Board b = play(4, 0, 2);
        Board before = (Board) b.clone();
        b.children();
        assertEquals(before, b);
        assertEquals(before.toString(), b.toString());
        assertEquals(before.getTurn(), b.getTurn());
        assertEquals(before.getAvailableMoves(), b.getAvailableMoves());
        assertFalse(b.isGameOver());
    }

    @Test
    void childrenAreIndependentFromEachOther() {
        Board b = new Board();
        List<Ilayout> children = b.children();
        children.get(0).move(8);                // change one child
        assertEquals(9, b.getAvailableMoves().size());
        assertEquals(8, children.get(1).getAvailableMoves().size());
        assertEquals(7, children.get(0).getAvailableMoves().size());
    }

    @Test
    void childEqualsMovingOnACopy() {
        Board b = play(4, 0);
        List<Ilayout> children = b.children();
        List<Integer> free = new ArrayList<>(b.getAvailableMoves());
        Collections.sort(free);
        for (int i = 0; i < free.size(); i++) {
            Board expected = (Board) b.clone();
            expected.move(free.get(i));
            assertEquals(expected, children.get(i));
            assertEquals(expected.getTurn(), children.get(i).getTurn());
        }
    }

    @Test
    void aWinningChildIsTerminalWithTheRightWinner() {
        Board b = play(0, 3, 1, 4);             // X to move, can win at 2
        for (Ilayout child : b.children()) {
            int cell = movePlayed(b, child);
            assertEquals(cell == 2, child.isGameOver(), "cell " + cell);
            if (cell == 2) assertEquals(Ilayout.ID.X, child.getWinner());
        }
    }

    @Test
    void aTerminalPositionHasNoChildren() {
        assertTrue(play(0, 3, 1, 4, 2).children().isEmpty());              // win
        assertTrue(play(0, 1, 2, 4, 3, 5, 7, 6, 8).children().isEmpty());  // draw
    }

    /* ---------- exhaustive check against an independent reference ---------- */

    private static long games, xWins, oWins, draws;

    private static Ilayout.ID refWinner(int[] g) {
        for (int[] l : LINES)
            if (g[l[0]] != 0 && g[l[0]] == g[l[1]] && g[l[1]] == g[l[2]])
                return g[l[0]] == 1 ? Ilayout.ID.X : Ilayout.ID.O;
        return null;
    }

    private static void explore(Board b, int[] g, int turn, int count) {
        Ilayout.ID w = refWinner(g);
        boolean over = w != null || count == 9;
        assertEquals(over, b.isGameOver());
        if (over) {
            Ilayout.ID expected = w == null ? Ilayout.ID.Blank : w;
            assertEquals(expected, b.getWinner());
            assertTrue(b.children().isEmpty());
            games++;
            if (expected == Ilayout.ID.X) xWins++; else if (expected == Ilayout.ID.O) oWins++; else draws++;
            return;
        }
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (g[i] == 0) free.add(i);
        assertEquals(new HashSet<>(free), b.getAvailableMoves());
        assertEquals(turn == 1 ? Ilayout.ID.X : Ilayout.ID.O, b.getTurn());

        List<Ilayout> children = b.children();
        assertEquals(free.size(), children.size());
        for (int i = 0; i < free.size(); i++) {
            int cell = free.get(i);
            assertEquals(cell, movePlayed(b, children.get(i)));            // ascending order
            Board next = (Board) b.clone();
            assertTrue(next.move(cell));
            assertEquals(next, children.get(i));
            g[cell] = turn;
            explore(next, g, 3 - turn, count + 1);
            g[cell] = 0;
        }
    }

    /**
     * Plays every possible game (255 168) and compares every state with a brute-force check of the
     * 8 lines. The totals are the known ones for Tic-Tac-Toe.
     */
    @Test
    @Tag("slow")
    void everyPossibleGameAgreesWithAnIndependentReference() {
        games = xWins = oWins = draws = 0;
        explore(new Board(), new int[9], 1, 0);
        assertEquals(255_168, games);
        assertEquals(131_184, xWins);
        assertEquals(77_904, oWins);
        assertEquals(46_080, draws);
    }
}
