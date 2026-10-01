package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

import sophena.calc.biogas.ehours.EhourSearch;

/// Tests the gas storage sizes that are derived from a biogas plant.
public class BiogasPlantsTest {

	@Test
	public void effectiveSizeOfANullPlantIsZero() {
		assertEquals(0, BiogasPlants.effectiveGasStorageSizeOf(null), 1e-10);
	}

	@Test
	public void effectiveSizeAtFullFillingLevelIsTheStorageSize() {
		var plant = TestPlant.of(1600, 4);
		plant.gasStorageFillingLevel = 100;
		assertEquals(1600, BiogasPlants.effectiveGasStorageSizeOf(plant), 1e-10);
	}

	@Test
	public void effectiveSizeScalesWithTheFillingLevel() {
		var plant = TestPlant.of(1600, 4);
		plant.gasStorageFillingLevel = 50;
		assertEquals(800, BiogasPlants.effectiveGasStorageSizeOf(plant), 1e-10);
		plant.gasStorageFillingLevel = 25;
		assertEquals(400, BiogasPlants.effectiveGasStorageSizeOf(plant), 1e-10);
	}

	@Test
	public void aFillingLevelOfZeroIsTreatedAsFull() {
		var plant = TestPlant.of(1600, 4);
		plant.gasStorageFillingLevel = 0;
		assertEquals(1600, BiogasPlants.effectiveGasStorageSizeOf(plant), 1e-10);
	}

	@Test
	public void aFillingLevelAboveHundredIsClampedToFull() {
		var plant = TestPlant.of(1600, 4);
		plant.gasStorageFillingLevel = 150;
		assertEquals(1600, BiogasPlants.effectiveGasStorageSizeOf(plant), 1e-10);
	}

	@Test
	public void effectiveSizeUsesTheDefaultSizeWhenNoSizeIsDefined() {
		var plant = TestPlant.of(1600, 4);
		plant.gasStorageSize = null;
		plant.gasStorageFillingLevel = 50;
		double def = BiogasPlants.defaultGasStorageSizeOf(plant);
		assertEquals(0.5 * def,
			BiogasPlants.effectiveGasStorageSizeOf(plant), 1e-10);
	}

	@Test
	public void aReducedFillingLevelShrinksTheStorageInTheCalculation() {
		var plant = TestPlant.of(1600, 4);

		plant.gasStorageFillingLevel = 100;
		var full = EhourSearch.runFlags(plant).orElseThrow();

		// 50 % of 1600 m3 is 800 m3, which cannot deliver the gas for a block
		// of the minimum runtime of 4 hours, so the plant never runs
		plant.gasStorageFillingLevel = 50;
		var half = EhourSearch.runFlags(plant).orElseThrow();

		assertFalse(Arrays.equals(full, half));
	}
}
