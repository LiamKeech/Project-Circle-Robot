package Code;

import java.util.ArrayList;

public class TimeTrialFitnessFunction {
    public static final double gridWidth = 30.0; // 60 x 60 grid
    public static final double cellWidth = gridWidth / 3.0; // 3 x 3 cells

    private static final int[] clockwiseOrder = {0, 1, 2, 5, 8, 7, 6, 3, 0};

    private static final double centrePenalty = 25.0;
    private static final double outsidePenalty = 50.0;
    private static final double cellReward = 20.0;
    private static final double traversalReward = 300.0;
    private static final double proximityReward = 5.0;
    private static final double speedReward = 10.0;

    private static final double maxDistance = Math.sqrt(2) * 2 * gridWidth;

    private static double distanceToCheckpoint(double x, double y, int cellID) {
        int col = cellID % 3;
        int row = cellID / 3;

        double cellCentreX = (col - 1) * cellWidth;
        double cellCentreY = (1 - row) * cellWidth;

        return Math.sqrt( Math.pow((x - cellCentreX), 2) + Math.pow((y - cellCentreY), 2) );
    }

    private static double distanceOutsideGrid(double x, double y) {
        double dx = Math.max(0, Math.abs(x) - gridWidth);
        double dy = Math.max(0, Math.abs(y) - gridWidth);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double distanceFromCentre(double x, double y) {
        return Math.min(Math.sqrt(x * x + y * y), cellWidth);
    }

    public static double evaluate(ArrayList<KheperaState> states) {
        double fitness = 0.0;
        int nextCellIdx = 0;
        int totalStates = states.size();

        for (int i = 0; i < states.size(); i++) {
            KheperaState state = states.get(i);
            double x = state.position.sx;
            double y = state.position.sy;
            int cellID = GridCellMap.getBlockID(x, y);

            if (cellID == -1) {
                double distanceOutsideGrid = distanceOutsideGrid(x, y);
                fitness -= outsidePenalty * (1 + distanceOutsideGrid / gridWidth);

            } else if (cellID == 4) {
                double distanceFromCentre = distanceFromCentre(x, y);
                fitness -= centrePenalty * (1 - (distanceFromCentre / cellWidth));

            } else {
                if (nextCellIdx < clockwiseOrder.length) {
                    int targetCell = clockwiseOrder[nextCellIdx];
                    double distance = distanceToCheckpoint(x, y, targetCell);
                    fitness += proximityReward * (1.0 - distance / maxDistance);

                    double proximityBonus = proximityReward * (1 - (distance / maxDistance));
                    fitness += proximityBonus;
                }

                if (nextCellIdx < clockwiseOrder.length && cellID == clockwiseOrder[nextCellIdx]) {
                    fitness += cellReward;

                    double speedFactor = 1.0 - ((double) i / totalStates); // Higher reward for reaching checkpoints earlier in the sequence
                    fitness += speedReward * speedFactor;

                    nextCellIdx++;
                    if (nextCellIdx == clockwiseOrder.length) {
                        fitness += traversalReward;
                        return fitness;
                    }
                }
            }
        }
        return fitness;
    }
}
