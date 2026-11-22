import java.util.*;

/**
 * Monte Carlo Tree Search for a two-player zero-sum game (Problem 3, Tic-Tac-Toe).
 * <p>
 * The algorithm is kept apart from the game, following the Lab 1 pattern: it only talks to the game
 * through {@link Ilayout}. Each call to {@link #chooseMove(Ilayout)} builds a search tree rooted at the
 * given state and repeats four phases: selection, expansion, simulation and backpropagation.
 * <p>
 * MAX is the player to move at the root. Every statistic {@code w} is counted from MAX's point of view
 * (win 1, draw 0.5, loss 0), so MAX nodes maximise the upper confidence bound
 * {@code w/n + c*sqrt(ln(t)/n)} and MIN nodes minimise the lower bound {@code w/n - c*sqrt(ln(t)/n)}.
 * The move returned is the root child that was visited the most.
 * <p>
 * In every search the number of iterations is positive, the exploration constant {@code c} is finite and
 * not negative, and for every node {@code 0 <= w <= n}.
 *
 * @author 85782 - Pedro Salgueiro
 * @author 90247 - Diogo Carvalho
 * @author 84085 - Francisco Melo
 * @version 2026-11-22
 * @see Ilayout#children()
 * @see "Problem 3 - MCTSv3.2c.pdf, Fig. 6.11"
 */
public class MCTS {

    /** A node of the search tree: a game state with the statistics of the simulations that went through it. */
    private static class Node {
        /** The game state of this node. */
        final Ilayout layout;
        /** The parent node, or {@code null} for the root. */
        final Node parent;
        /** The expanded successors; empty while the node is a leaf. */
        List<Node> children = new ArrayList<>();       // empty while the node is a leaf

        /** Whether the player to move in {@code layout} is MAX. */
        final boolean maxToPlay;   // true if layout.getTurn() is the player of the root
        /** The cell played to reach this node (-1 for the root). */
        final int move;           // cell played to get here
        /** Number of simulations that went through this node. */
        int n;                // simulations that went through here
        /** Sum of the results of those simulations, from MAX's point of view (a draw counts 0.5). */
        double w;                // wins of MAX (a draw counts 0.5)

        /**
         * Creates a leaf (no children, no simulations yet).
         *
         * @param layout    the game state of this node
         * @param parent    the parent node, or {@code null} for the root
         * @param maxToPlay whether the player to move in {@code layout} is MAX
         * @param move      the cell played to reach this node, or -1 for the root
         */
        Node(Ilayout layout, Node parent, boolean maxToPlay, int move) {
            this.layout = layout;
            this.parent = parent;
            this.maxToPlay = maxToPlay;
            this.move = move;
        }
    }

    /** Iterations per move. */
    private final int iterations;
    /** Exploration constant. */
    private final double c;
    /** Seed of the random generator of the simulations. */
    private final long seed;
    /** The player to move at the root (MAX). */
    private Ilayout.ID maxPlayer;      // who plays at the root; set once per search

    /**
     * Creates a search with a fixed budget.
     *
     * @param iterations number of iterations (selection + expansion + simulations + backpropagation) per move
     * @param c          exploration constant of the tree policy
     * @param seed       seed of the random generator used by the simulations (same seed, same move)
     * @throws IllegalArgumentException if {@code iterations <= 0} or {@code c} is negative, NaN or infinite
     */
    public MCTS(int iterations, double c, long seed) {
        if (iterations <= 0) throw new IllegalArgumentException("iterations must be positive");
        if (!(c >= 0) || Double.isInfinite(c)) {
            throw new IllegalArgumentException("c must be finite and not negative");   // also rejects NaN
        }
        this.iterations = iterations;
        this.c = c;
        this.seed = seed;
    }

    /**
     * Searches from {@code state} and returns the cell the player to move should play.
     * The state is never modified; the search works on the copies returned by {@code children()}.
     *
     * @param state the current game state
     * @return the cell of the most visited child of the root
     * @throws NullPointerException  if {@code state} is {@code null}
     * @throws IllegalStateException if the game is already over or the state has no free cell
     */
    public int chooseMove(Ilayout state){
        Objects.requireNonNull(state, "state must not be null");
        if (state.isGameOver()) throw new IllegalStateException("Game over: no move to choose.");
        Set<Integer> free = state.getAvailableMoves();
        if (free.isEmpty()) throw new IllegalStateException("Inconsistent state: not over but no free cell.");
        if (free.size() == 1) return free.iterator().next();   // forced move, nothing to search

        Random random = new Random(seed);
        maxPlayer = state.getTurn();

        Node root = new Node(state, null, true, -1);
        root.n = 1;
        root.w = scoreMax(simulation(state, random));

        for (int it = 0; it < iterations; it++){
            Node leaf = selectMove(root);
            if (leaf.layout.isGameOver()){
                backPropagation(leaf, scoreMax(leaf.layout));
            } else {
                expandLeaf(leaf);
                for (Node child : leaf.children){
                    double score = scoreMax(simulation(child.layout, random));
                    backPropagation(child, score);
                }
            }
        }
        return mostVisitedNode(root).move;
    }

