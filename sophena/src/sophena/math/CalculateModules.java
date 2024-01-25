package sophena.math;

public class CalculateModules {

	public static int getCount(double area, double collectorArea) {
		if (collectorArea <= 0)
			return 0;
		return (int) Math.floor(area / collectorArea);
	}
}
