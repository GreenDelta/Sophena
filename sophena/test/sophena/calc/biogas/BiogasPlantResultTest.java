package sophena.calc.biogas;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import sophena.calc.biogas.ehours.EhourSearch;
import sophena.model.Stats;

/// Tests how the two algorithms are integrated into `BiogasPlantResult`.
public class BiogasPlantResultTest {

	@Test
	public void usesTheHourBasedAlgorithmByDefault() {
		var plant = TestPlant.of(1600, 4);
		var res = BiogasPlantResult.calculate(plant);
		if (res.isError())
			fail(res.error());
		var result = res.value();

		assertEquals(BiogasAlgorithm.HOURS, BiogasAlgorithm.DEFAULT);
		assertArrayEquals(
			EhourSearch.runFlags(plant).orElseThrow(),
			result.runFlags());
		assertEquals(Stats.HOURS, result.runFlags().length);
		assertEquals(plant.gasStorageSize, result.gasStorageSize(), 1e-10);

		// the profile is used by the charts of the plant editor
		assertEquals(TestPlant.GAS_PER_HOUR,
			result.biogasProfile().volumeAt(0), 1e-10);
	}

	@Test
	public void calculatesTheRunHoursWithTheBlockAlgorithm() {
		var plant = TestPlant.of(1600, 4);
		var res = BiogasPlantResult.calculate(plant, BiogasAlgorithm.BLOCKS);
		if (res.isError())
			fail(res.error());
		var flags = res.value().runFlags();

		// the block search always runs the plant in blocks of at least the
		// minimum runtime
		var blocks = blocksOf(flags);
		assertFalse("the block search must find blocks", blocks.isEmpty());
		for (var block : blocks) {
			assertTrue("a block of the block search has " + block.length()
					+ " hours, the minimum runtime is " + plant.minimumRuntime,
				block.length() >= plant.minimumRuntime);
		}

		// the hour based algorithm produces many blocks that are shorter than
		// the minimum runtime, so the results differ
		var hours = BiogasPlantResult.calculate(plant).value().runFlags();
		assertFalse("the algorithms must produce different run hours",
			Arrays.equals(flags, hours));
		assertTrue("the hour based algorithm produces more blocks",
			blocksOf(hours).size() > blocks.size());
	}

	@Test
	public void returnsAnErrorWhenThePlantCannotBeCalculated() {
		// the storage of 200 m3 cannot hold a block of 3 hours
		var small = TestPlant.of(200, 3);
		var res = BiogasPlantResult.calculate(small, BiogasAlgorithm.BLOCKS);
		assertTrue(res.isError());
		assertTrue("unexpected message: " + res.error(),
			res.error().contains("minimum runtime"));

		// the hour based algorithm does not check the storage against the
		// minimum runtime, it only needs a plant that can be calculated
		assertTrue(BiogasPlantResult.calculate(small, BiogasAlgorithm.HOURS).isOk());

		var noBoiler = TestPlant.of(1600, 4);
		noBoiler.boilers.clear();
		assertTrue(BiogasPlantResult.calculate(noBoiler).isError());
		assertTrue(BiogasPlantResult.calculate(noBoiler, BiogasAlgorithm.BLOCKS).isError());
	}

	@Test
	public void returnsAnErrorWithoutAPlantOrAnAlgorithm() {
		assertTrue(BiogasPlantResult.calculate(null).isError());
		assertTrue(BiogasPlantResult.calculate(
			TestPlant.of(1600, 4), null).isError());
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
