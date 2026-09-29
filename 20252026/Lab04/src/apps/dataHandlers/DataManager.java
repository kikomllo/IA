package apps.dataHandlers;

//import com.opencsv.CSVReader;
import math.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import java.util.Scanner;

/** Data Manager responsible for reading from {@code dataPath(String)} and {@code labelsPath(String)}. Also,
 * responsible for storing in {@code data(Matrix[])} and {@code labels(Matrix[])} all the pixels and corresponding
 * labels for each image in the dataSet provided.
 *
 * @author Francisco Melo, a84085
 * @version 081220250352
 */
public class DataManager {
    private final int[] dataParams; //{rows, columns, dataSize}
    private final int[] labelsParams; //{rows, columns, labelsSize}
    private double[][] data;
    private double[][] labels;
    private Matrix[] w;
    private Matrix[] b;

    public DataManager(String dataPath, String labelsPath, int[] dataParams, int[] labelsParams, boolean read){
        this.dataParams = dataParams; //{rows, columns, dataSize}
        this.labelsParams = labelsParams;

        try {
            //check valid extensions
            String[] path = dataPath.split("\\.");
            if (!path[path.length-1].equals("csv"))
                throw new IllegalArgumentException();

            path = labelsPath.split("\\.");
            //check valid extension
            if (!path[path.length-1].equals("csv"))
                throw new IllegalArgumentException();

            //CSVReader readerData = new CSVReader(new FileReader(dataPath));
            //CSVReader readerLabels = new CSVReader(new FileReader(labelsPath));

            //readData(readerData);
            //readLabels(readerLabels);

            if (read) {
                readWeights();
                readBiases();
            }

            //readerLabels.close();
            //readerData.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public DataManager(String dataPath, String labelsPath, int[] dataParams, int[] labelsParams){
        this(dataPath, labelsPath, dataParams, labelsParams, false);
    }

    /**
     * Constructor for DataManager, receives {@code dataPath(String)} and {@code labelsPath(String)}.
     *
     * <h3>
     *     Note:
     *     <p>Both paths need to end in {@code ".csv"}.<p/>
     * </h3>
     *
     * @param dataPath {@code String} path for main data.
     * @param labelsPath {@code String} path for labels.
     */

    public DataManager(String dataPath, String labelsPath){
        this (dataPath, labelsPath, new int[] {1, 400, 800}, new int[] {1, 1, 800});
    }

    public DataManager(String dataPath, String labelsPath, boolean read){
        this (dataPath, labelsPath, new int[] {1, 400, 800}, new int[] {1, 1, 800}, read);
    }


    /**
     * Method responsible for reading the Data specified in the dataPath provided in the constructor.
     * <br>Receives {@code CSVReader} reader and throws generic {@code Exception}.
     *
     * @param reader {@code CSVReader} open on {@code dataPath(File)}.
     * @throws Exception {@code reader.readNext()} throws exception.
     */
    /*private void readData(CSVReader reader) throws Exception {
        this.data = new double[dataParams[2]][dataParams[0]*dataParams[1]];
        String[] line;
        int current = 0;

        while((line = reader.readNext()) != null && current < dataParams[2]){
            for(int i = 0; i < dataParams[0]*dataParams[1]; i++) {
                double value = Double.parseDouble(line[i]);
                if (value < 0 || value > 1) value = Math.round(value);
                if (value < 0 || value > 1) {
                    System.out.println("ERRO");
                    System.exit(0);
                }
                this.data[current][i] = value;
            }
            current++;
        }
    }*/

    /**
     * Method responsible for reading the Labels specified in the labelsPath provided in the constructor.
     * <br>Receives {@code CSVReader} reader and throws generic {@code Exception}.
     *
     * @param reader {@code CSVReader} open on {@code labelsPath(File)}.
     * @throws Exception {@code reader.readNext()} throws exception.
     */
    /*private void readLabels(CSVReader reader) throws Exception {
        this.labels = new double[labelsParams[2]][labelsParams[0]*labelsParams[1]];
        String[] line;
        int current = 0;

        while((line = reader.readNext()) != null && current < labelsParams[2]){
            for(int i = 0; i < labelsParams[0]*labelsParams[1]; i++)
                this.labels[current][i] = (Double.parseDouble(line[i]) == 2) ? 0 : 1;
            current++;
        }
    }*/

    private void readWeights() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("./data/weights.txt"));
        Matrix[] w = new Matrix[2];
        int[] params = {400, 10, 1};

