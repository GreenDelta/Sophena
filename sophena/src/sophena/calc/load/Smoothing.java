package sophena.calc.load;

import java.util.Arrays;

import sophena.Defaults;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.Stats;

public class Smoothing {

	/// Get the smoothing factor of the project. If not defined, a default factor
	/// is calculated based on the project data.
	public static double getFactor(Project project) {
		if (project == null || project.heatNet == null)
			return 0;
		var net = project.heatNet;
		if (net.smoothingFactor != null)
			return net.smoothingFactor;
		var fsi = net.simultaneityFactor;
		if (fsi >= 1.0) // full simultaneity -> no smoothing
			return 0;

		var maxLoad = ProjectLoad.getMax(project);
		var rawCurve = ProjectLoad.getRawCurve(project);
		var rawMax = Stats.max(rawCurve);
		if (maxLoad == 0 || rawMax == 0 || rawMax < (0.9 * maxLoad))
			return 0;

		var fsiEstimated = 0.9 * maxLoad * fsi / rawMax;
		var countEstimated = Math.round(20
				* Defaults.SMOOTHING_FACTOR * (1 - fsiEstimated)
				* Math.pow(2, (10 * (1 - fsiEstimated))));
		double factor = countEstimated / (20 * (1 - fsi) * Math.pow(2, 10 * (1 - fsi)));
		return factor < 0 || !Double.isFinite(factor)
				? 0
				: factor;
	}

	/**
	 * Returns the number of items that should be included in the moving average
	 * calculation based on the projects' simultaneity factor.
	 */
	public static int getCount(Project project) {
		if (project == null || project.heatNet == null)
			return 0;
		HeatNet net = project.heatNet;
		double si = net.simultaneityFactor;
		if (si >= 1.0) // full simultaneity -> no smoothing
			return 0;
		double sm = getFactor(project);
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
		int len = curve.length;
		int band = n / 2;
		double[] smoothCurve = new double[len];
		if (band <= 0 || len == 0) {
			System.arraycopy(curve, 0, smoothCurve, 0, len);
			return smoothCurve;
		}

		// A window that covers the whole curve (one year) means that we average
		// over the full year: the result is just the mean of the curve. Larger
		// windows would only count some values multiple times and are expensive to
		// calculate. Such windows do not occur with realistic inputs but, for
		// example, with a simultaneity factor close to 0, where the window size can
		// grow to more than 200.000 hours.
		long count = 2L * band + 1;
		if (count >= len) {
			Arrays.fill(smoothCurve, Stats.sum(curve) / len);
			return smoothCurve;
		}

		// Moving sum: for each element, add the value that enters the window
		// and remove the value that leaves it. We also track the number of
		// non-zero values in the window, so that a window with only zeros
		// results in exactly 0 and not in a small rounding error.
		double sum = 0;
		int nonZeros = 0;
		for (int j = -band; j <= band; j++) {
			double v = curve[circularIndexOf(j, len)];
			sum += v;
			if (v != 0) {
				nonZeros++;
			}
		}

		smoothCurve[0] = nonZeros > 0 ? sum / count : 0;
		for (int i = 1; i < len; i++) {
			double in = curve[circularIndexOf(i + band, len)];
			double out = curve[circularIndexOf(i - band - 1, len)];
			sum += in - out;
			if (in != 0) {
				nonZeros++;
			}
			if (out != 0) {
				nonZeros--;
			}
			smoothCurve[i] = nonZeros > 0 ? sum / count : 0;
		}
		return smoothCurve;
	}

	public static int circularIndexOf(int value, int length) {
		return Math.floorMod(value, length);
	}

}
