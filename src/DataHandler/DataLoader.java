package DataHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static List<DataPoint> loadData(String filePath) {
        List<DataPoint> dataPoints = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip the header line
                    continue;
                }

                String[] values = line.split(",");
                if (values.length == 7) {
                    double posX = Double.parseDouble(values[1]);
                    double posY = Double.parseDouble(values[2]);
                    double posZ = Double.parseDouble(values[3]);
                    double velX = Double.parseDouble(values[4]);
                    double velY = Double.parseDouble(values[5]);
                    double velZ = Double.parseDouble(values[6]);

                    dataPoints.add(new DataPoint(posX, posY, posZ, velX, velY, velZ));
                }
            }
            System.out.println("Data loaded successfully from " + filePath + " with " + dataPoints.size() + " data points.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataPoints;
    }
}
