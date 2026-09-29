package classes;
import interfaces.Ilayout;
import java.util.ArrayList;
import java.util.List;


/**
 * Config HxT of the Ball Sort Puzzle.
 *
 * <p> Each position saves the color of the ball present ({@code 'R'} - Red, {@code 'B'} - Blue, {@code 'G'} - Green, {@code 'E'} - Empty Space)
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

            for (int j = 0; j < this.T; j++){
                if (j == i || sizeTube[j] == H) continue;

                boolean empty = sizeTube[j] == 0;
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
	 * Verifies if the current config is equal to the object's config ({@code Object o})
	 *
	 * @param o Object for comparing
	 * @return {@code true} if {@code o} is a {@code BallSortLayout} with the same config
	 */
    @Override 
    public boolean equals(Object o){
        if (o instanceof BallSortLayout b){

            for(int i = 0; i < this.H; i++){
                for(int j = 0; j < T; j++){
                    if (b.config[i][j] != this.config[i][j]) return false;
                }
            }

            return true;
        }

        return false;
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
	 * Returns toString() hashcode.
	 *
	 * @return Hash of {@code toString()}
     * @see #toString()
	 */
    @Override
	public int hashCode() {
		return toString().hashCode();
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
     * Makes a deep copy of this config.
	 *
	 * @return New {@code BallSortLayout} same as this.
	 */
    @Override
    public BallSortLayout clone() {

		BallSortLayout copy = new BallSortLayout(this.H, this.T);

		for (int i = 0; i < this.H; i++) {
			for (int j = 0; j < this.T; j++) {
				copy.config[i][j] = config[i][j];
                if (config[i][j] != 'E') copy.sizeTube[j] += 1;
			}
		}

		return copy;
	}


}
