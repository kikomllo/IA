import java.util.Scanner;

public class Main {
    public static void main (String [] args) throws Exception{
        int CURRENT_TEST = 3;
        int CURRENT_X0 = 1;

        double[] weights = {-1, 0.5, 0.5};
        String dataPath = "./Lab03/data/data.txt";

        DataManager dataManager = new DataManager(dataPath);
        Neuron neuron = new Neuron(dataManager.getValueSet(CURRENT_TEST, CURRENT_X0), weights);

        System.out.printf("Calculated value: %d\nExpected value: %d\n", neuron.getY(), dataManager.expectedResult(CURRENT_TEST));
    }
}
