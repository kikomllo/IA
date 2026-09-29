package neural.activation;

import java.util.function.Function;

/**
 * @author Francisco Melo, a84085
 * @version 081220251932
 */
public class ReLU implements IDifferentiableFunction  {

    @Override
    public Function<Double, Double> fnc() {
        return (z) -> (z <= 0) ? 0 : z;
    }

    @Override
    public Function<Double, Double> derivative() {
        return (z) -> (z > 0) ? 1.0 : 0.0;
    }

}