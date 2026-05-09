package Code;

import java.util.ArrayList;

public class CheckpointFitnessFunction {
    public static final double gridWidth = 30.0; // 60 x 60 grid
    public static final double cellWidth = gridWidth / 3.0; // 3 x 3 cells

    private static final int[] clockwiseOrder = {0, 1, 2, 5, 8, 7, 6, 3, 0};

    private static final double centrePenalty = 25.0;
    private static final double outsidePenalty = 50.0;
    private static final double cellReward = 20.0;
    private static final double traversalReward = 300.0;
    private static final double proximityReward = 5.0;

    private static final double maxDistance = Math.sqrt(2) * 2 * gridWidth;

    private static double[] getCheckpointCoordinates(int cellID) {
        int col = cellID % 3;
        int row = cellID / 3;

        double centreX = (col - 1) * cellWidth;
        double centreY = (1 - row) * cellWidth;
        return new double[]{centreX, centreY};
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
                if (nextCellIdx < clockwiseOrder.length && cellID == clockwiseOrder[nextCellIdx]) {
                    fitness += cellReward;
                    nextCellIdx++;

                    if (nextCellIdx == clockwiseOrder.length) {
                        fitness += traversalReward;
                        return fitness;
                    }
                }

                if (nextCellIdx < clockwiseOrder.length) {
                    int targetCell = clockwiseOrder[nextCellIdx];
                    double[] targetPos = getCheckpointCoordinates(targetCell);

                    double dx = targetPos[0] - x;
                    double dy = targetPos[1] - y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    double proximityBonus = proximityReward * (1 - (distance / maxDistance));
                    fitness += proximityBonus;
                }
            }
        }
        return fitness;
    }
}
