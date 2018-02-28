package sophena.model;

import java.time.MonthDay;

public class MonthDayHour {

	private final MonthDay mday;
	private final int hour;

	public MonthDayHour(MonthDay mday, int hour) {
		this.mday = mday;
		this.hour = hour;
	}

	public static MonthDayHour of(int month, int day, int hour) {
		return new MonthDayHour(MonthDay.of(month, day), hour);
	}

	public static MonthDayHour parse(String s) {
		if (s == null)
			return MonthDayHour.of(1, 1, 0);
		String[] parts = s.split(":");
		MonthDay mday = MonthDay.parse(parts[0]);
		int hour;
		if (parts.length < 2) {
			hour = 0;
		} else {
			hour = Integer.parseInt(parts[1], 10);
		}
		return new MonthDayHour(mday, hour);
	}

	public MonthDay getMonthDay() {
		return mday;
	}

	public int getMonth() {
		return mday == null ? 1 : mday.getMonthValue();
	}

	public int getDay() {
		return mday == null ? 1 : mday.getDayOfMonth();
	}

	public int getHour() {
		return hour;
	}

	@Override
	public String toString() {
		String hh = String.format(":%02d", hour);
		if (mday == null)
			return "--01-01" + hh;
		return mday.toString() + hh;
	}

}
