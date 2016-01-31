package sophena.model;

public final class Stats {

	/**
	 * The number of annual hours (365 * 24).
	 */
	public static final int HOURS = 8760;

	private Stats() {
	}

	public static double sum(double[] vals) {
		double sum = 0;
		for (double v : vals)
			sum += v;
		return sum;
	}

	public static double max(double[] vals) {
		if (vals == null || vals.length == 0)
			return 0;
		double max = vals[0];
		for (int i = 1; i < vals.length; i++) {
			max = Math.max(max, vals[i]);
		}
		return max;
	}

	public static double min(double[] vals) {
		if (vals == null || vals.length == 0)
			return 0;
		double min = vals[0];
		for (int i = 1; i < vals.length; i++) {
			min = Math.min(min, vals[i]);
		}
		return min;
	}

	public static int min(int[] vals) {
		if (vals == null || vals.length == 0)
			return 0;
		int min = vals[0];
		for (int i = 1; i < vals.length; i++) {
			min = Math.min(min, vals[i]);
		}
		return min;
	}

	public static int max(int[] vals) {
		if (vals == null || vals.length == 0)
			return 0;
		int max = vals[0];
		for (int i = 1; i < vals.length; i++) {
			max = Math.max(max, vals[i]);
		}
		return max;
	}

	public static int nextStep(double value, int step) {
		if (step == 0)
			return 0;
		double s = step;
		double u = (value + s) % s;
		double l = s - u;
		return (int) Math.ceil(value + l);
	}

	/**
	 * A null- and overflow-safe method for getting the ith element of the given
	 * array.
	 */
	public static double get(double[] array, int i) {
		if (array == null)
			return 0;
		if (i < 0 || i >= array.length)
			return 0;
		else
			return array[i];
	}

	/** Adds the values of the first array to the values of the second array */
	public static void add(double[] from, double[] to) {
		for (int i = 0; i < from.length && i < to.length; i++)
			to[i] += from[i];
	}

	public static double[] copy(double[] source) {
		if (source == null)
			return null;
		double[] copy = new double[source.length];
		System.arraycopy(source, 0, copy, 0, source.length);
		return copy;
	}

}
