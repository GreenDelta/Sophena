package sophena.model;

import java.time.MonthDay;

import sophena.rcp.utils.Log;

/**
 * Arrays with elements mapped to annual hours are one of the key data
 * structures in Sophena. This class provides utilities for working with such
 * arrays.
 */
public class HoursTrace {

	public static final int[] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31,
			30, 31, 30, 31 };

	private HoursTrace() {
	}

	/**
	 * Returns the start and end index of an hours trace for the given time
	 * interval. It is expected that the given interval has a MonthDay format.
	 * The start index will be the index of the first hour and the end index the
	 * index of the last hour of the given days+months in an hours trace. If
	 * something went wrong [-1, -1] will be returned.
	 */
	public static int[] getDayInterval(TimeInterval time) {
		if (time == null)
			return new int[] { -1, -1 };
		try {
			MonthDay startDay = MonthDay.parse(time.start);
			MonthDay endDay = MonthDay.parse(time.end);
			int startHour = getFirstHour(startDay);
			int endHour = getFirstHour(endDay) + 23;
			return new int[] { startHour, endHour };
		} catch (Exception e) {
			Log.error(HoursTrace.class, "Failed to parse time span " + time, e);
			return new int[] { -1, -1 };
		}
	}

	/**
	 * Get the index of the first hour of the given month-day in an hours trace.
	 */
	public static int getFirstHour(MonthDay mday) {
		if (mday == null)
			return 0;

		// transform to 0-based indices
		int month = mday.getMonthValue() - 1;
		int day = mday.getDayOfMonth() - 1;
		int hour = 0;

		// add the days*hours of the months before
		for (int i = 0; i < month; i++) {
			hour += DAYS_IN_MONTH[i] * 24;
		}

		// add the hours of the days before
		if (day > 0) {
			hour += 24 * day;
		}
		return hour;
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

	public static void applyInterval(boolean[] trace, int[] interval,
			BooleanTraceFunction fn) {
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

	public interface BooleanTraceFunction {
		boolean apply(boolean old, int idx);
	}
}
