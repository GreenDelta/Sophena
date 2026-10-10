package sophena.calc.biogas;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import sophena.calc.biogas.eblocks.EblockSearch;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlantSettings;

/// Tests how the two algorithms are integrated into `BiogasRuntimeResult`.
public class BiogasPlantResultTest {

	@Test
	public void usesBlockAlgByDefault() {
		var plant = TestPlant.of(1600, 4);
		var res = BiogasRuntimeResult.calculate(plant);
		if (res.isError())
			fail(res.error());
		var result = res.value();

		assertEquals(BiogasAlgorithm.BLOCKS, BiogasAlgorithm.DEFAULT);
		assertArrayEquals(
			EblockSearch.runFlags(plant).orElseThrow(),
			result.runFlags());
		assertEquals(Stats.HOURS, result.runFlags().length);
		assertEquals(BiogasPlants.effectiveGasStorageSizeOf(plant),
			result.gasStorageSize(), 1e-10);

		// the profile is used by the charts of the plant editor
		assertEquals(TestPlant.GAS_PER_HOUR,
			result.biogasProfile().volumeAt(0), 1e-10);
	}

	@Test
	public void testRunHours() {
		var plant = TestPlant.of(1600, 4);
		var res = BiogasRuntimeResult.calculate(plant, BiogasAlgorithm.BLOCKS);
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
		var hours = BiogasRuntimeResult.calculate(plant, BiogasAlgorithm.HOURS)
			.value()
			.runFlags();
		assertFalse("the algorithms must produce different run hours",
			Arrays.equals(flags, hours));
		assertTrue("the hour based algorithm produces more blocks",
			blocksOf(hours).size() > blocks.size());
	}

	@Test
	public void returnsAnErrorWhenThePlantCannotBeCalculated() {
		// the storage of 200 m3 cannot hold a block of 3 hours
		var small = TestPlant.of(200, 3);
		var res = BiogasRuntimeResult.calculate(small, BiogasAlgorithm.BLOCKS);
		assertTrue(res.isError());
		assertTrue("unexpected message: " + res.error(),
			res.error().contains("minimum runtime"));

		// the hour based algorithm does not check the storage against the
		// minimum runtime, it only needs a plant that can be calculated
		assertTrue(BiogasRuntimeResult.calculate(small, BiogasAlgorithm.HOURS).isOk());

		var noBoiler = TestPlant.of(1600, 4);
		noBoiler.boilers.clear();
		assertTrue(BiogasRuntimeResult.calculate(noBoiler).isError());
		assertTrue(BiogasRuntimeResult.calculate(noBoiler, BiogasAlgorithm.BLOCKS).isError());
	}

	@Test
	public void returnsAnErrorWithoutAPlantOrAnAlgorithm() {
		assertTrue(BiogasRuntimeResult.calculate(null).isError());
		assertTrue(BiogasRuntimeResult.calculate(
			TestPlant.of(1600, 4), null).isError());
	}

	@Test
	public void testStorageProfile() {
		var plant = TestPlant.of(1600, 4);
		var res = BiogasRuntimeResult.calculate(plant, BiogasAlgorithm.BLOCKS);
		if (res.isError())
			fail(res.error());
		var result = res.value();

		var storage = result.storageProfile();
		assertEquals(Stats.HOURS, storage.length);
		double size = result.gasStorageSize();
		assertEquals(
			BiogasPlants.effectiveGasStorageSizeOf(plant), size, 1e-10);

		double max = 0;
		for (int h = 0; h < storage.length; h++) {
			assertTrue("negative storage at hour " + h, storage[h] >= 0);
			assertTrue("storage above its size at hour " + h,
				storage[h] <= size + 1e-9);
			max = Math.max(max, storage[h]);
		}
		// the storage is filled up to its size at some point in the year
		assertTrue("the storage must fill up", max > 0);
	}

	@Test
	public void heatLossesAreSubtractedFromTheProducerProfile() {
		var plant = TestPlant.of(1600, 4);
		plant.boilers.getFirst().boiler.maxPower = 600;
		plant.settings = BiogasPlantSettings.createDefault(null);

		plant.settings.heatLoss = 0;
		var without = BiogasRuntimeResult.calculate(plant)
			.orElseThrow().producerProfile();

		plant.settings.heatLoss = 20;
		var withLosses = BiogasRuntimeResult.calculate(plant)
			.orElseThrow().producerProfile();

		assertTrue(Stats.sum(without.maxPower)
			> Stats.sum(withLosses.maxPower));
	}

	@Test
	public void testProducerProfile() {
		var plant = TestPlant.of(1600, 4);
		// the test plant has no thermal power defined, so we set it here: a
		// producer profile is only created when the plant has a thermal power
		plant.boilers.getFirst().boiler.maxPower = 600;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		var stored = result.producerProfile();
		assertNotNull(stored);
		assertEquals(Stats.HOURS, stored.maxPower.length);

		// the stored profile was created with the default temperature
		var expected = result.asProducerProfile(
			BiogasRuntimeResult.PRODUCER_TEMPERATURE);
		assertArrayEquals(expected.maxPower, stored.maxPower, 1e-9);
		assertArrayEquals(expected.temperaturLevel, stored.temperaturLevel, 1e-9);

		// a profile with another temperature can still be created
		var other = result.asProducerProfile(90);
		assertArrayEquals(stored.maxPower, other.maxPower, 1e-9);
		int h = 0;
		while (h < Stats.HOURS && !result.runFlags()[h]) {
			h++;
		}
		assertTrue("the plant must run at some hour", h < Stats.HOURS);
		assertEquals(90, other.temperaturLevel[h], 1e-9);
		assertEquals(600, other.maxPower[h], 1e-9);
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