    /* 0. Score to actual Value */

    /**
     * Converts the result of a finished game into a value from MAX's point of view.
     *
     * @param gameFinished a terminal state
     * @return 1 if MAX won, 0.5 for a draw, 0 if MAX lost
     */
    private double scoreMax(Ilayout gameFinished) {
        Ilayout.ID winner = gameFinished.getWinner();
        if (winner == maxPlayer) return 1.0;                // MAX won
        if (winner == Ilayout.ID.Blank) return 0.5;         // draw
        return 0.0;                                         // MAX lost
    }

    /* 1. Selection: */

    /**
     * Selection: descends from {@code node} choosing a child with {@link #bestChild(Node)} until a leaf.
     *
     * @param node where the descent starts (the root)
     * @return the leaf that was reached
     */
    private Node selectMove(Node node){
        while (!node.children.isEmpty()){
            node = bestChild(node);
        }
        return node;
    }

    /**
     * Tree policy: picks the child of {@code parent} to follow. A child that was never visited goes
     * first. Otherwise a MAX parent maximises {@code w/n + c*sqrt(ln(t)/n)} and a MIN parent minimises
     * {@code w/n - c*sqrt(ln(t)/n)}, which is done by maximising {@code -w/n + c*sqrt(ln(t)/n)}.
     * Ties keep the first child.
     *
     * @param parent a node that already has children
     * @return the chosen child
     */
    Node bestChild(Node parent) {
        for (Node child : parent.children) {
            if (child.n == 0) return child;                 // unvisited first (assignment rule)
        }

        double sign = parent.maxToPlay ? 1.0 : -1.0;        // MAX: +merit, MIN: -merit
        double logT = Math.log(parent.n);

        Node best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (Node child : parent.children) {
            double merit = child.w / child.n;
            double uncertainty = c * Math.sqrt(logT / child.n);
            double confBound = sign * merit + uncertainty;  // merit - bonus < current_value <=>
                                                            // <=> -merit + bonus > current_value
                if (confBound > bestValue) {
                best = child;
                bestValue = confBound;
            }
        }
        return best;
    }

    /* 2. Expansion: */

    /**
     * Finds the cell played to go from {@code parent} to {@code child}: the only free cell of the
     * parent that is no longer free in the child ({@link Ilayout} is not extended for this).
     *
     * @param parent a state
     * @param child  one of the successors of {@code parent}
     * @return the cell that was played
     */
    private int movePlayed(Ilayout parent, Ilayout child) {
        Set<Integer> played = parent.getAvailableMoves();
        played.removeAll(child.getAvailableMoves());
        return played.iterator().next();
    }

    /**
     * Expansion: creates one child for every successor of the leaf, in the order of {@code children()}.
     *
     * @param leaf a non-terminal node without children
     */
    private void expandLeaf(Node leaf){

        for (Ilayout successor : leaf.layout.children()){
            boolean playingMax = (successor.getTurn() == maxPlayer);
            int slot = movePlayed(leaf.layout, successor);
            leaf.children.add(new Node(successor, leaf, playingMax, slot));
        }
    }

    /* 3. Simulation: */

    /**
     * Simulation (rollout): plays random legal moves from {@code start} until the game ends.
     * {@code start} is not modified.
     *
     * @param start  the state where the rollout begins
     * @param random source of the random choices
     * @return the terminal state reached ({@code start} itself if it is already terminal)
     */
    private Ilayout simulation(Ilayout start, Random random){
        Ilayout current = start;
        while (!current.isGameOver()){
            List<Ilayout> sucs = current.children();
            current = sucs.get(random.nextInt(sucs.size()));
        }
        
        return current;
    }

    /* 4. Backpropagation: */

    /**
     * Backpropagation: adds one visit and {@code result} to every node from {@code node} up to the root.
     * The result is always from MAX's point of view, so it is not inverted at MIN levels.
     *
     * @param node   where the simulation (or terminal evaluation) happened
     * @param result value from MAX's point of view, see {@link #scoreMax(Ilayout)}
     */
    private void backPropagation(Node node, double result){
        while (node != null){
            node.n++;
            node.w = node.w + result;
            node = node.parent;
        }
    }

    /* 5. Move Decision: */

    /**
     * Move decision: the child of the root with the most visits. On a tie the higher {@code w} wins
     * (same {@code n}, so a higher win rate), and then the first child.
     *
     * @param root a root that has children
     * @return the chosen child
     */
    private Node mostVisitedNode(Node root){
        Node bestNode = root.children.get(0);
        for (Node child: root.children){
            // most visits; on a tie, higher w/n (same n, so higher w); then the first generated
            if (child.n > bestNode.n || (child.n == bestNode.n && child.w > bestNode.w)) bestNode = child;
        }
        return bestNode;
    }
    
}
