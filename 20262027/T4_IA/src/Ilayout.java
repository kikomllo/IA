import java.util.*;

/**
 * A state of a two-player board game (here Tic-Tac-Toe), seen through the operations the search needs.
 * The search algorithm ({@link MCTS}) only talks to the game through this interface; {@link Board}
 * is the implementation.
 *
 * @author 85782 - Pedro Salgueiro
 * @author 90247 - Diogo Carvalho
 * @author 84085 - Francisco Melo
 * @version 2026-11-22
 * @see Board
 * @see MCTS
 */
interface Ilayout {
    /** Number of rows of the board. */
    int N=3;
    /** Number of columns of the board. */
    int M=3;
    /** Number of consecutive cells of the same player that win the game. */
    int K=3;

    /** The content of a cell and the identity of a player. */
    enum ID{
        /** An empty cell; as a winner, it means a draw. */
        Blank,
        /** The player that moves first. */
        X,
        /** The player that moves second. */
        O}



    /**
     * Places an X or an O on the specified index depending on whose turn it is.
     * @param index     the position on the board (example: index BOARD_WIDTH is location (0, 1))
     * @return          true if the move has not already been played
     */
    public boolean move (int index);


    /**
     * Tells whether the game has ended, by a win or a full board.
     * @return          true if the game is over; false otherwise
     */
    public boolean isGameOver();


    /**
     * Tells which player moves next.
     * @return          the ID for the current Turn
     */
    public ID getTurn();

    /**
     * Tells who won the game. It can only be asked after the game is over.
     * @return          the player who won (or Blank if the game is a draw)
     */
    public ID getWinner();

    /**
     * Get the indexes of all the positions on the board that are empty.
     * @return          the empty cells
     */
    public HashSet<Integer> getAvailableMoves();


    /**
     * Generates the successors of this state, one for each empty cell. The receiver is not modified.
     * @return the children of the receiver.
    */
    public List<Ilayout> children ();



}
