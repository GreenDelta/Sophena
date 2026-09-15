package sophena.calc.biogas.eblocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.model.Stats;

public class WindowTest {

	@Test
	public void windowFromTheInitialState() {
		var plant = TestPlant.of(1600, 4);
		var window = Window.of(State.initial(plant));

		assertEquals(0, window.startHour());
		// the storage takes the production of 8 hours: 8 * 200 = 1600 m3
		assertEquals(8, window.fillHour());
		// a full storage holds 7976 kWh = 6.38 hours of full load. Because the
		// plant produces 997 kWh in every hour in which it runs, it can run
		// much longer than that: it needs 253 kWh more than it produces per
		// hour, so the storage lasts for about 8 + 30 hours.
		assertEquals(38, window.emptyHour());
		assertEquals(30, window.maxRunHours());
		assertFalse(window.isEmpty());
	}

	@Test
	public void windowReservesTheFuelForTheRampDown() {
		// the storage of 200 m3 is full with the production of a single hour.
		// After the third hour of the run there are only 16 m3 = 82 kWh left,
		// which is not enough for the ramp-down (1250 * 0.125 = 156 kWh), so
		// the window ends one hour earlier.
		var plant = TestPlant.of(200, 2);
		var window = Window.of(State.initial(plant));

		assertEquals(1, window.fillHour());
		assertEquals(3, window.emptyHour());
		assertEquals(2, window.maxRunHours());
		assertFalse(window.isEmpty());

		// a window and the blocks of its states use the same limits
		assertNotNull(window.full().getBlock(2));
		assertNull(window.full().getBlock(3));
	}

	@Test
	public void windowWhenTheStorageNeverFills() {
		// the storage of 2_000_000 m3 is larger than the production of the
		// whole year (200 * 8760 = 1_752_000 m3), so the plant never has to run
		var plant = TestPlant.of(2_000_000, 4);
		var window = Window.of(State.initial(plant));

		assertEquals(Stats.HOURS, window.fillHour());
		assertEquals(Stats.HOURS, window.emptyHour());
		assertEquals(0, window.maxRunHours());
		assertTrue(window.isEmpty());
	}
}
