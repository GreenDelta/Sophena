package sophena.math;

import sophena.model.HeatNet;
import sophena.model.Project;

public class CalculateModules {

	public static int getCount(double area, double collectorArea) {
			return (int) Math.floor(area / collectorArea);
	}
}
