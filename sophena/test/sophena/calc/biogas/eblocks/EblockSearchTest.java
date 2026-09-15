package sophena.calc.biogas.eblocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import sophena.calc.biogas.TestPlant;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public class EblockSearchTest {

	@Test
	public void findsTheBestMinBlockAndExtendsIt() {
		// The window of the first block is [0, 38) and a block must start in
		// [0, 8]. The best block of 4 hours starts at the price peak in hour 6
		// with 4 * 100 = 400 ct/kWh, all other blocks have at most 310.
		//
		// After that the block grows to the left into the marked hours until
		// hour 3: the storage holds 200 m3 per filled hour, so it contains
		// 400 m3 = 1.6 hours of full load in hour 2, which is not enough for
		// the 8 hours that the block has at that point.
		//
		// Then it grows to the right until hour 13: a run of L hours needs
		// L + 0.25 hours of fuel and produces L * 0.8 hours of it, so it takes
		// 0.2 * L + 0.25 hours from the storage. From hour 3 the storage
		// contains 600 m3 = 2.39 hours of full load. A run of 10 hours takes
		// 2.27 hours from the storage and fits, a run of 11 hours takes 2.48.
		var plant = TestPlant.of(
			1600, 4, hour -> hour >= 6 && hour <= 9 ? 100 : 10);

		var first = firstBlock(plant);

		assertEquals(3, first.start().hour());
		assertEquals(10, first.length());
		assertEquals(13, first.end().hour());
	}

	@Test
	public void findBestMinBlockStartsAtTheHighestPrice() {
		// with a storage of 4000 m3 the storage is full after 20 hours, so the
		// candidates are the blocks that start in [0, 20]. The block with the
		// price peak wins over the blocks that only contain cheap hours.
		var plant = TestPlant.of(
			4000, 4, hour -> hour >= 6 && hour <= 9 ? 100 : 10);

		var block = findMinBlock(plant);

		assertEquals(6, block.start().hour());
		assertEquals(4, block.length());
	}

	@Test
	public void findBestMinBlockAvoidsHoursWithABlockedFeedIn() {
		// the same plant as above, but the feed-in of the peak hours is not
		// allowed. The penalty turns the peak into the worst hours of the
		// window, so the search starts at hour 2: this is the earliest hour
		// from which the storage can deliver a block of 4 hours. From hour 1
		// the storage contains 200 m3 = 0.8 hours of full load, which is less
		// than the 1.06 hours that the block needs.
		var plant = TestPlant.of(
			4000, 4, hour -> hour >= 6 && hour <= 9 ? 100 : 10);
		TestPlant.blockFeedIn(plant, 6, 7, 8, 9);

		assertEquals(2, findMinBlock(plant).start().hour());
	}

	@Test
	public void extendsABlockIntoTheMarkedHours() {
		// the marked hours of the window are the hours 6 to 29. The block
		// [6, 10) grows to the right until the storage is empty at hour 28 and
		// does not grow to the left because hour 5 is not marked.
		var plant = TestPlant.of(1600, 4);
		var window = Window.of(State.initial(plant));
		var marks = marksOf(window, 6, 30);

		var block = new EblockSearch(plant).extend(
			window, marks, stateAt(window, 6).getBlock(4));

		assertEquals(6, block.start().hour());
		assertEquals(22, block.length());
		assertEquals(28, block.end().hour());
	}

	@Test
	public void doesNotExtendABlockIntoUnmarkedHours() {
		// only the hours 6 to 11 are marked, so the block stops with hour 12
		var plant = TestPlant.of(1600, 4);
		var window = Window.of(State.initial(plant));
		var marks = marksOf(window, 6, 12);

		var block = new EblockSearch(plant).extend(
			window, marks, stateAt(window, 6).getBlock(4));

		assertEquals(6, block.start().hour());
		assertEquals(6, block.length());
		assertEquals(12, block.end().hour());
	}

	@Test
	public void createsBlocksThatCoverTheYear() {
		var plant = TestPlant.of(1600, 4);
		var res = EblockSearch.run(plant);
		if (res.isError())
			fail(res.error());
		var eblocks = res.value();
		var blocks = eblocks.blocks();

		assertFalse("the search must find blocks", blocks.isEmpty());
		int hours = 0;
		int lastHour = 0;
		for (var block : blocks) {
			assertTrue("a block has " + block.length() + " hours but the"
				+ " minimum runtime is " + plant.minimumRuntime,
				block.length() >= plant.minimumRuntime);
			assertTrue("the blocks must be ordered and must not overlap",
				block.start().hour() >= lastHour);
			lastHour = block.end().hour();
			assertTrue("a block must not end after the year",
				lastHour <= Stats.HOURS);
			hours += block.length();
		}

		// the run flags contain exactly the hours of the blocks
		var flags = eblocks.runFlags();
		assertEquals(Stats.HOURS, flags.length);
		int flagged = 0;
		for (boolean flag : flags) {
			if (flag) {
				flagged++;
			}
		}
		assertEquals(hours, flagged);
	}

	@Test
	public void stopsAtTheEndOfTheYear() {
		// near the end of the year the storage cannot be filled in the
		// remaining hours anymore: the window from hour 8750 to the end of the
		// year contains a run of only 3 hours, but the plant has a minimum
		// runtime of 4 hours. The search stops there instead of failing and
		// the gas of the last hours is not used.
		var res = EblockSearch.run(TestPlant.of(1600, 4));
		if (res.isError())
			fail(res.error());

		var last = res.value().blocks().getLast();
		assertTrue("the last block must end in the year",
			last.end().hour() <= Stats.HOURS);
		assertTrue("the plant does not run until the end of the year",
			last.end().hour() < Stats.HOURS);
	}

	@Test
	public void stopsWithAnErrorWhenTheStorageIsTooSmall() {
		var res = EblockSearch.runFlags(TestPlant.of(200, 3));

		assertTrue(res.isError());
		assertTrue("unexpected message: " + res.error(),
			res.error().contains("minimum runtime"));
	}

	@Test
	public void findBestMinBlockStopsWhenNoBlockFitsIntoTheWindow() {
		// the pre-check already rejects this plant, but a search step must also
		// not continue with an undefined state
		var plant = TestPlant.of(200, 3);
		var window = Window.of(State.initial(plant));
		assertEquals(2, window.maxRunHours());

		var res = new EblockSearch(plant).findBestMinBlock(window);

		assertTrue(res.isError());
		assertTrue("unexpected message: " + res.error(),
			res.error().contains("minimum runtime"));
	}

	private static Block firstBlock(BiogasPlant plant) {
		var res = EblockSearch.run(plant);
		if (res.isError())
			fail(res.error());
		return res.value().blocks().getFirst();
	}

	private static Block findMinBlock(BiogasPlant plant) {
		var window = Window.of(State.initial(plant));
		var res = new EblockSearch(plant).findBestMinBlock(window);
		if (res.isError())
			fail(res.error());
		return res.value();
	}

	/// The state of the window at the given hour, which must lie between `t_0`
	/// and `t_f`.
	private static State stateAt(Window window, int hour) {
		var state = window.start();
		while (state.hour() < hour) {
			state = state.fillNext();
		}
		return state;
	}

	/// Marks the given hours of the window.
	private static PriceMarks marksOf(Window window, int from, int to) {
		var marked = new boolean[window.emptyHour() - window.startHour()];
		for (int h = from; h < to; h++) {
			marked[h - window.startHour()] = true;
		}
		return new PriceMarks(window.startHour(), marked);
	}
}
