package sophena.calc.biogas.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sophena.calc.biogas.runtime.BiogasProfile.waterVaporCorrectionFactor;

import org.junit.Test;

import sophena.calc.biogas.TestPlant;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlantSettings;

/// Tests how the methane slip is applied to the produced gas.
public class BiogasProfileTest {

	@Test
	public void testWaterVaporCorrectionFactor() {
		assertEquals(0.991,	waterVaporCorrectionFactor(5), 1e-3);
		assertEquals(0.988, waterVaporCorrectionFactor(10), 1e-3);
		assertEquals(0.984, waterVaporCorrectionFactor(15), 1e-3);
		assertEquals(0.978, waterVaporCorrectionFactor(20), 1e-3);
		assertEquals(0.971, waterVaporCorrectionFactor(25), 1e-3);
		assertEquals(0.962, waterVaporCorrectionFactor(30), 1e-3);
		assertEquals(0.951, waterVaporCorrectionFactor(35), 1e-3);
		assertEquals(0.936, waterVaporCorrectionFactor(40), 1e-3);
		assertEquals(0.919, waterVaporCorrectionFactor(45), 1e-3);
	}

	@Test
	public void methaneSlipReducesTheProducedGasVolume() {
		var plant = TestPlant.of(1600, 4);
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.methaneSlip = 0;

		var without = BiogasProfile.of(plant);
		double volume = without.volumeAt(0);

		plant.settings.methaneSlip = 10;
		var with = BiogasProfile.of(plant);

		assertEquals(volume * 0.9, with.volumeAt(0), 1e-10);

		// the methane content is not changed by the slip
		assertEquals(without.methaneContentAt(0),
			with.methaneContentAt(0), 1e-10);
	}

	@Test
	public void aSlipAboveHundredDoesNotProduceNegativeGas() {
		var plant = TestPlant.of(1600, 4);
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.methaneSlip = 150;

		var profile = BiogasProfile.of(plant);
		for (int h = 0; h < Stats.HOURS; h++) {
			assertEquals(0, profile.volumeAt(h), 1e-12);
		}
	}

	@Test
	public void withoutSettingsThereIsNoMethaneSlip() {
		var plant = TestPlant.of(1600, 4);
		plant.settings = null;
		var profile = BiogasProfile.of(plant);
		assertTrue(profile.volumeAt(0) > 0);
	}
}
