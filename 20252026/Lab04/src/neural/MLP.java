package neural;

import math.Matrix;
import neural.activation.IDifferentiableFunction;
import neural.activation.Step;
import java.util.Random;

/**
 * @author hdaniel@ualg.pt
 * @version 202511052038
 */
public class MLP {

    private Matrix[] w;  //weights for each layer
    private Matrix[] b;  //biases for each layer
    private Matrix[] yp; //outputs for each layer
    private Matrix[] z;
    private IDifferentiableFunction[] act; //activation functions for each layer
    private int numLayers;

    /* Create a Multi-Layer Perceptron with the given layer sizes.
     * layerSizes is an array where each element represents the number of neurons in that layer.
     * For example, new MLP(new int[]{3, 5, 2}) creates a MLP with 3 input neurons,
     * 5 hidden neurons, and 2 output neurons.
     *
     * PRE: layerSizes.length >= 2
     * PRE: act.length == layerSizes.length - 1
     */
    public MLP(int[] layerSizes, IDifferentiableFunction[] act, int seed, Matrix[] weights) {
        if (seed == -1)
            seed = (int) System.currentTimeMillis();

        numLayers = layerSizes.length;

        //setup activation by layer
        this.act = act;

        //create output storage for each layer but the input layer
        yp = new Matrix[numLayers];
        z = new Matrix[numLayers];

        //create weights and biases for each layer
        //each row in w[l] represents the weights that are input
        w = new Matrix[numLayers-1];
        b = new Matrix[numLayers-1];

        Random rnd = new Random(seed);
        double limit = (double) 1 /layerSizes[0];
        double[] sampleLimits = {-limit, limit};

        for (int i=0; i < numLayers-1; i++) {
            if (weights == null)
                w[i] = Matrix.Rand(layerSizes[i], layerSizes[i + 1], rnd, sampleLimits);
            //System.out.println("Weights [" + i + "] Dims: [" + w[i].rows() + ", " + w[i].cols() + "]\n");
            b[i] = Matrix.Rand(1, layerSizes[i + 1], rnd, sampleLimits);
        }
        if (weights != null)
            w = weights;
    }

    public MLP(int[] layerSizes, IDifferentiableFunction[] act, int seed){
        this(layerSizes, act, seed, null);
    }


    // Feed forward propagation
    // also used to predict after training the net
    // yp[0] = X
    // yp[l+1] = Sigmoid( yp[l] * w[l]+b[l] )
    public Matrix predict(Matrix X) {
        yp[0] = X;
        for (int l=0; l < numLayers-1; l++) {
            z[l+1] = yp[l].dot(w[l]).addRowVector(b[l]);
            yp[l+1] = z[l+1].apply(act[l].fnc());
            //yp[l + 1] = yp[l].dot(w[l]).addRowVector(b[l]).apply(act[l].fnc());
        }
        return yp[numLayers-1];
    }


    // back propagation
    public Matrix backPropagation(Matrix X, Matrix y, double lr) {
        Matrix e = null;
        Matrix delta = null;

        //back propagation using generalised delta rule
        for (int l = numLayers-2; l >= 0; l--) {
            if (l == numLayers-2) {                     //output layer
                e = y.sub(yp[l + 1]);                   //e = y - yp[l+1]
            }
            else {                                      //propagate error to previous layer
                //e = delta * w[l+1]^T
                assert delta != null;
                e = delta.dot(w[l+1].transpose());
            }

            //dy = yp[l+1] .* (1-yp[l+1])
            Matrix dy = z[l+1].apply(act[l].derivative());

            //delta = e .* dy
            delta = e.mult(dy);

            //w[l] += yp[l]^T * delta * lr
            w[l] = w[l].add(yp[l].transpose().dot(delta).mult(lr));

            // update biases
            b[l] = b[l].addRowVector(delta.sumColumns().mult(lr));
        }
        return e;
    }


    public double[] train(Matrix X, Matrix y, Matrix evX, Matrix evY, double learningRate, int epochs) {
        //Matrix.shufflePair(X, y);

        int nSamples = X.rows();
        double[] mse = new double[epochs];

        double bestTestError = 1;
        Matrix[] bestW = new Matrix[w.length];

        for (int epoch=0; epoch < epochs; epoch++) {

            //forward propagation
            Matrix ypo = predict(X);

            Matrix e = y.sub(ypo);

            //mse
            mse[epoch] = e.dot(e.transpose()).get(0, 0) / nSamples;

            //backward propagation
            backPropagation(X, y, learningRate);

            //Matrix.shufflePair(evX, evY);

            //Predict and output results
            Matrix pred = predict(evX);

            //convert probabilities to integer classes: 2 or 3
            pred = pred.apply(new Step().fnc());
            evY = evY.apply(new Step().fnc());  

            int counter = 0;
            for (int i = 0; i < pred.rows(); i++){
                if (evY.get(i, 0) != pred.get(i, 0))
                    counter++;
            }

            if ((double) counter / evY.rows() < bestTestError) {
                bestTestError = (double) counter / evY.rows();

                for (int i = 0; i < bestW.length; i++){
                    bestW[i] = w[i].clone();
                }
            }
        }

        for (int i = 0; i < bestW.length; i++){
            w[i] = bestW[i].clone();
        }

        return mse;
    }

    public Matrix[] getWeights(){
        return w;
    }

    public Matrix[] getBiases(){
        return b;
    }
}
