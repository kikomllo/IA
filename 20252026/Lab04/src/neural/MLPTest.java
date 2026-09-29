package neural;

import apps.dataHandlers.DataManager;
import math.Matrix;
import neural.activation.IDifferentiableFunction;
import neural.activation.ReLU;
import neural.activation.Sigmoid;
import neural.activation.Step;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MLPTest {
    @Test
    public void mainMLPTest(){
        double lr    = 0.001;//define a learning rate in range [0, 1]
        int   epochs = 100  ;//define the number of epochs in the order of thousands
        int[] topology = {400, 10, 1};

        DataManager dataManager = new DataManager(
                "./data/dataset.csv", "./data/labels.csv"
        );

        //Dataset
        Matrix[] tr = dataManager.getTrainingMatrixPair();

        Matrix[] ev = dataManager.getTestingMatrixPair();

        //Train the MLP
        MLP mlp = new MLP(topology,
                new IDifferentiableFunction[]{
                        new ReLU(),
                        new Sigmoid()},
                -1);
        mlp.train(tr[0], tr[1], ev[0], ev[1], lr, epochs);

        //Matrix.shufflePair(ev[0], ev[1]);

        //Predict and output results
        Matrix pred = mlp.predict(ev[0]);

        //convert probabilities to integer classes: 2 or 3
        pred = pred.apply(new Step().fnc());

        int counter = 0;
        Matrix labels = ev[1].apply(new Step().fnc());
        for (int i = 0; i < pred.rows(); i++){
            if (labels.get(i, 0) != pred.get(i, 0))
                counter++;
        }

        System.out.println(((double)counter / labels.rows()));

        assertEquals(labels, pred);


//        //print output
//        for (int i = 0; i < pred.rows(); i++) {
//            for (int j = 0; j < pred.cols(); j++) {
//                System.out.print((int) pred.get(i, j));
//            }
//            System.out.println();
//        }
    }

//    @Test
//    void predictTest() {
//        int[] topology = {400, 64, 1};
//
//        MLP mlp = new MLP(topology,
//                new IDifferentiableFunction[]{
//                        new ReLU(),
//                        new Sigmoid()},
//                -1);
//
//        DataManager dataManager = new DataManager(
//                "./data/dataset.csv", "./data/labels.csv"
//        );
//
//        //Dataset
//        Matrix trX = dataManager.getMatrixData(0);
//
//        Matrix trY = dataManager.getMatrixLabels(0);
//
//        System.out.println(trY);
//
//        mlp.train(trX, trY, 0.99, 100);
//
//        //Predict and output results
//        Matrix pred = mlp.predict(trX);
//
//        System.out.println(pred);
//    }
//
//    @Test
//    void predictOneInputTest() {
//        int[] topology = {400, 64, 1};
//
//        MLP mlp = new MLP(topology,
//                new IDifferentiableFunction[]{
//                        new ReLU(),
//                        new Sigmoid()},
//                -1);
//
//        DataManager dataManager = new DataManager(
//                "./data/dataset.csv", "./data/labels.csv"
//        );
//
//        //Dataset
//        Matrix trX = dataManager.getMatrixData(0);
//
//        Matrix trY = dataManager.getMatrixLabels(0);
//
//        mlp.train(trX, trY, 0.2, 100);
//
//        //Predict and output results
//        Matrix pred = mlp.predict(trX);
//
//        System.out.println(pred);
//    }

    @Test
    void backPropagation() {

    }

    @Test
    void MLPIntensiveTraining() throws IOException {
        //TODO: INTENSIVE TRAINING FOR BEST PERCENTAGE!!!

        double bestPercent = 1.0f;
        Matrix[] bestWeights = null;
        Matrix[] bestBiases = null;
        final int ROUNDS = 1000;

        for (int round = 0; round < ROUNDS; round++){
            System.out.println("Round[" + round + "]: ");

            double lr    = 0.004;//define a learning rate in range [0, 1]
            int   epochs = 100  ;//define the number of epochs in the order of thousands
            int[] topology = {400, 10, 1};

            DataManager dataManager = new DataManager(
                    "./data/dataset.csv", "./data/labels.csv", true
            );

            //Dataset
            Matrix[] tr = dataManager.getTrainingMatrixPair();

            //Matrix[] ev = dataManager.getTestingMatrixPair();
            Matrix[] ev = {dataManager.getMatrixData(), dataManager.getMatrixLabels()};

            //Train the MLP
            MLP mlp = new MLP(topology,
                    new IDifferentiableFunction[]{
                            new ReLU(),
                            new Sigmoid()},
                    -1, dataManager.getW());
            mlp.train(tr[0], tr[1], ev[0], ev[1], lr, epochs);

            //Matrix.shufflePair(ev[0], ev[1]);

            //Predict and output results
            Matrix pred = mlp.predict(ev[0]);

            //convert probabilities to integer classes: 2 or 3
            pred = pred.apply(new Step().fnc());

            int counter = 0;
            Matrix labels = ev[1].apply(new Step().fnc());
            for (int i = 0; i < pred.rows(); i++){
                if (labels.get(i, 0) != pred.get(i, 0))
                    counter++;
            }

            System.out.println(((double)counter / labels.rows()));
            System.out.println("Best Percent: " + bestPercent);
            if (((double)counter / labels.rows()) < bestPercent && (double)counter / labels.rows() != 0) {
                bestPercent = ((double) counter / labels.rows());
                bestWeights = mlp.getWeights();
                bestBiases = mlp.getBiases();


                new File("./data/weights.txt").delete();
                File file = new File("./data/weights.txt");

                try (Writer newWriter = new BufferedWriter(new FileWriter(file))) {
                    newWriter.write(bestWeights[0] + "" + bestWeights[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new File("./data/biases.txt").delete();
                file = new File("./data/biases.txt");

                try (Writer newWriter = new BufferedWriter(new FileWriter(file))) {
                    newWriter.write(bestBiases[0] + "" + bestBiases[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Best Percent: " + bestPercent);
        new File("./data/weights.txt").delete();
        File file = new File("./data/weights.txt");

        try (Writer newWriter = new BufferedWriter(new FileWriter(file))) {
            newWriter.write(bestWeights[0] + "" + bestWeights[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new File("./data/biases.txt").delete();
        file = new File("./data/biases.txt");

        try (Writer newWriter = new BufferedWriter(new FileWriter(file))) {
            newWriter.write(bestBiases[0] + "" + bestBiases[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test() throws IOException {

        double lr    = 0.002;//define a learning rate in range [0, 1]
        int   epochs = 100  ;//define the number of epochs in the order of thousands
        int[] topology = {400, 10, 1};

        DataManager dataManager = new DataManager(
                "./data/dataset.csv", "./data/labels.csv", true
        );

        //Dataset
        //Matrix[] tr = dataManager.getTrainingMatrixPair();

        Matrix[] ev = {dataManager.getMatrixData(), dataManager.getMatrixLabels()};
        //Matrix[] ev = dataManager.getTestingMatrixPair();

        //Train the MLP
        MLP mlp = new MLP(topology,
                new IDifferentiableFunction[]{
                        new ReLU(),
                        new Sigmoid()},
                -1, dataManager.getW());
        //mlp.train(tr[0], tr[1], ev[0], ev[1], lr, epochs);

        //Matrix.shufflePair(ev[0], ev[1]);

        //Predict and output results
        Matrix pred = mlp.predict(ev[0]);

        //convert probabilities to integer classes: 2 or 3
        pred = pred.apply(new Step().fnc());

        int counter = 0;
        Matrix labels = ev[1].apply(new Step().fnc());
        for (int i = 0; i < pred.rows(); i++){
            if (labels.get(i, 0) != pred.get(i, 0))
                counter++;
        }

        System.out.println(((double)counter / labels.rows()));

        assertEquals(labels, pred);
    }
}