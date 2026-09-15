package sophena.calc.biogas.ehours;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import sophena.calc.biogas.TestPlant;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Tests the hour based algorithm that selects the run hours of a biogas
/// plant. This is the algorithm that was used before the block search was
/// added, so these tests describe its behavior:
///
/// - `ElectricityPriceSchedule` marks the hours of every day in which the
///   storage of the plant could be emptied. The number of marked hours is
///   `hoursToEmpty` of the storage that is filled with the production of that
///   day, and the marked hours are the hours with the highest prices.
/// - The plant runs in a marked hour when the storage can deliver the gas for
///   the minimum runtime, and it runs also when the storage is full or when it
///   is already running and did not reach the minimum runtime.
/// - The plant stops when the storage is empty, also when it did not run for
///   the minimum runtime. Blocks that are shorter than the minimum runtime are
///   therefore normal in this algorithm, which is what the block based
///   algorithm (`sophena.calc.biogas.eblocks.EblockSearch`) fixes.
public class EhourSearchTest {

	@Test
	public void runsBlocksThatAreShorterThanTheMinimumRuntime() {
		var plant = TestPlant.of(1600, 4);
		var flags = run(plant);
		var blocks = blocksOf(flags);

		assertEquals(Stats.HOURS, flags.length);
		// the plant runs in 6617 of the 8760 hours, split into 1459 blocks
		assertEquals(6617, count(flags));
		assertEquals(1459, blocks.size());

		// the first block is only 3 hours long although the plant has a
		// minimum runtime of 4 hours: the storage is empty after that
		assertEquals(1, blocks.getFirst().start());
		assertEquals(3, blocks.getFirst().length());
		assertTrue("the old algorithm produces blocks that are shorter than"
				+ " the minimum runtime",
			blocks.getFirst().length() < plant.minimumRuntime);
		assertEquals(5, blocks.get(1).start());
		assertEquals(3, blocks.get(1).length());
	}

	@Test
	public void runsWhenTheStorageIsFullOrThePriceIsGood() {
		// the price peak is in the hours 18 to 23 of every day, but the
		// storage is already full in hour 7 (the production of 8 hours fills
		// it) and forces the plant to run then
		var plant = TestPlant.of(1600, 4,
			hour -> hour % 24 >= 18 ? 100 : 10);
		var flags = run(plant);
		var blocks = blocksOf(flags);

		assertEquals(6619, count(flags));
		assertEquals(7, blocks.getFirst().start());
		assertEquals(4, blocks.getFirst().length());
		assertEquals(12, blocks.get(1).start());
	}

	@Test
	public void runsTheMarkedHoursWhenTheStorageIsNeverFull() {
		// the storage of 1_800_000 m3 is never full, so `hoursToEmpty` of the
		// storage that is filled with the production of a day (24 * 200 m3 =
		// 4800 m3 = 19.14 hours of full load) is the only limit: 19 hours of
		// every day are marked and the plant starts to run in these hours as
		// soon as the storage can deliver the minimum runtime
		var plant = TestPlant.of(1_800_000, 4);
		var flags = run(plant);
		var blocks = blocksOf(flags);

		assertEquals(6890, count(flags));
		assertEquals(369, blocks.size());
		assertEquals(1, blocks.getFirst().start());
		assertEquals(3, blocks.getFirst().length());
		// one of the first blocks is even shorter than a run of the minimum
		// runtime
		assertEquals(2, blocks.get(2).length());
	}

	/// The run hours of the hour based algorithm.
	private static boolean[] run(BiogasPlant plant) {
		return EhourSearch.runFlags(plant).orElseThrow();
	}

	private static int count(boolean[] flags) {
		int count = 0;
		for (boolean flag : flags) {
			if (flag) {
				count++;
			}
		}
		return count;
	}

	/// The blocks of consecutive hours in which the plant runs.
	private static List<TestBlock> blocksOf(boolean[] flags) {
		var blocks = new ArrayList<TestBlock>();
		int start = -1;
		for (int h = 0; h < flags.length; h++) {
			if (flags[h] && start < 0) {
				start = h;
			} else if (!flags[h] && start >= 0) {
				blocks.add(new TestBlock(start, h - start));
				start = -1;
			}
		}
		if (start >= 0) {
			blocks.add(new TestBlock(start, flags.length - start));
		}
		return blocks;
	}

	private record TestBlock(int start, int length) {
	}
}
