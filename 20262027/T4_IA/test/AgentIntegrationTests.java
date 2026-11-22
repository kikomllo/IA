import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.Function;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests: the real {@link Board} together with {@link MCTSAgent} and {@link MCTS}.
 * (MCTSTests tests the algorithm on a separate reference board; these confirm that both pieces work
 * together, including the order of {@code Board.children()}.)
 */
class AgentIntegrationTests {

    private static final Minimax ORACLE = new Minimax();

    private static Board play(int... cells) {
        return TestPositions.play(cells);
    }

    /** Plays a full game; returns +1 if X wins, -1 if O wins, 0 for a draw. */
    private static int playGame(Function<Board, Integer> asX, Function<Board, Integer> asO) {
        Board b = new Board();
        while (!b.isGameOver()) {
            int cell = (b.getTurn() == Ilayout.ID.X ? asX : asO).apply(b);
            assertTrue(b.getAvailableMoves().contains(cell), "illegal move " + cell + "\n" + b);
            assertTrue(b.move(cell));
        }
        Ilayout.ID w = b.getWinner();
        return w == Ilayout.ID.X ? 1 : w == Ilayout.ID.O ? -1 : 0;
    }

    private static int randomMove(Board b, Random random) {
        List<Integer> free = new ArrayList<>(b.getAvailableMoves());
        Collections.sort(free);
        return free.get(random.nextInt(free.size()));
    }

    /** Results {losses, draws, wins} of the agent against an opponent, half the games as X, half as O. */
    private static int[] versus(Function<Integer, Function<Board, Integer>> agentForGame,
                                Function<Integer, Function<Board, Integer>> opponentForGame, int games) {
        int[] result = new int[3];
        for (int i = 0; i < games; i++) {
            Function<Board, Integer> agent = agentForGame.apply(i), opponent = opponentForGame.apply(i);
            int outcome = (i % 2 == 0) ? playGame(agent, opponent) : -playGame(opponent, agent);
            result[outcome + 1]++;
        }
        return result;
    }

    /* ---------- contract ---------- */

    @Test
    void agentReturnsALegalCellAndLeavesTheBoardUntouched() {
        Board b = play(0, 4, 8);
        Board before = (Board) b.clone();
        int cell = new MCTSAgent(300, 0.5, 1).choose(b);
        assertTrue(before.getAvailableMoves().contains(cell));
        assertEquals(before, b);
        assertEquals(before.getTurn(), b.getTurn());
        assertEquals(before.getAvailableMoves(), b.getAvailableMoves());
        assertFalse(b.isGameOver());
    }

    @Test
    void sameSeedGivesTheSameMove() {
        Board b = play(0, 4);
        assertEquals(new MCTSAgent(500, 0.5, 9).choose(b), new MCTSAgent(500, 0.5, 9).choose(b));
        assertEquals(new MCTSAgent(500, 0.5, 9).choose(b), new MCTSAgent(500, 0.5, 9).choose(play(0, 4)));
    }

    @Test
    void theSameAgentCanBeUsedManyTimes() {
        MCTSAgent agent = new MCTSAgent(200, 0.5, 3);
        Board b = play(4);
        int first = agent.choose(b);
        assertEquals(first, agent.choose(b));
    }

    @Test
    void theOnlyFreeCellIsReturned() {
        // X O X
        // O X O
        // O X .     X to move, only cell 8 left and nobody has won yet
        Board b = play(0, 1, 2, 3, 4, 5, 7, 6);          // 8 cells played, no winner yet
        assertFalse(b.isGameOver());
        assertEquals(Set.of(8), b.getAvailableMoves());
        assertEquals(8, new MCTSAgent(50, 0.5, 1).choose(b));
    }

    @Test
    void aFinishedGameHasNoMove() {
        Board b = play(0, 3, 1, 4, 2);
        assertThrows(IllegalStateException.class, () -> new MCTSAgent(50, 0.5, 1).choose(b));
    }

