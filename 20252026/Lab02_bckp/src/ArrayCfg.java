import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayCfg implements Ilayout, Cloneable{
    private static int size = 3;
    private final int[] data;
    private double k = 0;

    public ArrayCfg(){ data = new int[size]; }

    public ArrayCfg(String input) throws IllegalStateException{
        String [] arr_input = input.split(" "); //Split string in whitespaces
        size = arr_input.length;
        data = new int[size];

        if (size < 3) throw new
            IllegalStateException("Invalid arg in Node constructor");

        for (int i = 0; i < size; i++ )
            data[i] = Integer.parseInt(arr_input[i]);
    }

    public ArrayCfg(int[] data){
        size = data.length;
        if (size < 3) throw new
            IllegalStateException("Invalid arg in Node constructor");

        this.data = data;
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

    public int[] numberSwapper(int[] current, int[] positions){
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
        try {
            ArrayCfg clone = (ArrayCfg) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}