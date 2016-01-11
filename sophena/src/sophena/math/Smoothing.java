package sophena.math;

public class Smoothing {

	/**
	 * Smoothes the given curve (from a load profile) by calculating the moving
	 * average with the given number of elements for each element in the curve
	 * (see also https://en.wikipedia.org/wiki/Moving_average).
	 */
	public static double[] means(double[] curve, int n) {
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
		if (value >= 0 && value < length)
			return value;
		return circularIndex(value % length, length);
	}

}
