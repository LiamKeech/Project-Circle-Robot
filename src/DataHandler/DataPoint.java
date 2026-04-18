package DataHandler;

public class DataPoint {
    double PosX, PosY, PosZ;
    double VelX, VelY, VelZ;

    public DataPoint(double posX, double posY, double posZ, double velX, double velY, double velZ) {
        PosX = posX;
        PosY = posY;
        PosZ = posZ;
        VelX = velX;
        VelY = velY;
        VelZ = velZ;
    }

    public double[] toArray() {
        return new double[]{PosX, PosY, PosZ, VelX, VelY, VelZ};
    }
}
