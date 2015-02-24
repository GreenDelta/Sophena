package sophena.model;

public final class Statistics {

	private Statistics() {
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

}
