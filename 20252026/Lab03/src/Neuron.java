/**
 * Class responsible for the implementation of a Neuron, the implementation requires input values and weights so the neuron works.
 *
 * <p>
 *  <h3>gFunction: </h3>
 *  <li>Sigmoid (hardcoded): {@code 1 / (1 + exp(-z))}</li>
 * </p>
 *
 * @author Francisco Melo, a84085
 * @author Gabriel Vaz, a75571
 * @author Maria Santos, a71295
 * @version 09-11-2025
 */
public class Neuron {
    private int[] values;
    private double[] weights;
    private int size = 0;

    /**
     * Constructor of Neuron.
     *
     * @param values {@code int[]} input values.
     * @param weights {@code double[]} weights for values.
     * @throws Exception if {@code values.length != weights.length}.
     */
    public Neuron(int[] values, double[] weights) throws Exception {
        if (values.length != weights.length)
                throw new Exception("Different values/weights size: v.size - " + values.length + ", w.size - " + weights.length);

        this.size = values.length;
        this.values = values;
        this.weights = weights;
    }

    /**
     * Method responsible for calc of {@code z}.
     * {@code z} is the sum of all {@code values * weights}.
     *
     * @return {@code z}
     */
    public double calcZ() {
        double z = 0;

        z += 1 * weights[0];

        for (int i = 1; i < size; i++) {
            z += (double) (values[i]) * weights[i];
        }

        return z;
    }

    /**
     * Method responsible for processing the values between 1 and 0.
     *
     *
     * <h3>Current hardcoded function:</h3>
     * <li>Sigmoid: {@code 1 / (1 + exp(-z))}</li>
     *
     * @param z Value for processing.
     * @return Constrained value.
     */
    public double gFunction(double z){
        return 1 / (1 + Math.exp(-z)); //sigmoid function
    }

    /**
     * @return {@code y} calculated value by the neuron.
     */
    public int getY(){
        return (int) Math.round(gFunction(calcZ()));
    }
}
