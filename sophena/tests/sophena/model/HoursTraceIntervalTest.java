package sophena.model;

import static org.junit.Assert.assertEquals;

import java.time.MonthDay;

import org.junit.Test;

public class HoursTraceIntervalTest {

	@Test
	public void testFullRange() {
		TimeInterval time = new TimeInterval();
		time.start = MonthDay.of(1, 1).toString();
		time.end = MonthDay.of(12, 31).toString();
		int[] interval = HoursTrace.getDayInterval(time);
		assertEquals(0, interval[0]);
		assertEquals(Stats.HOURS - 1, interval[1]);

		double[] data = new double[Stats.HOURS];
		HoursTrace.applyInterval(data, interval, (d, i) -> 1.0);
		for (int i = 0; i < Stats.HOURS; i++) {
			assertEquals(1.0, data[i], 1e-16);
		}
	}

	@Test
	public void testInnerRange() {
		TimeInterval time = new TimeInterval();
		time.start = MonthDay.of(8, 7).toString();
		time.end = MonthDay.of(9, 3).toString();
		int[] interval = HoursTrace.getDayInterval(time);
		assertEquals(5232, interval[0]);
		assertEquals(5903, interval[1]);

		double[] data = new double[Stats.HOURS];
		HoursTrace.applyInterval(data, interval, (d, i) -> 1.0);
		for (int i = 0; i < Stats.HOURS; i++) {
			if (i >= 5232 && i <= 5903) {
				assertEquals(1.0, data[i], 1e-16);
			} else {
				assertEquals(0.0, data[i], 1e-16);
			}
		}
	}

	@Test
	public void testOuterRange() {
		TimeInterval time = new TimeInterval();
		time.start = MonthDay.of(12, 24).toString();
		time.end = MonthDay.of(1, 14).toString();
		int[] interval = HoursTrace.getDayInterval(time);
		assertEquals(8568, interval[0]);
		assertEquals(335, interval[1]);

		double[] data = new double[Stats.HOURS];
		HoursTrace.applyInterval(data, interval, (d, i) -> 1.0);
		for (int i = 0; i < Stats.HOURS; i++) {
			if (i >= 8568 || i <= 335) {
				assertEquals(1.0, data[i], 1e-16);
			} else {
				assertEquals(0.0, data[i], 1e-16);
			}
		}
	}
}
