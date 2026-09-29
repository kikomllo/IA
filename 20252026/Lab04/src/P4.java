import apps.dataHandlers.DataManager;
import math.Matrix;
import neural.MLP;
import neural.activation.IDifferentiableFunction;
import neural.activation.ReLU;
import neural.activation.Sigmoid;
import neural.activation.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author hdaniel@ualg.pt <br>
 * Francisco Melo, a84085
 * @version 202511050300
 */
public class P4 {

    public static void main(String[] args) throws IOException {
        int[] topology = {400, 10, 1};

        //Get input and create evaluation Matrix

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        ArrayList<double[]> inputList = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) break;

            String[] values = line.split(",");
            double[] img = new double[400];

            // Preencher o vetor com segurança
            for (int i = 0; i < Math.min(values.length, 400); i++) {
                img[i] = Double.parseDouble(values[i]);
            }
            inputList.add(img);
        }


        double[][] data = new double[inputList.size()][400];

        for(int i = 0; i < inputList.size(); i++){
            data[i] = inputList.get(i);
        }

        Matrix evX = new Matrix(data);

        DataManager dataManager = new DataManager(
                "./src/data/dataset.csv", "./src/data/labels.csv", true
        );

        //Train the MLP
        MLP mlp = new MLP(topology,
                new IDifferentiableFunction[]{
                        new ReLU(),
                        new Sigmoid()},
                -1, dataManager.getW());

        //Predict and output results
        Matrix pred = mlp.predict(evX);

        //convert probabilities to integer classes: 2 or 3
        pred = pred.apply(new Step().fnc());

        for (int i = 0; i < pred.rows(); i++){
            System.out.println((int) pred.get(i, 0));
        }

        //sc.close();

    }
}
