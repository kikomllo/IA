package classes;
import interfaces.Ilayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Config HxT of the Ball Sort Puzzle.
 *
 * <p> Each position saves the color of the ball present ({@code 'R'} - Red, {@code 'B'} - Blue, {@code 'P'} - Purple, {@code 'Y'} - Yellow, {@code 'G'} - Green, {@code 'E'} - Empty Space)
 * 
 * <p> Implements {@link Ilayout} and is resolved by {@link BestFirst}.
 *
 * @see Ilayout
 * @see BestFirst
 */
public class BallSortLayout implements Ilayout, Cloneable{

    private final char config[][];
    private final int sizeTube[];
    private final int H;
    private final int T;

    // Both are computed the first time they are needed, and not in the constructor: children()
    // creates a clone and only then moves the ball, so the board is not final when it is built.
    private String key;         // Tubes written as text and sorted: same key = same layout, whatever the tube order
    private int hCache = -1;    // Value of the heuristic, -1 while not computed

    /**
     * Creates the shell of BallSortLayout and initializes {@code config} and {@code sizeTube}
     * 
     * @param H - Height of all tubes
     * @param T - Number of tubes
     */
    public BallSortLayout(int H, int T){
        this.H = H;
        this.T = T;
        config = new char[H][T];
        sizeTube = new int[T];
    }

    /**
     * Creates configured BallSortLayout from a string, line by line.
	 *
	 * <p>Example, {@code new BallSortLayout("E E B E\nE R B E\nR R B E\n")} configs to this:
	 * <pre>
	 * E E B E
     * E R B E
     * R R B E
	 * </pre>

     * No valid config testing.
	 *
	 * @param str string with {@code H} lines and {@code T} columns.
	 */
    public BallSortLayout(String string){
        String rows[]  = string.split("\n");
        this.H = rows.length;
        this.T = rows[0].split(" ").length;

        config = new char[this.H][this.T];
        sizeTube = new int[this.T];

        for (int i = 0; i < this.H; i++){
            String tubos[] = rows[i].split(" ");
            for (int j = 0; j < this.T; j++) {
                config[i][j] = tubos[j].charAt(0);
                if (config[i][j] != 'E') sizeTube[j] += 1;
            }
        }
    }

    /**
     * Creates children by making all possible valid moves (1 move only) from the current state.
	 * <p> Each children is a {@code clone()} of the current state with one move done.
	 * <p> Only the first empty tube is used as destination: all empty tubes are alike, so moving
	 * to another one would give the same layout with two tubes swapped.
	 * @return {@code List<Ilayout>} - All the children
	 * @see #clone()
	 */
    @Override
    public List<Ilayout> children(){
        List<Ilayout> result = new ArrayList<>();

        for (int i = 0; i < this.T; i++){
            if (sizeTube[i] == 0) continue;

            int realIndex = H - sizeTube[i];
            char color = config[realIndex][i];

            boolean triedEmpty = false;
            for (int j = 0; j < this.T; j++){
                if (j == i || sizeTube[j] == H) continue;

                boolean empty = sizeTube[j] == 0;

                // Skip the 2nd, 3rd... empty tube
                if (empty){
                    if (triedEmpty) continue;
                    triedEmpty = true;
                }
                int colorIndex = H - sizeTube[j];
                char tubeTop = empty ? 'E' : config[colorIndex][j];

                if (empty || tubeTop == color){
                    int emptyIndex = colorIndex - 1;

                    BallSortLayout clone = this.clone();

                    clone.config[emptyIndex][j] = color;
                    clone.config[realIndex][i] = 'E';
                    clone.sizeTube[i]--;
                    clone.sizeTube[j]++;
                    result.add(clone);
                }
            }
        }

        return result;
    }

    /**
	 * Verifies if the current config is equal to the object's config ({@code Object o}),
	 * ignoring the order of the tubes.
	 *
	 * <p>Two boards with the same tubes in a different order are the same puzzle (same cost to solve),
	 * so the search must see them as one state. 
     * <p>This shrinks the search up to {@code T!} times.
	 *
	 * @param o Object for comparing
	 * @return {@code true} if {@code o} is a {@code BallSortLayout} with the same tubes, in any order
	 * @see #key()
	 */
    @Override 
    public boolean equals(Object o){
        if (o instanceof BallSortLayout b){
            return this.key().equals(b.key());
        }

        return false;
    }

