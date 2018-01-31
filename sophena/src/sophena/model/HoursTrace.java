package sophena.model;

import java.time.MonthDay;

import sophena.rcp.utils.Log;

/**
 * Arrays with elements mapped to annual hours are one of the key data
 * structures in Sophena. This class provides utilities for working with such
 * arrays.
 */
public class HoursTrace {

	public static final int[] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	private HoursTrace() {
	}

	/**
	 * Returns the start and end index of an hours trace for the given time
	 * interval. It is expected that the given interval has a MonthDay format. The
	 * start index will be the index of the first hour and the end index the index
	 * of the last hour of the given days+months in an hours trace. If something
	 * went wrong [-1, -1] will be returned.
	 */
	public static int[] getDayInterval(TimeInterval time) {
		if (time == null)
			return new int[] { -1, -1 };
		try {
			MonthDay startMD = MonthDay.parse(time.start);
			int startMonth = startMD.getMonthValue() - 1;
			int startDay = startMD.getDayOfMonth() - 1;
			MonthDay endMD = MonthDay.parse(time.end);
			int endMonth = endMD.getMonthValue() - 1;
			int endDay = endMD.getDayOfMonth() - 1;

			int start = 0;
			int end = 0;

			// add the days*hours of the months before
			int uMonth = Math.max(startMonth, endMonth);
			for (int month = 0; month < uMonth; month++) {
				if (month < startMonth) {
					start += DAYS_IN_MONTH[month] * 24;
				}
				if (month < endMonth) {
					end += DAYS_IN_MONTH[month] * 24;
				}
			}

			// add the hours of the days before
			// note that we have a zero based index (startDay = 1 -> 24 before)
			if (startDay > 0) {
				start += 24 * startDay;
			}
			if (endDay > 0) {
				end += 24 * endDay;
			}

			return new int[] { start, end + 23 };
		} catch (Exception e) {
			Log.error(HoursTrace.class, "Failed to parse time span " + time, e);
			return new int[] { -1, -1 };
		}
	}

	public static void applyInterval(double[] trace, int[] interval, TraceFunction fn) {
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
