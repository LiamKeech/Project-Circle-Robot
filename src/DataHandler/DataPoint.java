package DataHandler;

public class DataPoint {
    private final double[] inputs;
    private final double[] expectedOutputs;

    public DataPoint(double x, double y, double normLeft, double normRight, double normDuration) {
        this.inputs = new double[]{x, y};
        this.expectedOutputs = new double[]{normLeft, normRight, normDuration};
    }

    public double[] getInputs() {
        return inputs;
    }

    public double[] getExpectedOutputs() {
        return expectedOutputs;
    }
}