package DataHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    // Normalisation constants make it safe to scale between 0 and 1
    private static final double MAX_POS = 35.0;
    private static final double MAX_SPEED = 14000.0;
    private static final double MAX_DURATION = 100.0;

    // Procedurally generated training data, takes -20, 20 and calculates new positions to go around a circle
    // Read CSV file, normalise the values for easier evolution in the neural network, and return a list of DataPoints

    public static List<DataPoint> loadData(String filePath) {
        List<DataPoint> dataset = new ArrayList<>();
        String line;
        boolean isFirstLine = true;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");

                // NORMALISE
                double normX = Double.parseDouble(values[0]) / MAX_POS;
                double normY = Double.parseDouble(values[1]) / MAX_POS;
                double normLeft = Double.parseDouble(values[2]) / MAX_SPEED;
                double normRight = Double.parseDouble(values[3]) / MAX_SPEED;
                double normDuration = Double.parseDouble(values[4]) / MAX_DURATION;

                dataset.add(new DataPoint(normX, normY, normLeft, normRight, normDuration));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return dataset;
    }
}