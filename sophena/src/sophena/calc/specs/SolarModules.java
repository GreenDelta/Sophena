package sophena.calc.specs;

public class SolarModules {

	private SolarModules() {
	}

	public static int getCount(double area, double collectorArea) {
		if (collectorArea <= 0)
			return 0;
		return (int) Math.ceil(area / collectorArea);
	}
}
