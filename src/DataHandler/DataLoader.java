package DataHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    private static final double MAX_SPEED = 14000.0;
    private static final double MAX_DURATION = 100.0;

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
                if (values.length < 5) continue;

                double x = Double.parseDouble(values[0]);
                double y = Double.parseDouble(values[1]);

                // NORMALISE
                double normLeft = Double.parseDouble(values[2]) / MAX_SPEED;
                double normRight = Double.parseDouble(values[3]) / MAX_SPEED;
                double normDuration = Double.parseDouble(values[4]) / MAX_DURATION;

                dataset.add(new DataPoint(x, y, normLeft, normRight, normDuration));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return dataset;
    }
}