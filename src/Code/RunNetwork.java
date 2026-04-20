package Code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//Get speeds from the CSV file, turn it into commands, run it, display it

public class RunNetwork {
    private static final State START_STATE = new State(-20, 20, 270);

    public static void main(String[] args) {
        String csvFile = "data/predicted_outputs.csv";
        ArrayList<Command> commands = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");

                double normLeft = Double.parseDouble(values[2]);
                double normRight = Double.parseDouble(values[3]);
                double normDuration = Double.parseDouble(values[4]);

                int physicalLeftSpeed = (int) Math.round(normLeft * 14000.0);
                int physicalRightSpeed = (int) Math.round(normRight * 14000.0);
                int physicalDuration = (int) Math.round(normDuration * 100.0);
                commands.add(new Command(physicalLeftSpeed, physicalRightSpeed, physicalDuration));
            }
        } catch (IOException e) {
            System.err.println("Failed to read predictions: " + e.getMessage());
            return;
        }

        System.out.println("Successfully loaded " + commands.size() + " commands.");

        KheperaSimulator sim = new KheperaSimulator(START_STATE);
        ArrayList<KheperaState> simulatedStates = sim.getKheperaState(commands);

        ArrayList<State> drivenPath = new ArrayList<>();
        for (KheperaState ks : simulatedStates) {
            drivenPath.add(ks.position);
        }

        VisualFrame vis = new VisualFrame(50, 50, 800, 800, new ArrayList<>(), 1.0, new Point(999, 999), new Point(START_STATE.sx, START_STATE.sy), 1.0, 3.0);
        vis.setPath(drivenPath, "Running NN Predictions");
        new Thread(vis).start();
    }
}
