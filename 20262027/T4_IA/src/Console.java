import java.util.Scanner;


/**
 * For playing in the console: a human plays X and the {@link MCTSAgent} plays O.
 * <p>
 * The console always holds a board and a reader of the keyboard (or of another input, for the tests),
 * and a human move is only accepted when it is a free cell of the board.
 *
 * @author 85782 - Pedro Salgueiro
 * @author 90247 - Diogo Carvalho
 * @author 84085 - Francisco Melo
 * @version 2026-11-22
 * @see Board
 * @see MCTSAgent
 */
public class Console {
    /** The game being played. */
    private Board board;
    /** Where the moves of the human are read from. */
    private final Scanner sc;

    /**
     * Creates a game that reads the moves of the human from the keyboard.
     */
    public Console() {
        this(new Scanner(System.in));
    }

    /**
     * Creates a game that reads the moves of the human from the given input.
     *
     * @param sc the reader of the moves of the human
     */
    Console(Scanner sc) {
        this.board = new Board();
        this.sc = sc;
    }

    /**
     * Game on
     */
    private void play () {
        while (true) {
            playMove();
            if (board.isGameOver()) {
                printWinner();
                break;
            }
        }
    }


    /**
     * Handle the move to be played: the human plays X and the MCTS agent plays O.
     */

    private void playMove () {
    	int position;

        if (board.getTurn() == Ilayout.ID.X) {
        	position=getHumanMove();
        	//position = XAgent.play(board);
        	board.move(position);

        } else {
        	position = MCTSAgent.play(board);
        	board.move(position);
        }
    }

    /**
     * Shows the board and whose turn it is.
     */
    private void printGameStatus () {
       IO.println("\n" + board + "\n");
       IO.println(board.getTurn().name() + "'s turn.");
    }

    /**
     * For reading in and interpreting the move that the user types into the console. It asks again
     * until the user types the index of a free cell; it does not play the move.
     *
     * @return the index of a free cell of the board
     * @throws IllegalStateException if the input ends before a valid move is read
     */
    int getHumanMove() {
        printGameStatus ();

        while (true) {
            System.out.print("Index of move: ");

            if (!sc.hasNextLine()) {
                throw new IllegalStateException("The input ended before a valid move was read.");
            }

            int move;
            try {
                move = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                IO.println("\nInvalid move.");
                IO.println("\nThe index of the move must be an integer.");
                continue;
            }

            if (move < 0 || move >= Ilayout.N* Ilayout.M) {
               IO.println("\nInvalid move.");
               IO.println("\nThe index of the move must be between 0 and "
                        + (Ilayout.N * Ilayout.M - 1) + ", inclusive.");
            } else if (!board.isBlank(move)) {
               IO.println("\nInvalid move.");
               IO.println("\nThe selected index must be blank.");
            } else {
                return move;
            }
        }
    }


    /**
     * Shows the final board and the result of the game.
     */
    private void printWinner () {
        Ilayout.ID winner = board.getWinner();

        IO.println("\n" + board + "\n");

        if (winner == Ilayout.ID.Blank) {
            IO.println("It's a draw.");
        } else {
            IO.println("Player " + winner.toString() + " wins!");
        }
    }



    /**
     * Plays three games against the MCTS agent and shows the average time of a game.
     */
     void main()  {
        final int repetitions=3;
        long times = 0;
        for(int i=0; i<repetitions; i++) {
            Console game = new Console();
            long startTime = System.currentTimeMillis();
            game.play();
            long totalTime = System.currentTimeMillis() - startTime;
            times += totalTime;
        }
        IO.println("Av Time: " + times*1.0f/repetitions+ " milisecs");
    }

}
