package apps;

import math.Matrix;
import neural.MLP;
import neural.activation.IDifferentiableFunction;
import neural.activation.Sigmoid;
import neural.activation.Step;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MLPNXORTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    void test1000runs(){
        for (int i = 0; i < 1; i++){
            double lr    = 0.2;//define a learning rate in range [0, 1]
            int   epochs = 10000;//define the number of epochs in the order of thousands
            int[] topology = {2, 2, 1};

            //Dataset
            Matrix trX = new Matrix(
                    new double[][]{
                            {0, 0},
                            {0, 1},
                            {1, 0},
                            {1, 1}});


            Matrix trY = new Matrix(
                    new double[][]{
                            {1},
                            {0},
                            {0},
                            {1}});

            double[][] input = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};

            Matrix evX = new Matrix(input);

            //Train the MLP
            MLP mlp = new MLP(topology,
                    new IDifferentiableFunction[]{
                            new Sigmoid(),
                            new Sigmoid(),},
                    -1);
            mlp.train(trX, trY, lr, epochs);

            //Predict and output results
            Matrix pred = mlp.predict(evX);

            //convert probabilities to integer classes: 0 or 1
            pred = pred.apply(new Step().fnc());

            assertEquals(
                    "1.000\n" +
                    "0.000\n" +
                    "0.000\n" +
                    "1.000",
                    pred.toString().replace(" ", ""));
        }
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

}