package sophena.calc.biogas.eblocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.calc.biogas.TestPlant;

public class PriceMarksTest {

	@Test
	public void marksTheHoursWithTheHighestPrices() {
		// the window [0, 38) contains 4 hours with a price of 100 and 34 hours
		// with a price of 10. 30 hours are marked, so 4 hours with a price of
		// 10 are not marked. Hours with the same price are marked from the
		// earliest to the latest.
		var plant = TestPlant.of(1600, 4, PriceMarksTest::peakAt6);
		var window = Window.of(State.initial(plant));
		var marks = PriceMarks.of(plant, window, window.maxRunHours());

		assertEquals(30, markCount(marks, window));
		for (int hour = 6; hour <= 9; hour++) {
			assertTrue("the peak hour " + hour + " must be marked",
				marks.contains(hour));
		}
		assertTrue(marks.contains(29));
		assertFalse(marks.contains(30));
	}

	@Test
	public void marksTheEarlierHourWhenThePricesAreEqual() {
		var plant = TestPlant.of(1600, 4);
		var window = Window.of(State.initial(plant));
		var marks = PriceMarks.of(plant, window, 5);

		assertEquals(5, markCount(marks, window));
		for (int hour = 0; hour < 5; hour++) {
			assertTrue("hour " + hour + " must be marked", marks.contains(hour));
		}
		assertFalse(marks.contains(5));
	}

	@Test
	public void marksHoursWithABlockedFeedInLast() {
		// the window [0, 38) has 28 hours with a price of 10 and 10 hours with
		// a blocked feed-in. 30 hours are marked, so 2 of the blocked hours are
		// marked as well. The penalty is subtracted from the price, so these
		// are the peak hours 6 and 7 with -900 instead of the hours 0 to 5 with
		// -990 ct/kWh.
		var plant = TestPlant.of(1600, 4, PriceMarksTest::peakAt6);
		TestPlant.blockFeedIn(plant, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		var window = Window.of(State.initial(plant));
		var marks = PriceMarks.of(plant, window, window.maxRunHours());

		assertEquals(30, markCount(marks, window));
		for (int hour = 10; hour <= 37; hour++) {
			assertTrue("hour " + hour + " must be marked", marks.contains(hour));
		}
		assertTrue("the blocked hours with the highest value must be marked",
			marks.contains(6));
		assertTrue(marks.contains(7));
		assertFalse("the other blocked hours must not be marked",
			marks.contains(8));
		assertFalse(marks.contains(0));
	}

	@Test
	public void doesNotMarkHoursOutsideOfTheWindow() {
		var plant = TestPlant.of(1600, 4);
		var window = Window.of(State.initial(plant));
		var marks = PriceMarks.of(plant, window, 30);

		assertFalse(marks.contains(-1));
		assertFalse(marks.contains(window.emptyHour()));
	}

	/// A price of 100 ct/kWh in the hours 6 to 9 and 10 ct/kWh in all other
	/// hours.
	private static double peakAt6(int hour) {
		return hour >= 6 && hour <= 9 ? 100 : 10;
	}

	private static int markCount(PriceMarks marks, Window window) {
		int count = 0;
		for (int h = window.startHour(); h < window.emptyHour(); h++) {
			if (marks.contains(h)) {
				count++;
			}
		}
		return count;
	}
}
