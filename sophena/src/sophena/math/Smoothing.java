package sophena.math;

import sophena.Defaults;
import sophena.calc.ProjectLoad;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.Stats;

public class Smoothing {

	/**
	 * Get the smoothing factor of the project. A default factor is calculated based
	 * on the project data.
	 */
	public static double getFactor(Project project) {
		if (project == null || project.heatNet == null)
			return 0;
		var net = project.heatNet;
		if (net.smoothingFactor != null)
			return net.smoothingFactor;
		var maxLoad = ProjectLoad.getMax(project);
		var fsi = net.simultaneityFactor;
		var rawCurve = ProjectLoad.getRawCurve(project);
		var rawMax = Stats.max(rawCurve);
		if (rawMax == 0)
			return 0;
		var fsiEstimated = 0.9 * maxLoad * fsi / rawMax;
		var countEstimated = Math.round(20
				* Defaults.SMOOTHING_FACTOR * (1 - fsiEstimated)
				* Math.pow(2, (10 * (1 - fsiEstimated))));
		double factor = countEstimated / (20 * (1 - fsi) * Math.pow(2, 10 * (1 - fsi)));
		return factor < 0 ? 0 : factor;
	}

	/**
	 * Returns the number of items that should be included in the moving average
	 * calculation based on the projects' simultaneity factor.
	 */
	public static int getCount(Project project) {
		if (project == null || project.heatNet == null)
			return 0;
		HeatNet net = project.heatNet;
		double sm = getFactor(project);
		double si = net.simultaneityFactor;
		return (int) Math.round(20 * sm * (1 - si) * Math.pow(2, (10 * (1 - si))));
	}

	/**
	 * Smoothes the given curve (from a load profile) by calculating the moving
	 * average with the given number of elements for each element in the curve (see
	 * also https://en.wikipedia.org/wiki/Moving_average).
	 */
	public static double[] on(double[] curve, int n) {
		if (curve == null)
			return null;
		int band = n / 2;
		double[] smoothCurve = new double[curve.length];
		if (band == 0) {
			System.arraycopy(curve, 0, smoothCurve, 0, curve.length);
			return smoothCurve;
		}
		double count = 2 * band + 1;
		for (int i = 0; i < curve.length; i++) {
			double sum = 0;
			for (int j = i - band; j <= i + band; j++) {
				sum += curve[circularIndex(j, curve.length)];
			}
			smoothCurve[i] = sum / count;
		}
		return smoothCurve;
	}

	public static int circularIndex(int value, int length) {
		if (value < 0)
			return circularIndex(length + value, length);
		if (value < length)
			return value;
		return circularIndex(value % length, length);
	}

}
