package Code;

import javafx.util.Pair;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class SensorReadingSimulator {

    private static double[] xCoords = {-.049, -0.063, -0.045, -0.015, 0.015, 0.045, 0.063, 0.049, 0};
    private static double[] yCoords = {-0.038, 0.017, 0.051, 0.067, 0.067, 0.051, 0.017, -0.038, -0.052};

    private static double[] angles = {127.8, 74.9, 41.4, 12.6, -12.6, -41.4, -74.9, -127.8, 180};

    private double sensorAngle, obstacleDangerDistance, robotRadius, obstacleRadius, lineRadius;
    private Random random;
    private BasicNetwork NN;

    public SensorReadingSimulator(double sensorAngle, double obstacleDangerDistance, double robotRadius, double obstacleRadius, double lineRadius) {
        this.sensorAngle = sensorAngle;
        this.obstacleDangerDistance = obstacleDangerDistance;
        this.robotRadius = robotRadius;
        this.obstacleRadius = obstacleRadius;
        this.lineRadius = lineRadius;
        random = new Random();
    }

    public double[] getSensorReadings(int robotX, int robotY, double robotA, ArrayList<Point> courseObstacles) {
        int maxDistance = 20;
        int maxAngle = 32;
        int nrSensors = 9;
        Random rand = new Random();

        BasicNetwork NN = ((BasicNetwork)EncogDirectoryPersistence.loadObject(new File("NNSingle.eg")));

        double[] readings = new double[nrSensors];
        for (int x = 0; x < nrSensors; x++)
            readings[x] = rand.nextInt(40);

        for(Point p : courseObstacles) {

            double distance = distance(new double[]{robotX, robotY}, new double[]{p.x, p.y});
            if ( distance < maxDistance) {

                for (int x = 0; x < nrSensors; x++) {
                    Pair<Double, Double> rotated = rotateXY(xCoords[x], yCoords[x], robotA);
                    double sensorX = rotated.getKey();
                    double sensorY = rotated.getValue();
                    double sensorTheta = formatAngle(angles[x] + robotA);

                    double deltaX = p.x - (robotX + sensorX);
                    double deltaY = p.y - (robotY + sensorY);

                    int quadrant = getQuadrant(deltaX, deltaY);
                    double angle = Math.toDegrees(Math.atan(Math.abs(deltaY) / Math.abs(deltaX)));
                    switch (quadrant) {
                        case 1:
                            angle = 90 - angle;
                            break;
                        case 2:
                            angle = 90 + angle;
                            break;
                        case 3:
                            angle = 270 - angle;
                            break;
                        case 4:
                            angle = 270 + angle;
                            break;
                    }

                    double finalAngle = Math.abs(sensorTheta-angle);
                    if (finalAngle < maxAngle) {
                        double[] input = new double[2];
                        input[0] = NormalizeZeroOne(distance + 5, 0, maxDistance); //800-1100, 1800-2000, 3000-3900, 3000
//                        input[0] = NormalizeZeroOne(distance, 0, maxDistance); //800-1100, 1800-2000, 3000-3900, 3000
                        input[1] = NormalizeZeroOne(finalAngle, 0, maxAngle);
                        double[] output = new double[1];
                        NN.compute(input, output);
//                        double newReading = deNormalizeZeroOne(output[0], 0, 4200);
                        double newReading = deNormalizeZeroOne(output[0], 0, 3900);
                        readings[x] = (readings[x] < newReading) ? newReading : readings[x];
                    }
                }

            }
        }

//        for (int x = 0; x < nrSensors; x++)
//        {
//            readings[x] += rand.nextInt(200) - 100;
//            if (readings[x] < 0)
//                readings[x] = 0;
//        }

        return readings;
    }
    
    public double[] getLineReading(int robotX, int robotY, double robotA, ArrayList<Point> linePoints) {
         //angles are counter clockwise starting at 0 pointing up  
        
        double X = robotX;
        double Y = robotY + robotRadius;
        
        int sensorDistance = 1;
        
        double sensorAngle = Math.toDegrees(Math.asin(sensorDistance/robotRadius));
        
        double sensor1A = -(robotA + sensorAngle);        
        double sensor2A = -(robotA - sensorAngle);
        
        double sensor1X = X + robotRadius * Math.sin(Math.toRadians(sensor1A));
        double sensor1Y = Y - robotRadius * (1-Math.cos(Math.toRadians(sensor1A)));
        double sensor2X = X + robotRadius * Math.sin(Math.toRadians(sensor2A));
        double sensor2Y = Y - robotRadius * (1-Math.cos(Math.toRadians(sensor2A))) ;
        
        double[] sensors = new double[2];
        double sensor1Value = getUnderSensorReading(sensor1X, sensor1Y, linePoints);
        double sensor2Value = getUnderSensorReading(sensor2X, sensor2Y, linePoints);
        
        sensors[0] = sensor1Value;
        sensors[1] = sensor2Value;
        
        return sensors;        
    }
    
    public double getUnderSensorReading(double sensorX, double sensorY, ArrayList<Point> linePoints) {
        double sensorNoise = 100;
        for(int i = 0; i < linePoints.size(); i+=2) {
            double x1 = linePoints.get(i).x;
            double y1 = linePoints.get(i).y;
            double x2 = linePoints.get(i+1).x;
            double y2 = linePoints.get(i+1).y;
            double centerX = 0;
            double centerY = 0;
            double arcAngle = 0;
            double width = x1-x2;
            if(width < 0)
            {
                centerX = x2;
                centerY = y2 - KheperaSimulator.arcRadius;
                arcAngle = 270;
            }
            else if(width > 0)
            {
                centerX = x2;
                centerY = y2 + KheperaSimulator.arcRadius;
                arcAngle = 90;
            }
            double height = y1-y2;
            if(height < 0)
            {
                centerX = x2 + KheperaSimulator.arcRadius;
                centerY = y2;
                arcAngle = 0;
            }
            else if(height > 0)
            {
                centerX = x2 - KheperaSimulator.arcRadius;
                centerY = y2;
                arcAngle = 180;
            }
            double lineDistance = Double.MAX_VALUE;
            
            double angle = Math.toDegrees(Math.atan2(sensorX - centerX, sensorY - centerY));
            if(angle < 0)
                angle = -angle;
            
            if(angle<arcAngle+90 && angle>arcAngle-90) //calculating diustance on curve
            {
                double arcDistance = Math.sqrt(Math.pow(sensorX - centerX, 2) + Math.pow(sensorY - centerY, 2));
                lineDistance = Math.abs(arcDistance - KheperaSimulator.arcRadius);
            }
            else //Calculating distance on line
            {
                if(width == 0)
                {
                    if((sensorY<=y1 && sensorY>=y2) || (sensorY<=y2 && sensorY>=y1))
                    {
                        lineDistance = Math.abs(x2 - sensorX);
                    }
                }
                if(height == 0)
                {
                    if((sensorX<=x1 && sensorX>=x2) || (sensorX<=x2 && sensorX>=x1))
                    {
                        lineDistance = Math.abs(y2 - sensorY);
                    }
                }
            }

            if(lineDistance < lineRadius)
            {                
                //return Math.max(Math.min(1-(lineDistance + (new Random().nextDouble() - 0.5) * sensorNoise * 2) / (lineRadius), 1), 0);
                //return new Random().nextInt(100) + 2000;
                return 0;
            }  
            
        }
        
        //return new Random().nextInt(90) + 3900;
        return 4000;
    }

    double distance(double[] pointA, double[] pointB) {
        double d1 = pointA[0] - pointB[0];
        double d2 = pointA[1] - pointB[1];

        return Math.sqrt(d1 * d1 + d2 * d2);
    }

    public static Pair<Double, Double> rotateXY(double x, double y, double angle) {
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return new Pair(cos * x - sin * y, sin * x + cos * y);
    }

    public static double formatAngle(double angle) {
        while (angle > 360) {
            angle -= 360;
        }

        while (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public static int getQuadrant(double x, double y) {
        if (x >= 0) {
            if (y >= 0)
                return 4;
            else
                return 3;
        } else {
            if (y >= 0)
                return 1;
            else
                return 2;
        }
    }

    public static double NormalizeZeroOne(double val, double low, double high) {
        return (val - low) / (high - low);
    }

    public static int deNormalizeZeroOne(double val, double low, double high) {
        return (int) (val * (high-low) + low);
    }
}
