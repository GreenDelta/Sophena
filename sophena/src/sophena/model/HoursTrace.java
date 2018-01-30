package sophena.model;

/**
 * Arrays with elements mapped to annual hours are one of the key data
 * structures in Sophena. This class provides utilities for working with such
 * arrays.
 */
public class HoursTrace {

	public static final int[] DAYS_IN_MONTH = {
			31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	private HoursTrace() {
	}

	public static int[] getDayIntervalIndex() {
		// TODO: not yet implemented
		// see ProjectLoad
		return new int[] { -1, -1 };
	}

	public static void applyInterval(double[] trace, int[] interval,
			TraceFunction fn) {
		if (trace == null || interval == null || interval.length < 2)
			return;
		int start = interval[0];
		int end = interval[1];
		int max = trace.length - 1;
		if (start < 0 || start > max || end < 0 || end > max)
			return;
		if (start < end) {
			for (int i = start; i <= end; i++) {
				trace[i] = fn.apply(trace[i], i);
			}
		} else {
			for (int i = 0; i < Stats.HOURS; i++) {
				if (i >= start || i <= end) {
					trace[i] = fn.apply(trace[i], i);
				}
			}
		}
	}

	@FunctionalInterface
	public interface TraceFunction {
		double apply(double old, int idx);
	}
}
