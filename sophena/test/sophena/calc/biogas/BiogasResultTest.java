package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.calc.biogas.costs.BiogasCostResult;
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
	public void suggestedHeatPriceReachesTheCapitalReturnRate() {
		var plant = plantWithHeat();
		plant.settings.useCapitalReturnRate = true;
		plant.settings.capitalReturnRate = 20;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertCapitalReturnRate(r, r.costs().dynamicTotal, 20);
		assertCapitalReturnRate(r, r.costs().staticTotal, 20);
	}

	@Test
	public void suggestedHeatPriceReachesTheAnnualSurplus() {
		var plant = plantWithHeat();
		plant.settings.useCapitalReturnRate = false;
		plant.settings.expectedAnnualSurplus = 5000;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertAnnualSurplus(r, r.costs().dynamicTotal, 5000);
		assertAnnualSurplus(r, r.costs().staticTotal, 5000);
	}

	@Test
	public void returnsErrors() {
		assertTrue(BiogasResult.calculate(null).isError());

		var plant = TestPlant.of(1600, 4);
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.electricityPrices = null;
		assertTrue(BiogasResult.calculate(plant).isError());
	}

	private static BiogasPlant plantWithHeat() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.boilers.getFirst().boiler.maxPower = 600;
		plant.settings = BiogasPlantSettings.createDefault(null);
		return plant;
	}

	private static void assertCapitalReturnRate(
		BiogasResult r, BiogasCostResult.Summary s, double expected
	) {
		double achieved = 100 * (s.annualSurplus
			+ s.suggestedHeatPrice * r.revenues().totalGeneratedHeat() / 1000)
			/ s.totalAnnualCosts;
		assertEquals(expected, achieved, 1e-6);
	}

	private static void assertAnnualSurplus(
		BiogasResult r, BiogasCostResult.Summary s, double expected
	) {
		double achieved = s.annualSurplus
			+ s.suggestedHeatPrice * r.revenues().totalGeneratedHeat() / 1000;
		assertEquals(expected, achieved, 1e-6);
	}
}
