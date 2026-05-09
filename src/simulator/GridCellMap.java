package Code;

public class GridCellMap {
    public static final double GRID_WIDTH = 30.0;
    public static final double CELL_WIDTH = GRID_WIDTH / 3.0;

    private GridCellMap() { }

    public static int getBlockID(double x, double y) {
        if (Math.abs(x) > GRID_WIDTH || Math.abs(y) > GRID_WIDTH) {
            return -1;
        }

        int col = (x < -CELL_WIDTH) ? 0 : (x < CELL_WIDTH ? 1 : 2);
        int row = (y >  CELL_WIDTH) ? 0 : (y < -CELL_WIDTH ? 2 : 1);

        return row * 3 + col;
    }

    public static int getBlockID(State s) {
        return getBlockID(s.sx, s.sy);
    }
}