    /**
	 * Builds the key of this layout (only the first time, then it is saved).
	 *
	 * <p>Each tube is written as text, from the bottom to the top and without the {@code 'E'}s,
	 * then the tubes are sorted and joined with {@code ','}.
	 * Example: tubes {@code "RB"}, {@code "B"} and an empty one give the key {@code ",B,RB"}
	 * (the same key for any tube order).
	 *
	 * @return {@code String}: the key
	 */
    private String key(){
        if (key == null){
            String tubes[] = new String[T];
            for (int j = 0; j < T; j++){
                StringBuilder tube = new StringBuilder();
                for (int i = H - 1; i >= H - sizeTube[j]; i--){
                    tube.append(config[i][j]);
                }
                tubes[j] = tube.toString();
            }
            Arrays.sort(tubes);
            key = String.join(",", tubes);
        }
        return key;
    }

    /**
	 * Parse config into text: H lines and T columns with corresponding color characters
     * 
     * <p>Example:
	 * <pre>
	 * E E B E
     * E R B E
     * R R B E
	 * </pre>
     * 
	 * @return {@code String}: Layout in text format, with {@code '\n'} at the end of each line.
	 */
    @Override
    public String toString(){
        StringBuilder strBuilder = new StringBuilder();

        for(int i = 0; i < this.H; i++){
            for(int j = 0; j < T; j++){
                strBuilder.append(config[i][j]);
                if (j == T-1) strBuilder.append('\n');
                else strBuilder.append(' ');
            }
        }

        return strBuilder.toString();
    }

    /**
	 * Returns the key's hashcode (same key = same hash, as {@link #equals(Object)} needs).
	 *
	 * @return Hash of {@code key()}
	 * @see #key()
	 */
    @Override
	public int hashCode() {
		return key().hashCode();
	}


    /**
	 * Checks if the current config is a valid Goal (All tubes empty or full with the same color)
	 *
	 * @param l - Expecting {@code null}
	 * @return {@code true} if current {@code config} is a valid Goal
	 */
    @Override
    public boolean isGoal(Ilayout l){
        if (l != null) return false;

        char color;
        int colorIndex;
        for (int i = 0; i < T; i++){
            if (sizeTube[i] != 0){
                if (sizeTube[i] != H) return false;

                colorIndex = H - sizeTube[i];
                color = config[colorIndex][i];
                for (int j = colorIndex; j < H; j++){
                    if (config[j][i] != color) return false;
                }
            }
        }

        return true;
    }

    /**
	 * Returns the cost of one move
	 *
	 * @return {@code 1}, all moves cost the same.
	 */
    @Override 
    public double getK(){
        return 1;
    }

    /**
	 * Heuristic: estimates the number of moves left, never more than the real number.
	 *
     * 
	 * <p>{@code h = (number of balls) - (sum, for each color, of its biggest base)}.
	 * <p>The base of a tube is the group of balls of the same color at its bottom.
	 * <p>Only these balls can stay where they are (a ball can only stay if the ones below it stay),
	 * and, since in the goal each color is in one tube, only one base per color can stay.
	 * Every other ball moves at least once.
	 *
	 * <p>Is computed only the first time, then it is saved.
	 *
	 * @param goal - Not used (the goal is "all tubes empty or full with the same color")
	 * @return {@code double}: the estimate
	 */
    @Override
    public double getH(Ilayout goal){
        if (hCache < 0){
            int[] maxBase = new int[Color.values().length];  // Largest base of each color
            int balls = 0;

            for (int j = 0; j < T; j++){
                int n = sizeTube[j];
                if (n == 0) continue;
                balls += n;

                char color = config[H - 1][j];                              // Bottom Color ball
                int numSameColor = 0;
                for (int i = H - 1; i >= H - n && config[i][j] == color; i--) 
                    numSameColor++;

                int c = Color.valueOf(String.valueOf(color)).ordinal();     // Check Color enum
                if (numSameColor > maxBase[c]) maxBase[c] = numSameColor;
            }

            int stay = 0;                                                   // Balls that may never move
            for (int c = 0; c < maxBase.length; c++) stay += maxBase[c];

            hCache = balls - stay;
        }
        return hCache;
    }

    /**
     * Makes a deep copy of this config.
	 *
	 * <p>The saved {@code key} and heuristic are not copied: the copy is going to be changed.
	 *
	 * @return New {@code BallSortLayout} same as this.
	 */
    @Override
    public BallSortLayout clone() {

		BallSortLayout copy = new BallSortLayout(this.H, this.T);

		for (int i = 0; i < this.H; i++) {
			System.arraycopy(config[i], 0, copy.config[i], 0, this.T);
		}
		System.arraycopy(sizeTube, 0, copy.sizeTube, 0, this.T);

		return copy;
	}


}
