/**
 * Agent that chooses its move with Monte Carlo Tree Search.
 * <p>
 * It has the same interface as {@link XAgent}: it receives an {@link Ilayout} and returns the cell
 * (0 to {@code N*M-1}) where it wants to play, without modifying the layout it receives. All the
 * search lives in {@link MCTS}; this class only fixes the parameters of the search.
 * <p>
 * Java does not allow a static and an instance method with the same signature, so the static
 * {@link #play(Ilayout)} (same name and signature as {@link XAgent#play(Ilayout)}, for {@link Console})
 * uses the default parameters, and the instance method {@link #choose(Ilayout)} uses the parameters
 * given to the constructor (for the tests and the experiments).
 */
public class MCTSAgent {

    /** Number of MCTS iterations per move used by {@link #play(Ilayout)}. */
    public static final int ITERATIONS = 2000;

    /** Exploration constant used by {@link #play(Ilayout)}. */
    public static final double C = 0.5;

    /** Iterations per move of {@link #choose(Ilayout)}. */
    private final int iterations;
    /** Exploration constant of {@link #choose(Ilayout)}. */
    private final double c;
    /** Seed of {@link #choose(Ilayout)}. */
    private final long seed;

    /**
     * Creates an agent with explicit search parameters. A fixed seed makes its moves reproducible.
     *
     * @param iterations number of MCTS iterations per move, must be positive
     * @param c          exploration constant of the tree policy, must not be negative
     * @param seed       seed of the random generator used by the simulations
     * @throws IllegalArgumentException if {@code iterations <= 0} or {@code c} is negative
     */
    public MCTSAgent(int iterations, double c, long seed) {
        new MCTS(iterations, c, seed);          // validates the parameters now, not on the first move
        this.iterations = iterations;
        this.c = c;
        this.seed = seed;
    }

    /**
     * Chooses the move of the player to move with the parameters given to the constructor.
     * The same agent and state always give the same move.
     *
     * @param board the current state (not modified)
     * @return the cell to play
     * @throws IllegalStateException if the game is already over
     */
    public int choose(Ilayout board) {
        return new MCTS(iterations, c, seed).chooseMove(board);
    }

    /**
     * Chooses the move with the default parameters ({@link #ITERATIONS}, {@link #C}) and a new random
     * seed, so two games are not identical. Same signature as {@link XAgent#play(Ilayout)}, so it can
     * replace it in {@link Console}.
     *
     * @param board the current state (not modified)
     * @return the cell to play
     * @throws IllegalStateException if the game is already over
     */
    static int play(Ilayout board) {
        return new MCTS(ITERATIONS, C, System.nanoTime()).chooseMove(board);
    }
}
