package Code;

import java.util.Random;

public class Noise 
{
	public static double meanX = 0;
	public static double meanY = 0;
	public static double meanA = 0;
	public static double varianceX = 0.7838724512;
	public static double varianceY = 0.9704673903;
	public static double varianceA = 30.;

//	public static double varianceX = 0;
//	public static double varianceY = 0;
//	public static double varianceA = 0;

	public static double getXNoise()
	{
		Random r = new Random();
		return r.nextGaussian() * Math.sqrt(varianceX) + meanX;
	}

	public static double getYNoise()
	{
		Random r = new Random();
		return r.nextGaussian() * Math.sqrt(varianceY) + meanY;
	}

	public static double getANoise()
	{
		Random r = new Random();
		return r.nextGaussian() * Math.sqrt(varianceA) + meanA;
	}	
}
