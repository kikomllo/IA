package neural.activation;

import java.util.function.Function;

/**
 * @author hdaniel@ualg.pt
 * @version 202511051244
 */
public class Step implements IDifferentiableFunction {

    static private double threshold = 0.5;

    @Override
    public Function<Double, Double> fnc() {
        return num -> (double) ((num < 0.5f) ? 2 : 3);
    }

    @Override
    public Function<Double, Double> derivative() {
        throw new UnsupportedOperationException("Step function is not differentiable.");
    }

}