    @Test
    void invalidParametersAreRejectedWhenTheAgentIsCreated() {
        assertThrows(IllegalArgumentException.class, () -> new MCTSAgent(0, 0.5, 1));
        assertThrows(IllegalArgumentException.class, () -> new MCTSAgent(10, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new MCTSAgent(10, Double.NaN, 1));
    }

    @Test
    void theDefaultStaticPlayReturnsALegalCell() {
        Board b = play(4);
        int cell = MCTSAgent.play(b);                      // same signature as XAgent.play
        assertTrue(b.getAvailableMoves().contains(cell));
        assertEquals(play(4), b);
    }

    @Test
    void xAgentStillPlaysARandomLegalCell() {
        Board b = play(4);
        for (int i = 0; i < 20; i++) assertTrue(b.getAvailableMoves().contains(XAgent.play(b)));
    }

    /* ---------- tactics on the real board ---------- */

    @Test
    void takesTheWinInOneMove() {
        List<TestPositions.Position> positions = TestPositions.winInOnePositions(ORACLE);
        assertFalse(positions.isEmpty());
        for (TestPositions.Position p : positions) {
            int cell = new MCTSAgent(1000, 0.5, 1).choose(p.board());
            assertEquals(p.target(), cell, p.name() + "\n" + p.board());
        }
    }

    @Test
    void blocksTheOpponentsWinInOneMove() {
        List<TestPositions.Position> positions = TestPositions.blockPositions(ORACLE);
        assertFalse(positions.isEmpty());
        for (TestPositions.Position p : positions) {
            int cell = new MCTSAgent(1000, 0.5, 1).choose(p.board());
            assertEquals(p.target(), cell, p.name() + "\n" + p.board());
        }
    }

    @Test
    void thePositionsUsedAboveCoverEveryLineForBothPlayers() {
        Set<String> winKeys = new HashSet<>(), blockKeys = new HashSet<>();
        for (TestPositions.Position p : TestPositions.winInOnePositions(ORACLE)) winKeys.add(p.name().substring(0, 12));
        for (TestPositions.Position p : TestPositions.blockPositions(ORACLE)) blockKeys.add(p.name().substring(0, 9));
        assertEquals(Set.of("win in 1 X l", "win in 1 O l"), winKeys);
        assertEquals(Set.of("block X l", "block O l"), blockKeys);
        assertTrue(TestPositions.winInOnePositions(ORACLE).size() >= 40);
        assertTrue(TestPositions.blockPositions(ORACLE).size() >= 40);
    }

    @Test
    void playsTheOptimalMoveFromTheEmptyBoardAndAfterACornerOpening() {
        // After X takes a corner, the only non-losing reply for O is the centre.
        Board b = play(0);
        assertEquals(Set.of(4), ORACLE.optimalMoves(b));
        assertEquals(4, new MCTSAgent(3000, 0.5, 1).choose(b));
    }

    /* ---------- full games ---------- */

    @Test
    @Tag("slow")
    void neverLosesToTheRandomAgentAsXOrAsO() {
        for (double c : new double[]{0.2, 0.5, 1.0}) {
            int[] r = versus(
                i -> { MCTSAgent a = new MCTSAgent(500, c, 100 + i); return a::choose; },
                i -> { Random rnd = new Random(7 + i); return b -> randomMove(b, rnd); },
                60);
            assertEquals(0, r[0], "losses against random with c=" + c + " (L/D/W=" + Arrays.toString(r) + ")");
            assertTrue(r[2] >= 40, "too few wins against random with c=" + c + " " + Arrays.toString(r));
        }
    }

    @Test
    @Tag("slow")
    void neverLosesToAPerfectMinimaxPlayerAsXOrAsO() {
        int[] r = versus(
            i -> { MCTSAgent a = new MCTSAgent(2000, 1.0, 5 + i); return a::choose; },
            i -> { Random rnd = new Random(11 + i); return b -> ORACLE.randomOptimalMove(b, rnd); },
            20);
        assertEquals(0, r[0], "losses against minimax: " + Arrays.toString(r));
        assertEquals(20, r[1], "against perfect play every game must be a draw: " + Arrays.toString(r));
    }

    @Test
    @Tag("slow")
    void mctsAgainstMctsIsADraw() {
        for (int i = 0; i < 10; i++) {
            MCTSAgent x = new MCTSAgent(2000, 1.0, i), o = new MCTSAgent(2000, 1.0, 100 + i);
            assertEquals(0, playGame(x::choose, o::choose), "game " + i);
        }
    }

    @Test
    @Tag("slow")
    void findsAnOptimalMoveInRandomPositions() {
        Random random = new Random(2024);
        for (int i = 0; i < 60; i++) {
            Board b = TestPositions.randomPosition(random);
            int cell = new MCTSAgent(3000, 0.5, i).choose(b);
            assertTrue(ORACLE.optimalMoves(b).contains(cell), "position " + i + "\n" + b + "\nchose " + cell);
        }
    }

    @Test
    void agentConstructorRejectsInfiniteExploration() {
        assertThrows(IllegalArgumentException.class, () -> new MCTSAgent(10, Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class, () -> new MCTSAgent(10, Double.NEGATIVE_INFINITY, 1));
    }

    @Test
    void agentRejectsANullBoard() {
        assertThrows(NullPointerException.class, () -> new MCTSAgent(10, 0.5, 1).choose(null));
    }
}
