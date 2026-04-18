package EvolutionaryNeuralNetwork;

import java.util.Random;

public class FFNN {
    private static final int INPUT_SIZE = 2;
    private static final int HIDDEN_SIZE = 10;
    private static final int OUTPUT_SIZE = 3;

    private double[][] weightsInputHidden;
    private double[][] weightsHiddenOutput;
    private double[] biasHidden;
    private double[] biasOutput;

    public FFNN() {
        Random rand = new Random(); // Initialise between -1 and 1

        weightsInputHidden = new double[HIDDEN_SIZE][INPUT_SIZE];
        weightsHiddenOutput = new double[OUTPUT_SIZE][HIDDEN_SIZE];
        biasHidden = new double[HIDDEN_SIZE];
        biasOutput = new double[OUTPUT_SIZE];

        double min = -1.0;
        double max = 1.0;

        for (int h = 0; h < HIDDEN_SIZE; h++) {
            biasHidden[h] = rand.nextDouble(min, max);

            for (int i = 0; i < INPUT_SIZE; i++) {
                weightsInputHidden[h][i] = rand.nextDouble(min, max);
            }
        }

        for (int o = 0; o < OUTPUT_SIZE; o++) {
            biasOutput[o] = rand.nextDouble(min, max);

            for (int h = 0; h < HIDDEN_SIZE; h++) {
                weightsHiddenOutput[o][h] = rand.nextDouble(min, max);
            }
        }
    }

    // Forward pass through the network.
    // Calculate values by multiplying weights and adding biases
    // Result passed through activation function
    public double[] fire(double[] inputs) {
        double[] hiddenLayer = new double[HIDDEN_SIZE];
        for (int h = 0; h < HIDDEN_SIZE; h++) {
            double sum = biasHidden[h];
            for (int i = 0; i < INPUT_SIZE; i++) {
                sum += weightsInputHidden[h][i] * inputs[i];
            }
            hiddenLayer[h] = sigmoid(sum);
        }

        double[] outputLayer = new double[OUTPUT_SIZE];
        for (int o = 0; o < OUTPUT_SIZE; o++) {
            double sum = biasOutput[o];
            for (int h = 0; h < HIDDEN_SIZE; h++) {
                sum += weightsHiddenOutput[o][h] * hiddenLayer[h];
            }
            outputLayer[o] = sum; // Linear activation for output
        }

        return outputLayer;
    }

    // Plain MSE across all outputs.
    public double calculateError(double[] predicted, double[] actual) {
        double sumSquaredError = 0.0;
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            double diff = predicted[i] - actual[i];
            sumSquaredError += (diff * diff);
        }
        return sumSquaredError / OUTPUT_SIZE;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    // EA Methods - Throw everything into 1D array, adjusts weights, rebuild from 1D array

    public int getChromosomeLength() {
        return (HIDDEN_SIZE * INPUT_SIZE) + HIDDEN_SIZE + (OUTPUT_SIZE * HIDDEN_SIZE) + OUTPUT_SIZE;
    }

    public void setChromosome(double[] chromosomeGenes) {
        int idx = 0;

        // 1. Input layer -> Hidden layer weights (2D array)
        for (int h = 0; h < HIDDEN_SIZE; h++) {
            System.arraycopy(chromosomeGenes, idx, weightsInputHidden[h], 0, INPUT_SIZE);
            idx += INPUT_SIZE;
        }

        // 2. Hidden layer biases
        System.arraycopy(chromosomeGenes, idx, biasHidden, 0, HIDDEN_SIZE);
        idx += HIDDEN_SIZE;

        // 3. Hidden layer -> Output layer weights (2D array)
        for (int o = 0; o < OUTPUT_SIZE; o++) {
            System.arraycopy(chromosomeGenes, idx, weightsHiddenOutput[o], 0, HIDDEN_SIZE);
            idx += HIDDEN_SIZE;
        }

        // 4. Output layer biases
        System.arraycopy(chromosomeGenes, idx, biasOutput, 0, OUTPUT_SIZE);
    }
}
