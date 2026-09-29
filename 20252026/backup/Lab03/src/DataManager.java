import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Manager responsible to read and store the input values in the neurons and expected Y values produced by them.
 *
 * <p>
 *  <h3>Format:</h3>
 *  <li>{@code {(x1_0, x2_0, y_0), ... ,(x1_n, x2_n, y_n)}}</li>
 * </p>
 *
 * @author Francisco Melo, a84085
 * @version 08-11-2025
 */
public class DataManager {
    private int size = 0;
    private BufferedReader bf;
    private ArrayList<Integer> values;

    /**
     * Constructor of DataManager.
     *
     * @param path {@code String} path for dataSet file.
     */
    public DataManager(String path) {
        try {
            this.bf = new BufferedReader(new FileReader(path));
            loadValues();
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Loads values from file in given path
     *
     * @throws IOException
     */
    private void loadValues() throws IOException{
        values = new ArrayList<>();
        String line = bf.readLine();
        line = line.replace(" ", "").trim();
        char[] chars = line.toCharArray();

        for (char aChar : chars) {
            try{
                values.add(Integer.parseInt(String.valueOf(aChar)));
            } catch (Exception _){}
        }

        size = values.size()/3;
    }

    /**
     * Getter for all values on dataSet.
     * @return values {@code ArrayList<Integer>}
     */
    public ArrayList<Integer> getValues(){
        return values;
    }

    /**
     * Getter for a subSet of values in dataSet including predetermined x0 value.
     *
     * <h3>Each subSet corresponds to every 3 values in dataSet:</h3>
     * <li>{@code [x1, x2, y]}</li>
     *
     * <h3>The return has the format: </h3>
     * <li>{@code [x0, x1, x2}]</li>
     *
     * @param i {@code int} index of desired subSet
     * @param x0 {@code int} value for predetermined input value
     * @return {@code int[]} {@code [x0, x1, x2]}
     * @throws IndexOutOfBoundsException if subSet index is bigger than subSets.length
     */
    public int[] getValueSet(int i, int x0) throws IndexOutOfBoundsException{
        if (i >= size) throw new IndexOutOfBoundsException("GetValueSet int i >= size_values");
        ArrayList<Integer> valueSet = new ArrayList<>();

        valueSet.add(x0);
        valueSet.add(values.get(i*3));
        valueSet.add(values.get(i*3 + 1));

        return valueSet.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Returns expected value y for specific subSet.
     * @param i {@code int} index of subSet
     * @return {@code int} value y.
     * @throws IndexOutOfBoundsException if subSet index is bigger than subSets.length
     */
    public int expectedResult(int i) throws IndexOutOfBoundsException{
        if (i >= size) throw new IndexOutOfBoundsException("ExpectedResult int i >= size_values");
        return values.get(i*3 + 2);
    }
}
