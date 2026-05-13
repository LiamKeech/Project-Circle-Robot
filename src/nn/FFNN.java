package nn;

public class FFNN {
    private final int numInputs = 1;
    private final int numHidden = 5;
    private final int numOutputs = 3;

    private final double[][] hiddenWeights; // 2x5 matrix
    private final double[] hiddenBiases;    // 5 values
    private final double[][] outputWeights; // 5x3 matrix
    private final double[] outputBiases;    // 3 values

    public FFNN() {
        hiddenWeights = new double[numInputs][numHidden];
        hiddenBiases = new double[numHidden];
        outputWeights = new double[numHidden][numOutputs];
        outputBiases = new double[numOutputs];
    }

    public int getChromosomeLength() {
        /*
        Flat gene = (Input-Hidden Weights) + (Hidden Biases) + (Hidden-Output Weights) + (Output Biases)
         */
        return (numInputs * numHidden) + numHidden + (numHidden * numOutputs) + numOutputs;
    }

    public void setChromosome(double[] genes) {
        /*
        Map the flat gene array to the FFNN's weights and biases
         */
        int index = 0;

        // 1. Input to Hidden Weights
        for (int i = 0; i < numInputs; i++) {
            for (int j = 0; j < numHidden; j++) {
                hiddenWeights[i][j] = genes[index++];
            }
        }
        // 2. Hidden Biases
        for (int j = 0; j < numHidden; j++) {
            hiddenBiases[j] = genes[index++];
        }
        // 3. Hidden to Output Weights
        for (int i = 0; i < numHidden; i++) {
            for (int j = 0; j < numOutputs; j++) {
                outputWeights[i][j] = genes[index++];
            }
        }
        // 4. Output Biases
        for (int j = 0; j < numOutputs; j++) {
            outputBiases[j] = genes[index++];
        }
    }

    public double[] fire(double[] inputs) {
        double[] hiddenOutputs = new double[numHidden];

        // Calculate Hidden Layer
        for (int j = 0; j < numHidden; j++) {
            double sum = hiddenBiases[j];
            for (int i = 0; i < numInputs; i++) {
                sum += inputs[i] * hiddenWeights[i][j];
            }
            hiddenOutputs[j] = sigmoid(sum);
        }

        // Calculate Output Layer
        double[] finalOutputs = new double[numOutputs];
        for (int j = 0; j < numOutputs; j++) {
            double sum = outputBiases[j];
            for (int i = 0; i < numHidden; i++) {
                sum += hiddenOutputs[i] * outputWeights[i][j];
            }
            finalOutputs[j] = sigmoid(sum);
        }
        return finalOutputs;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}