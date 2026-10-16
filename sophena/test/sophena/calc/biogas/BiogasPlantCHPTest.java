package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;

/// Tests the calculation of the CHP performance of a biogas plant.
public class BiogasPlantCHPTest {

	@Test
	public void emptyPlant() {
		var chp = BiogasPlantCHP.of(null);
		assertEquals(0, chp.electricPower(), 1e-10);
		assertEquals(0, chp.thermalPower(), 1e-10);
		assertFalse(chp.isValid());

		chp = BiogasPlantCHP.of(new BiogasPlant());
		assertFalse(chp.isValid());
	}

	@Test
	public void singleBlock() {
		var plant = new BiogasPlant();
		plant.boilers.add(block(500, 600, 0.4, 0.45));

		var chp = BiogasPlantCHP.of(plant);
		assertTrue(chp.isValid());
		assertEquals(500, chp.electricPower(), 1e-10);
		assertEquals(600, chp.thermalPower(), 1e-10);
		assertEquals(0.4, chp.electricEfficiency(), 1e-10);
		assertEquals(0.45, chp.thermalEfficiency(), 1e-10);
	}

	@Test
	public void sumsThePowersOfTheBlocks() {
		var plant = new BiogasPlant();
		plant.boilers.add(block(500, 600, 0.4, 0.4));
		plant.boilers.add(block(500, 200, 0.5, 0.6));

		var chp = BiogasPlantCHP.of(plant);
		assertEquals(1000, chp.electricPower(), 1e-10);
		assertEquals(800, chp.thermalPower(), 1e-10);
	}

	@Test
	public void weightsTheElectricEfficiencyByTheElectricPower() {
		var plant = new BiogasPlant();
		plant.boilers.add(block(1000, 100, 0.4, 0.2));
		plant.boilers.add(block(1000, 900, 0.6, 0.5));

		var chp = BiogasPlantCHP.of(plant);
		assertEquals(0.5, chp.electricEfficiency(), 1e-10);

		// the thermal efficiency is weighted by the thermal power
		assertEquals((100 * 0.2 + 900 * 0.5) / 1000,
			chp.thermalEfficiency(), 1e-10);
	}

	@Test
	public void skipsBlocksWithoutAnElectricPower() {
		var plant = new BiogasPlant();
		plant.boilers.add(block(0, 600, 0.4, 0.4));

		var chp = BiogasPlantCHP.of(plant);
		assertFalse(chp.isValid());
		assertEquals(0, chp.electricPower(), 1e-10);
		assertEquals(0, chp.thermalPower(), 1e-10);
	}

	private static BiogasPlantBoiler block(
		double electricPower,
		double thermalPower,
		double electricRate,
		double thermalRate
	) {
		var boiler = new Boiler();
		boiler.maxPowerElectric = electricPower;
		boiler.maxPower = thermalPower;
		boiler.efficiencyRateElectric = electricRate;
		boiler.efficiencyRate = thermalRate;
		var entry = new BiogasPlantBoiler();
		entry.boiler = boiler;
		return entry;
	}
}
