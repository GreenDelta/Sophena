package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;

/// Tests the overall result of a biogas plant.
public class BiogasResultTest {

	@Test
	public void calculatesAllSubResults() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertNotNull(r.runtime());
		assertNotNull(r.revenues());
		assertNotNull(r.costs());
		assertEquals(plant, r.runtime().plant());
	}

	@Test
	public void revenuesArePartOfTheCostResult() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		var settings = BiogasPlantSettings.createDefault(null);
		settings.interestRate = 0;
		settings.electricityRevenuesFactor = 1.0;
		plant.settings = settings;

		var r = BiogasResult.calculate(plant).orElseThrow();

		// with 0 % interest the annuity equals the first-year value
		assertEquals(r.revenues().totalRevenues(),
			r.costs().dynamicTotal.revenuesElectricity, 1e-6);
		assertEquals(r.revenues().totalRevenues(),
			r.costs().staticTotal.revenuesElectricity, 1e-6);
	}

	@Test
	public void returnsErrors() {
		assertTrue(BiogasResult.calculate(null).isError());

		var plant = TestPlant.of(1600, 4);
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.electricityPrices = null;
		assertTrue(BiogasResult.calculate(plant).isError());
	}
}
