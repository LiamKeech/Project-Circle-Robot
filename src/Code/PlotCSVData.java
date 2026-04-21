package Code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PlotCSVData {

    public static void main(String[] args) {
        String csvFile = "data/circle_training_data.csv";
        ArrayList<State> csvPath = new ArrayList<>();
        State startState = null;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");

                double X = Double.parseDouble(values[0]);
                double Y = Double.parseDouble(values[1]);

                State currentState = new State(X, Y, 0);

                if (startState == null) {
                    startState = currentState;
                }

                csvPath.add(currentState);
            }
        } catch (IOException e) {
            System.err.println("Failed to read CSV: " + e.getMessage());
            return;
        }

        System.out.println("Successfully loaded " + csvPath.size() + " actual points.");

        if (startState != null) {
            VisualFrame vis = new VisualFrame(50, 50, 800, 800, new ArrayList<>(), 1.0, new Point(999, 999), new Point((int)startState.sx, (int)startState.sy), 1.0, 3.0);
            vis.setPath(csvPath, "CSV Trajectory");
            new Thread(vis).start();
        }
    }
}