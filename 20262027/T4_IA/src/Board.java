import java.util.*;

/**
 * A Tic-Tac-Toe board: an {@code N x M} grid where X moves first and the first player to complete
 * {@code K} consecutive cells in a row, column or diagonal wins. A cell is identified by its index,
 * which starts at 0 and grows from left to right and from top to bottom ({@code index = y * M + x}).
 * <p>
 * The grid, the move counter, the set of free cells, the winner and the end of the game always agree:
 * the counter is the number of non-blank cells, the free cells are exactly the blank ones, and the
 * game is over only when a player has a line or the board is full.
 *
 * @author 85782 - Pedro Salgueiro
 * @author 90247 - Diogo Carvalho
 * @author 84085 - Francisco Melo
 * @version 2026-11-22
 * @see Ilayout
 */
public class Board implements Ilayout,Cloneable{
    /** The cells of the board, indexed {@code [row][column]}. */
    private ID[][] board;
    /** The player that moves next. */
    private ID playersTurn;
    /** The winner once the game is over: X, O, or Blank for a draw. */
    private ID winner;
    /** The indexes of the blank cells. */
    private HashSet<Integer> movesAvailable;

    /** Number of cells already played. */
    private int moveCount;
    /** Whether the game has ended. */
    private boolean gameOver;


    /**
     * Creates an empty board where X moves first.
     */
    Board() {
        board = new ID[N][M];
        movesAvailable = new HashSet<>();
        reset();
    }

    /**
     * Set the cells to be blank and load the available moves (all the moves are
     * available at the start of the game).
     */
    private void initialize () {
        for (int row = 0; row < N; row++)
            for (int col = 0; col < M; col++) {
                board[row][col] = ID.Blank;
            }
        movesAvailable.clear();

        for (int i = 0; i < N*M; i++) {
            movesAvailable.add(i);
        }
    }

    /**
     * Restart the game with a new blank board.
     */
    public void reset() {
        moveCount = 0;
        gameOver = false;
        playersTurn = ID.X;
        winner = ID.Blank;
        initialize();
    }

    /**
     * Places an X or an O on the specified index depending on whose turn it is.
     * @param index     position starts in 0 and increases from left to right and from top to bottom
     * @return          true if the move has not already been played
     * @throws IllegalArgumentException if {@code index} is not between 0 and {@code N*M-1}
     * @throws IllegalStateException    if the game is already over
     */
    public boolean move (int index) {
        if (index < 0 || index >= N * M) {
            throw new IllegalArgumentException("index must be between 0 and " + (N * M - 1) + ": " + index);
        }
        return move(index% M, index/M);
    }

    /**
     * Places an X or an O on the specified location depending on who turn it is.
     * @param x the x coordinate of the location
     * @param y the y coordinate of the location
     * @return  true if the move has not already been played
     * @throws IllegalArgumentException if {@code x} is not between 0 and {@code M-1} or {@code y} is not
     *                                  between 0 and {@code N-1}
     * @throws IllegalStateException    if the game is already over
     */
   public boolean move (int x, int y) {

        if (x < 0 || x >= M || y < 0 || y >= N) {
            throw new IllegalArgumentException("cell out of the board: x=" + x + ", y=" + y);
        }

        if (gameOver) {
            throw new IllegalStateException("Game over. No more moves can be played.");
        }

        if (board[y][x] == ID.Blank) {
            board[y][x] = playersTurn;
        } else {
            return false;
        }

        moveCount++;
        movesAvailable.remove(y * M + x);

        // The game is a draw.
        if (moveCount == N * M) {
            winner = ID.Blank;
            gameOver = true;
        }

        // Check for a winner.
        if (checkLine(x, y)){
            winner = playersTurn;
            gameOver = true;
        }
        
        playersTurn = (playersTurn == ID.X) ? ID.O : ID.X;
        return true;
    }

    /**
     * Checks whether the piece just placed at {@code (x, y)} completes a line of {@code K} pieces.
     * Only the four lines through that cell (row, column and both diagonals) can have been completed,
     * so only those are examined, each one in both directions.
     *
     * @param x the column of the cell just played
     * @param y the row of the cell just played
     * @return true if the player who just moved has a line
     */
    private boolean checkLine(int x, int y){
        final int[][] VECTORS = {{1, 0},{0, 1},{1, 1},{1, -1}};
        ID player = board[y][x];
        int sum;

        for (int[] vector : VECTORS){
            sum = 1 +   countLine(x, y, vector[0], vector[1], player) +
                        countLine(x, y, -vector[0], -vector[1], player);
            if (sum >= K) 
                return true;
        }

        return false;
    }

