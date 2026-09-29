import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a config based on a vector {@code int[]} for search, implements {@link Ilayout}
 *
 * <p>
 * Each instance stores values in ({@code int[]}) and allows generation of children-nodes through swapping of two values.
 * Each instance has a cost value for search algorithms.
 * </p>
 *
 * <h2>Generation of children and cost {@code k}</h2>
 * <p>
 * The method {@link #children()} generates all possible combinations by switching
 * two elements in the array (pairs {@code (i, j)} with {@code i < j}).
 * For each child, the cost {@code k} is defined by the parity of the elements in question:
 * </p>
 * <ul>
 *   <li>Both even (even-even) => {@code k = 2}</li>
 *   <li>Both odd (odd-odd) => {@code k = 20}</li>
 *   <li>Different parities (even-odd) => {@code k = 11}</li>
 * </ul>
 *
 * @author Francisco Melo, a84085
 * @version 13-10-2025
 */
public class ArrayCfg implements Ilayout, Cloneable{
    private static int size = 3;
    private final int[] data;
    private double k = 0;

    /**
     * Constructor by omission: Creates array with {@link #size} (global).
     */
    public ArrayCfg(){ data = new int[size]; }

    /**
     * Deep copy of ({@link ArrayCfg}){@code other}.
     *
     * @param other {@code ArrayCfg}
     */
    public ArrayCfg(ArrayCfg other) {
        this.data = other.data.clone();
        size = this.data.length;
        this.k = other.k;
    }


    /**
     * Constructor from string.
     *
     * <p>Input example: {@code "1 4 7 2"}</p>
     *
     * <p>
     * This constructor redefines global {@link #size} for all existing
     * instances and initializes {@link #data} with the values provided.
     * </p>
     *
     * @param input {@code String} with spaces between numbers.
     * @throws IllegalStateException if {@code size < 3}.
     * @throws NumberFormatException if any {@code Character} is not a valid {@code Integer}.
     */
    public ArrayCfg(String input) throws IllegalStateException, NumberFormatException{
        String [] arr_input = input.split(" "); //Split string in whitespaces
        size = arr_input.length;
        data = new int[size];

        if (size < 3) throw new
            IllegalStateException("Invalid arg in Node constructor");

        for (int i = 0; i < size; i++ )
            data[i] = Integer.parseInt(arr_input[i]);
    }

    /**
     * Constructor from an array of integers.
     *
     * <p>
     * This constructor redefines global {@link #size} for all existing
     * instances and initializes {@link #data} with the values provided
     * in input {@code data}.
     * </p>
     *
     * @param data {@code int[]} data that defines a configuration.
     * @throws IllegalStateException if {@code size < 3}.
     */

    public ArrayCfg(int[] data){
        size = data.length;
        if (size < 3) throw new
            IllegalStateException("Invalid arg in Node constructor");

        this.data = data.clone();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for(int i=0; i<size; i++){
            str.append(data[i]).append(" ");
        }

        return str.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayCfg other)) return false;

        for (int i = 0; i < size; i++) {
            if (!(this.data[i] == other.data[i])) return false;
        }

        return true;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Swaps (in-place) the elements at the indicated positions in the provided array.
     *
     * @param current The array to be modified.
     * @param positions The pair of indices {@code {i, j}} to swap.
     * @return the {@code current} array itself, after the swap.
     * @throws ArrayIndexOutOfBoundsException if any index is out of bounds.
     * @throws NullPointerException if either {@code current} or {@code positions} is {@code null}.
     **/
    public int[] numberSwapper(int[] current, int[] positions) throws ArrayIndexOutOfBoundsException, NullPointerException{
        int bckp = current[positions[0]];

        current[positions[0]] = current[positions[1]];
        current[positions[1]] = bckp;

        return current;
    }

    @Override
    public List<Ilayout> children() {
        List<Ilayout> children = new ArrayList<>();

        for(int i = 0; i < size; i++){
            for (int j = i+1; j < size; j++){
                ArrayCfg child_node = new ArrayCfg(numberSwapper(this.data.clone(), new int[]{i, j}));

                boolean e_i = (this.data[i] % 2 == 0);
                boolean e_j = (this.data[j] % 2 == 0);

                if (e_i == e_j) {
                    child_node.setK(e_i ? 2 : 20);
                } else {
                    child_node.setK(11);
                }

                children.add(child_node);
            }
        }

        return children;
    }

    @Override
    public boolean isGoal(Ilayout l) {
        return this.equals(l);
    }

    public void setK(double k){
        this.k = k;
    }

    @Override
    public double getK() {
        return k;
    }


    @Override
    public ArrayCfg clone() {
        return new ArrayCfg(this);
    }

}