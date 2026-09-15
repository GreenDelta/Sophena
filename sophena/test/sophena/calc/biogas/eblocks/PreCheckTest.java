package sophena.calc.biogas.eblocks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.calc.biogas.TestPlant;

public class PreCheckTest {

	@Test
	public void acceptsAPlantThatFitsTogether() {
		// the standard plant: a storage of 1600 m3 holds the gas of 8 hours
		assertFalse(PreCheck.validate(TestPlant.of(1600, 4)).isError());
		// the smallest storage that is checked in the tests: 200 m3 is enough
		// for a block of 2 hours but not for a longer one
		assertFalse(PreCheck.validate(TestPlant.of(200, 2)).isError());
	}

	@Test
	public void rejectsAStorageThatIsTooSmallForTheMinimumRuntime() {
		var res = PreCheck.validate(TestPlant.of(200, 3));

		assertTrue(res.isError());
		assertTrue("unexpected message: " + res.error(),
			res.error().contains("minimum runtime"));
	}

	@Test
	public void rejectsAStorageThatIsSmallerThanTheHourlyProduction() {
		// the plant produces 200 m3 in an hour, so a storage of 150 m3 can
		// never be filled
		var res = PreCheck.validate(TestPlant.of(150, 2));

		assertTrue(res.isError());
		assertTrue("unexpected message: " + res.error(),
			res.error().contains("single hour"));
	}

	@Test
	public void rejectsAnInvalidMinimumRuntime() {
		assertTrue(PreCheck.validate(TestPlant.of(1600, 1)).isError());
		assertTrue(PreCheck.validate(TestPlant.of(1600, 13)).isError());
	}

	@Test
	public void rejectsAPlantWithoutElectricityPrices() {
		var plant = TestPlant.of(1600, 4);
		plant.electricityPrices = null;

		assertTrue(PreCheck.validate(plant).isError());
	}

	@Test
	public void rejectsAPlantWithoutBoilers() {
		var plant = TestPlant.of(1600, 4);
		plant.boilers.clear();

		assertTrue(PreCheck.validate(plant).isError());
	}

	@Test
	public void rejectsAPlantWithoutSubstrates() {
		var plant = TestPlant.of(1600, 4);
		plant.substrateProfiles.clear();

		assertTrue(PreCheck.validate(plant).isError());
	}
}
