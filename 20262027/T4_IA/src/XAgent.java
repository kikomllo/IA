/**
 * A demo reactive agent that plays randomly. It is kept as the reference opponent of {@link MCTSAgent}.
 * <p>
 * It keeps no state between moves, and it only returns cells that are free in the layout it receives.
 *
 * @author 85782 - Pedro Salgueiro
 * @author 90247 - Diogo Carvalho
 * @author 84085 - Francisco Melo
 * @version 2026-11-22
 * @see MCTSAgent
 */
public class XAgent {
	/**
	 * Creates the agent; it has no state.
	 */
	public XAgent() {
	}

	/**
	 * Chooses uniformly at random one of the free cells of the layout, which is not modified.
	 *
	 * @param board         the board to play on
	 * @return a valid random move (the index of a free cell)
	 * @throws IllegalArgumentException if the board has no free cell
	 * @see Ilayout#getAvailableMoves()
	 */
	static int play(Ilayout board) {
        int[] moves = new int[board.getAvailableMoves().size()];
        int index = 0;
        for (Integer item : board.getAvailableMoves()) {
            moves[index++] = item;
        }
        int randomMove = moves[new java.util.Random().nextInt(moves.length)];
        return randomMove;
    }
}