        while (scanner.hasNext()){
            for(int i = 0; i < 2; i++){
                double[][] dataWi = new double[params[i]][params[i+1]];

                for (int j = 0; j < params[i]; j++){
                    String line = scanner.nextLine();
                    String[] values = line.split(" ");

                    for(int k = 0; k < params[i+1]; k++){
                        dataWi[j][k] = Double.parseDouble(values[k]);
                    }
                }

                w[i] = new Matrix(dataWi);
            }
        }

        this.w = w;
    }

    private void readBiases() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("./data/biases.txt"));
        Matrix[] b = new Matrix[2];
        int[] params = {1, 10, 1, 1};

        while (scanner.hasNext()){
            for(int i = 0; i < 2; i++){
                double[][] dataBi = new double[params[i*2]][params[(i*2)+1]];

                for (int j = 0; j < params[i*2]; j++){
                    String line = scanner.nextLine();
                    String[] values = line.split(" ");

                    for(int k = 0; k < params[i*2+1]; k++){
                        dataBi[j][k] = Double.parseDouble(values[k]);
                    }
                }

                b[i] = new Matrix(dataBi);
            }
        }

        this.b = b;
    }

    public Matrix[] getTrainingMatrixPair(){
        int newSize = (int)(data.length*0.8);

        double[][] trainingData = new double[newSize][data[0].length];
        double[][] trainingLabels = new double[newSize][labels[0].length];

        Matrix[] matrixPairDataLabel = {getMatrixData(), getMatrixLabels()};

        Matrix.shufflePair(matrixPairDataLabel[0], matrixPairDataLabel[1]);

        double[][][] randomDataLabels = {matrixPairDataLabel[0].data(), matrixPairDataLabel[1].data()};

        for (int i = 0; i < newSize; i++){
            trainingData[i] = randomDataLabels[0][i];
            trainingLabels[i] = randomDataLabels[1][i];
        }

        return new Matrix[] {new Matrix(trainingData), new Matrix(trainingLabels)};
    }

    public Matrix[] getTestingMatrixPair(){
        int trainingSize = (int)(data.length*0.8);
        int newSize = labels.length-trainingSize;

        double[][] testingData = new double[newSize][data[0].length];
        double[][] testingLabels = new double[newSize][labels[0].length];

        Matrix[] matrixPairDataLabel = {getMatrixData(), getMatrixLabels()};

        Matrix.shufflePair(matrixPairDataLabel[0], matrixPairDataLabel[1]);

        double[][][] randomDataLabels = {matrixPairDataLabel[0].data(), matrixPairDataLabel[1].data()};

        for (int i = trainingSize, counter = 0; i < data.length; i++, counter++){
            testingData[counter] = randomDataLabels[0][i];
            testingLabels[counter] = randomDataLabels[1][i];
        }

        return new Matrix[] {new Matrix(testingData), new Matrix(testingLabels)};
    }

    /**
     * @return {@code double[][] data} received from {@code dataPath} specified in the class constructor.
     */
    public double[][] getData() {
        return data;
    }

    public double[][] getData(int i) {
        return new double[][]{data[i]};
    }

    public Matrix getMatrixData() {
        return new Matrix(data);
    }

    public Matrix getMatrixData(int i) {
        return new Matrix(new double[][]{data[i]});
    }

    /**
     * @return {@code double[][] labels} received from {@code labelsPath} specified in the class constructor.
     */
    public double[][] getLabels() {
        return labels;
    }

    public double[][] getLabels(int i) {
        return new double[][]{labels[i]};
    }

    public Matrix getMatrixLabels() {
        return new Matrix(labels);
    }

    public Matrix[] getW(){
        return this.w;
    }

    public Matrix getMatrixLabels(int i) {
        return new Matrix(new double[][]{labels[i]});
    }


}
