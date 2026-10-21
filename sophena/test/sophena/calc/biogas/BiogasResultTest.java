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
	public void heatPriceReachesTheCapitalReturnRate() {
		var plant = plantWithHeat();
		plant.settings.capitalReturnRate = 20;
		plant.settings.expectedAnnualSurplus = 0;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertCapitalReturnRate(r, r.costs().dynamicTotal, true);
		assertCapitalReturnRate(r, r.costs().staticTotal, false);
	}

	@Test
	public void heatPriceReachesTheAnnualSurplus() {
		var plant = plantWithHeat();
		plant.settings.expectedAnnualSurplus = 5000;

		var r = BiogasResult.calculate(plant).orElseThrow();
		assertAnnualSurplus(r, r.costs().dynamicTotal);
		assertAnnualSurplus(r, r.costs().staticTotal);
	}

	@Test
	public void heatSharesAndCoolingIncreaseTheHeatPrices() {
		var plant = plantWithHeat();
		plant.settings.expectedAnnualSurplus = 5000;
		plant.settings.interestRate = 0;
		plant.settings.electricityFactor = 1.0;

		var base = BiogasResult.calculate(plant).orElseThrow();
		var baseStat = base.costs().staticTotal;
		double basePriceCapital = baseStat.heatPriceCapitalReturnRate;
		double basePriceSurplus = baseStat.heatPriceAnnualSurplus;
		double baseCosts = baseStat.totalAnnualCosts;

		plant.settings.fermenterHeatShare = 10;
		plant.settings.unusedHeatShare = 20;
		var r = BiogasResult.calculate(plant).orElseThrow();

		double generated = r.revenues().totalGeneratedHeat();
		double sellable = generated * 0.7;
		double coolingCosts = generated * 0.2 * 0.005
			* plant.settings.electricityPrice;

		var s = r.costs().staticTotal;
		assertEquals(baseCosts + coolingCosts, s.totalAnnualCosts, 1e-6);
		assertTrue(s.heatPriceCapitalReturnRate > basePriceCapital);
		assertTrue(s.heatPriceAnnualSurplus > basePriceSurplus);
		double max = Math.max(
			s.heatPriceCapitalReturnRate, s.heatPriceAnnualSurplus);
		assertEquals(max * sellable / 1000.0, s.revenuesHeat, 1e-6);
	}

	@Test
	public void heatPricesAreNeverNegative() {
		var plant = plantWithHeat();
		// with a high feed-in tariff the plant already earns more from the
		// electricity than it needs to reach its expected profit, so the heat
		// prices are clamped at 0
		plant.settings.isFixedRemuneration = true;
		plant.settings.feedInTariff = 500;

		var r = BiogasResult.calculate(plant).orElseThrow();
		var summaries = new BiogasCostResult.Summary[] {
			r.costs().dynamicTotal, r.costs().staticTotal
		};
		for (var s : summaries) {
			assertEquals(0, s.heatPriceCapitalReturnRate, 1e-10);
			assertEquals(0, s.heatPriceAnnualSurplus, 1e-10);
			assertEquals(0, s.revenuesHeat, 1e-10);
		}
	}

	@Test
	public void effectiveHeatPriceUsesTheDefaultOrTheUserValue() {
		var plant = plantWithHeat();
		var r = BiogasResult.calculate(plant).orElseThrow();
		var stat = r.costs().staticTotal;
		double expected = Math.max(
			stat.heatPriceCapitalReturnRate, stat.heatPriceAnnualSurplus);

		assertEquals(expected, r.defaultHeatPrice(), 1e-10);
		assertEquals(expected, r.getEffectiveHeatPrice(null), 1e-10);
		assertEquals(42.0, r.getEffectiveHeatPrice(42.0), 1e-10);
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

	/// The heat that can be sold to the heat network in kWh/a.
	private static double soldHeat(BiogasResult r) {
		var settings = r.plant().settings;
		double share = Math.min(1,
			Math.clamp(settings.fermenterHeatShare, 0, 100) / 100.0
				+ Math.clamp(settings.unusedHeatShare, 0, 100) / 100.0);
		return r.revenues().totalGeneratedHeat() * (1 - share);
	}

	/// Checks that the total capital return rate (Gesamtkapitalrentabilität)
	/// of the given summary reaches the expected value when the heat is sold
	/// for `heatPriceCapitalReturnRate`:
	///
	/// - static: `GKR = (JÜ + i * I) / I`
	/// - dynamic: `GKR = (JÜ + 0,5 * i * I) / (I / 2)`
	///
	/// with `I = investments - investmentFunding` and
	/// `i = interestRate / 100`.
	private static void assertCapitalReturnRate(
		BiogasResult r, BiogasCostResult.Summary s, boolean dynamic
	) {
		var settings = r.plant().settings;
		double capital = s.investments - s.investmentFunding;
		if (dynamic) {
			capital /= 2.0;
		}
		double surplus = s.annualFunding
			+ s.revenuesElectricity
			+ s.heatPriceCapitalReturnRate * soldHeat(r) / 1000.0
			- s.totalAnnualCosts;
		double interest = settings.interestRate / 100.0;
		double achieved = 100 * (surplus + capital * interest) / capital;
		assertEquals(settings.capitalReturnRate, achieved, 1e-6);
	}

	/// Checks that the annual surplus of the given summary reaches the
	/// expected value when the heat is sold for `heatPriceAnnualSurplus`.
	private static void assertAnnualSurplus(
		BiogasResult r, BiogasCostResult.Summary s
	) {
		double surplus = s.annualFunding
			+ s.revenuesElectricity
			+ s.heatPriceAnnualSurplus * soldHeat(r) / 1000.0
			- s.totalAnnualCosts;
		assertEquals(r.plant().settings.expectedAnnualSurplus, surplus, 1e-6);
	}
}