    /**
     * Counts the consecutive cells of {@code player} found by walking from {@code (x, y)}, without
     * counting that cell, in the direction {@code (dx, dy)} until the board ends or another cell appears.
     *
     * @param x      the column where the walk starts
     * @param y      the row where the walk starts
     * @param dx     the step in columns
     * @param dy     the step in rows
     * @param player the player whose cells are counted
     * @return the number of consecutive cells of {@code player}
     */
    private int countLine(int x, int y, int dx, int dy, ID player){
        int counter = 0;
        int currentX = x + dx, currentY = y + dy;

        while (0 <= currentX && currentX < M && 0 <= currentY && currentY < N){
            if (board[currentY][currentX] != player) break;
            counter++;
            currentX += dx; currentY += dy;
        }

        return counter;
    }

    /**
     * Check to see if the game is over (if there is a winner or a draw).
     * @return true if the game is over
     */
    public boolean isGameOver () {
        return gameOver;
    }

    /**
     * Check to see who's turn it is.
     * @return the player who's turn it is
     */
    public ID getTurn () {
        return playersTurn;
    }

    /**
     * Tells who won the game.
     * @return the player who won (or Blank if the game is a draw)
     * @throws IllegalStateException if the game is not over yet
     */
    public ID getWinner () {
        if (!gameOver) {
            throw new IllegalStateException("Not over yet!");
        }
        return winner;
    }

    /**
     * Get the indexes of all the positions on the board that are empty.
     * @return          the empty cells
     */
    public HashSet<Integer> getAvailableMoves () {
        return new HashSet<>(movesAvailable);
    }



    /**
     * Copies the board; the copy shares no cell and no set of free cells with the original.
     * @return  an deep copy of the board
     */
    public Object clone () {
    	try {
	        Board b = (Board) super.clone();

	        b.board = new ID[N][M];
	        for (int i=0; i<N; i++)
	        	for (int j=0; j<M; j++)
	        		b.board[i][j] = this.board[i][j];

	        b.playersTurn       = this.playersTurn;
	        b.winner            = this.winner;
	        b.movesAvailable    = new HashSet<Integer>();
	        b.movesAvailable.addAll(this.movesAvailable);
	        b.moveCount         = this.moveCount;
	        b.gameOver          = this.gameOver;
	        return b;
    	}
    	catch (CloneNotSupportedException e) {
    		throw new InternalError(e);
    	}
    }

    /**
     * Draws the board with one line per row: {@code -} for a blank cell, X or O otherwise.
     * @return the board as text
     */
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < N; y++) {
            for (int x = 0; x < M; x++) {
                if (board[y][x] == ID.Blank) {
                    sb.append("-");
                } else {
                    sb.append(board[y][x].name());
                }
                sb.append(" ");
            }
            if (y != N -1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Generates the successors of this state: one independent copy for each free cell, with that cell
     * played, in ascending order of the cell. The receiver is not modified.
     *
     * @return the children of the receiver (empty if the game is over)
     */
    public List<Ilayout> children() {
        List<Ilayout> children = new ArrayList<>();
        if (gameOver) return children;

        List<Integer> sortedAvai = new ArrayList<>(movesAvailable);
        Collections.sort(sortedAvai);

        for (int move : sortedAvai){
            Board child = (Board) clone();
            child.move(move);
            children.add(child);
        }

        return children;
    }


	/**
	 * Two boards are equal when all their cells are equal.
	 * @param other the object to compare with
	 * @return true if {@code other} is a board with the same cells
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		Board that = (Board) other;

		for (int x=0; x<N; x++)
            for (int y=0; y<M; y++)
            	if (board[x][y] != that.board[x][y]) return false;
        return true;
	}

	/**
	 * Hash code consistent with {@link #equals(Object)}: it depends only on the cells.
	 * @return the hash code of the cells
	 */
	@Override
	public int hashCode() {
		return Arrays.deepHashCode(board);
	}

	/**
	 * Tells whether a cell is empty.
	 * @param index the position of the cell, from 0 to {@code N*M-1}
	 * @return true if nothing was played in the cell
	 * @throws IllegalArgumentException if {@code index} is not between 0 and {@code N*M-1}
	 */
	public boolean isBlank (int index) {
		if (index < 0 || index >= N * M) {
			throw new IllegalArgumentException("index must be between 0 and " + (N * M - 1) + ": " + index);
		}
		int row = index / M;
		int column = index % M;
        return (board[row][column] == ID.Blank);
	}
}
