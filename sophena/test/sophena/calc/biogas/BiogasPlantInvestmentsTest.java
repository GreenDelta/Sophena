package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import sophena.calc.biogas.costs.BiogasCostCalculator;
import sophena.calc.biogas.costs.BiogasCostResult;
import sophena.calc.biogas.costs.BiogasRuntimeRevenues;
import sophena.model.ProductCosts;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;

/// Tests the investment cost calculation and the cloning of the investment
/// related entries of a biogas plant.
public class BiogasPlantInvestmentsTest {

	@Test
	public void sumOfNewInvestments() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.investments.add(investment(20_000));
		plant.investments.add(investment(5_000));
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var costs = costResult(plant, result);
		assertEquals(25_000, costs.dynamicTotal.investments, 1e-10);
	}

	@Test
	public void refurbishmentUsesOnlyTheGivenShare() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.investments.add(refurbishment(10_000, 30));
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var costs = costResult(plant, result);
		assertEquals(3_000, costs.dynamicTotal.investments, 1e-10);
	}

	@Test
	public void totalInvestmentCombinesBoilersAndEntries() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.boilers.get(0).costs = costs(50_000);
		plant.investments.add(investment(20_000));
		plant.investments.add(refurbishment(10_000, 25));
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var costs = costResult(plant, result);
		assertEquals(50_000 + 20_000 + 2_500,
			costs.dynamicTotal.investments, 1e-10);
	}

	@Test
	public void operationHoursOfEntriesAreIncluded() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 0;
		plant.settings.operationFactor = 1.0;
		plant.settings.insuranceCostsShare = 0;

		var boilerCosts = costs(50_000);
		boilerCosts.operation = 100;
		plant.boilers.get(0).costs = boilerCosts;

		var newEntry = investment(20_000);
		newEntry.costs.operation = 40;
		plant.investments.add(newEntry);
		var refurb = refurbishment(10_000, 25);
		refurb.costs.operation = 10;
		plant.investments.add(refurb);

		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var costs = costResult(plant, result);

		// 150 operation hours * 25 EUR/h; with an operation factor of 1 and an
		// interest rate of 0 the annuity equals the first-year value
		assertEquals(3_750, costs.dynamicTotal.operationCosts, 1e-6);
	}

	@Test
	public void fundingReducesTheCapitalCosts() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 0;
		plant.settings.investmentFactor = 1.0;
		plant.settings.funding = 10_000;

		var boilerCosts = costs(50_000);
		boilerCosts.duration = 20;
		plant.boilers.get(0).costs = boilerCosts;

		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var costs = costResult(plant, result);

		// with 0 % interest the annuity factor is 1 / T: the capital costs are
		// 50_000 / 20 = 2_500 minus the funding 10_000 / 20 = 500 EUR/a
		assertEquals(10_000, costs.dynamicTotal.investmentFunding, 1e-10);
		assertEquals(2_000, costs.dynamicTotal.capitalCosts, 1e-6);
		assertEquals(2_000, costs.staticTotal.capitalCosts, 1e-6);
	}

	@Test
	public void copyClonesEntries() {
		var plant = new BiogasPlant();
		plant.id = "plant-1";
		plant.name = "Plant";
		var entry = investment(20_000);
		entry.id = "entry-1";
		entry.name = "Behälter";
		plant.investments.add(entry);

		var copy = plant.copy();

		assertNotEquals(plant.id, copy.id);
		assertEquals("Plant", copy.name);
		assertEquals(1, copy.investments.size());
		var copied = copy.investments.get(0);
		assertNotEquals(entry.id, copied.id);
		assertEquals("Behälter", copied.name);
		assertEquals(20_000, copied.costs.investment, 1e-10);
	}

	@Test
	public void capitalCostsIncludeTheInvestmentEntries() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 0;
		plant.settings.investmentFactor = 1.0;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		var before = costResult(plant, result);

		var entry = investment(10_000);
		entry.costs.duration = 20;
		plant.investments.add(entry);
		var after = costResult(plant, result);

		// a duration that equals the observation period gives A / T
		assertEquals(500,
			after.dynamicTotal.capitalCosts - before.dynamicTotal.capitalCosts, 1e-6);
		assertEquals(10_000,
			after.dynamicTotal.investments - before.dynamicTotal.investments, 1e-10);

		// a refurbishment only spends the given share
		var refurb = refurbishment(10_000, 25);
		refurb.costs.duration = 20;
		plant.investments.add(refurb);
		var withRefurb = costResult(plant, result);
		assertEquals(125,
			withRefurb.dynamicTotal.capitalCosts - after.dynamicTotal.capitalCosts, 1e-6);
		assertEquals(2_500,
			withRefurb.dynamicTotal.investments - after.dynamicTotal.investments, 1e-10);
	}

	@Test
	public void refurbishmentReplacementsUseTheFullInvestment() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 0;
		plant.settings.investmentFactor = 1.0;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		var before = costResult(plant, result);

		var refurb = refurbishment(10_000, 30);
		refurb.costs.duration = 10;
		plant.investments.add(refurb);
		var after = costResult(plant, result);

		// initial: 3_000 EUR (30 %); one replacement after 10 years: 10_000 EUR;
		// spread over 20 years => (3_000 + 10_000) / 20 = 650 EUR/a
		assertEquals(650,
			after.dynamicTotal.capitalCosts - before.dynamicTotal.capitalCosts, 1e-6);
		assertEquals(3_000,
			after.dynamicTotal.investments - before.dynamicTotal.investments, 1e-10);
	}

	@Test
	public void refurbishmentMaintenanceUsesTheFullInvestment() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 2;
		plant.settings.investmentFactor = 1.0;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		var normal = investment(10_000);
		normal.costs.duration = 10;
		normal.costs.maintenance = 2;
		normal.costs.repair = 1;
		plant.investments.add(normal);
		var withNormal = costResult(plant, result);

		plant.investments.clear();
		var refurb = refurbishment(10_000, 30);
		refurb.costs.duration = 10;
		refurb.costs.maintenance = 2;
		refurb.costs.repair = 1;
		plant.investments.add(refurb);
		var withRefurb = costResult(plant, result);

		// maintenance and repair are applied to the full investment, so the
		// operation costs are the same as for a normal investment
		assertEquals(withNormal.dynamicTotal.operationCosts,
			withRefurb.dynamicTotal.operationCosts, 1e-6);
	}

	private BiogasInvestmentEntry investment(double investment) {
		var entry = new BiogasInvestmentEntry();
		entry.costs = costs(investment);
		return entry;
	}

	private BiogasInvestmentEntry refurbishment(
		double investment, double share
	) {
		var entry = investment(investment);
		entry.refurbishmentShare = share;
		return entry;
	}

	private static BiogasCostResult costResult(
		BiogasPlant plant, BiogasRuntimeResult result
	) {
		var revenues = BiogasRuntimeRevenues.calculate(plant, result)
			.orElseThrow();
		return new BiogasCostCalculator(plant, result, revenues).calculate();
	}

	private ProductCosts costs(double investment) {
		var costs = new ProductCosts();
		costs.investment = investment;
		return costs;
	}
}
