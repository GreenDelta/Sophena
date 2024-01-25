package sophena.math;

public class CalculateModules {

	public static int getCount(double area, double collectorArea) {
			return (int) Math.floor(area / collectorArea);
	}
}
