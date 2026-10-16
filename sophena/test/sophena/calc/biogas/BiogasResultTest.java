package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import sophena.calc.biogas.costs.BiogasCostResult;
import sophena.model.ProductCosts;
import sophena.model.biogas.BiogasInvestmentEntry;
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
		assertEquals(plant, r.plant());
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
		var revenues = r.revenues();
		var dyn = r.costs().dynamicTotal;

		// with 0 % interest and a price change factor of 1 the annuity equals
		// the first-year value
		double additional = revenues.spotMarketRevenuesSum()
			- revenues.marketRevenuesSum();
		assertEquals(revenues.feedInFundingSum(),
			dyn.feedInFundingRevenues, 1e-6);
		assertEquals(revenues.marketRevenuesSum(),
			dyn.referenceMarketRevenues, 1e-6);
		assertEquals(additional, dyn.additionalRevenues, 1e-6);
		assertEquals(revenues.feedInFundingSum()
			+ revenues.marketRevenuesSum()
			+ additional
			- dyn.directMarketerPayment,
			dyn.revenuesElectricity, 1e-6);
		assertEquals(dyn.revenuesElectricity,
			r.costs().staticTotal.revenuesElectricity, 1e-6);
	}

	@Test
	public void suggestedHeatPriceReachesTheCapitalReturnRate() {
		var plant = plantWithHeat();
		plant.settings.useCapitalReturnRate = true;
		plant.settings.capitalReturnRate = 20;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertCapitalReturnRate(r, r.costs().dynamicTotal, true);
		assertCapitalReturnRate(r, r.costs().staticTotal, false);
	}

	@Test
	public void suggestedHeatPriceReachesTheAnnualSurplus() {
		var plant = plantWithHeat();
		plant.settings.useCapitalReturnRate = false;
		plant.settings.expectedAnnualSurplus = 5000;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertAnnualSurplus(r.costs().dynamicTotal, 5000);
		assertAnnualSurplus(r.costs().staticTotal, 5000);
	}

	@Test
	public void suggestedHeatPriceIsNeverNegative() {
		var plant = plantWithHeat();
		// with a high feed-in tariff the plant already earns more from the
		// electricity than it needs to reach its expected profit, so the heat
		// price is clamped at 0
		plant.settings.isFixedRemuneration = true;
		plant.settings.feedInTariff = 500;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertEquals(0, r.costs().dynamicTotal.suggestedHeatPrice, 1e-10);
		assertEquals(0, r.costs().staticTotal.suggestedHeatPrice, 1e-10);
		assertEquals(0, r.costs().dynamicTotal.revenuesHeat, 1e-10);
		assertEquals(0, r.costs().staticTotal.revenuesHeat, 1e-10);
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

		// with the default prices the plant earns much more from the
		// electricity than it needs, so the suggested heat price would be
		// clamped at 0; we set the prices to 0 so that the plant depends on
		// the revenues from the heat
		Arrays.fill(plant.electricityPrices.values, 0);

		// the capital return rate is calculated from the investments, so the
		// plant needs at least one investment entry
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 500_000;
		plant.investments.add(entry);
		return plant;
	}

	/// Checks that the total capital return rate (Gesamtkapitalrentabilität)
	/// of the given summary reaches the expected value:
	///
	/// - static: `GKR = (JÜ + i * I) / I`
	/// - dynamic: `GKR = (JÜ + 0,5 * i * I) / (I / 2)`
	///
	/// with `JÜ = totalRevenues - totalAnnualCosts`,
	/// `I = investments - investmentFunding`, and `i = interestRate / 100`.
	private static void assertCapitalReturnRate(
		BiogasResult r, BiogasCostResult.Summary s, boolean dynamic
	) {
		var settings = r.plant().settings;
		double capital = s.investments - s.investmentFunding;
		if (dynamic) {
			capital /= 2.0;
		}
		double surplus = s.totalRevenues - s.totalAnnualCosts;
		double interest = settings.interestRate / 100.0;
		double achieved = 100 * (surplus + capital * interest) / capital;
		assertEquals(settings.capitalReturnRate, achieved, 1e-6);
	}

	private static void assertAnnualSurplus(
		BiogasCostResult.Summary s, double expected
	) {
		double achieved = s.totalRevenues - s.totalAnnualCosts;
		assertEquals(expected, achieved, 1e-6);
	}
}
