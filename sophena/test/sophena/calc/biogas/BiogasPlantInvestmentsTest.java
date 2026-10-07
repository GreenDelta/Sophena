package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import sophena.model.ProductCosts;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.BiogasPlantSettings;

/// Tests the investment cost calculation and the cloning of the investment
/// related entries of a biogas plant.
public class BiogasPlantInvestmentsTest {

	@Test
	public void sumOfNewInvestments() {
		var plant = new BiogasPlant();
		plant.investments.add(investment(20_000));
		plant.investments.add(investment(5_000));
		assertEquals(25_000, BiogasPlants.totalInvestment(plant), 1e-10);
	}

	@Test
	public void refurbishmentUsesOnlyTheGivenShare() {
		var plant = new BiogasPlant();
		plant.investments.add(refurbishment(10_000, 30));
		assertEquals(3_000, BiogasPlants.totalInvestment(plant), 1e-10);
	}

	@Test
	public void totalInvestmentCombinesBoilersAndEntries() {
		var plant = new BiogasPlant();
		var boiler = new BiogasPlantBoiler();
		boiler.costs = costs(50_000);
		plant.boilers.add(boiler);
		plant.investments.add(investment(20_000));
		plant.investments.add(refurbishment(10_000, 25));
		assertEquals(50_000 + 20_000 + 2_500,
			BiogasPlants.totalInvestment(plant), 1e-10);
	}

	@Test
	public void totalOperationHoursIncludeEntries() {
		var plant = new BiogasPlant();
		var boiler = new BiogasPlantBoiler();
		boiler.costs = costs(50_000);
		boiler.costs.operation = 100;
		plant.boilers.add(boiler);
		var newEntry = investment(20_000);
		newEntry.costs.operation = 40;
		plant.investments.add(newEntry);
		var refurb = refurbishment(10_000, 25);
		refurb.costs.operation = 10;
		plant.investments.add(refurb);
		assertEquals(150, BiogasPlants.totalOperationHours(plant), 1e-10);
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

		var before = new BiogasCostCalculator(plant, result).calculate();

		var entry = investment(10_000);
		entry.costs.duration = 20;
		plant.investments.add(entry);
		var after = new BiogasCostCalculator(plant, result).calculate();

		// a duration that equals the observation period gives A / T
		assertEquals(500, after.capitalCosts - before.capitalCosts, 1e-6);
		assertEquals(10_000, after.investments - before.investments, 1e-10);

		// a refurbishment only spends the given share
		var refurb = refurbishment(10_000, 25);
		refurb.costs.duration = 20;
		plant.investments.add(refurb);
		var withRefurb = new BiogasCostCalculator(plant, result).calculate();
		assertEquals(125, withRefurb.capitalCosts - after.capitalCosts, 1e-6);
		assertEquals(2_500, withRefurb.investments - after.investments, 1e-10);
	}

	@Test
	public void refurbishmentReplacementsUseTheFullInvestment() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 0;
		plant.settings.investmentFactor = 1.0;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		var before = new BiogasCostCalculator(plant, result).calculate();

		var refurb = refurbishment(10_000, 30);
		refurb.costs.duration = 10;
		plant.investments.add(refurb);
		var after = new BiogasCostCalculator(plant, result).calculate();

		// initial: 3_000 EUR (30 %); one replacement after 10 years: 10_000 EUR;
		// spread over 20 years => (3_000 + 10_000) / 20 = 650 EUR/a
		assertEquals(650, after.capitalCosts - before.capitalCosts, 1e-6);
		assertEquals(3_000, after.investments - before.investments, 1e-10);
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
		var withNormal = new BiogasCostCalculator(plant, result).calculate();

		plant.investments.clear();
		var refurb = refurbishment(10_000, 30);
		refurb.costs.duration = 10;
		refurb.costs.maintenance = 2;
		refurb.costs.repair = 1;
		plant.investments.add(refurb);
		var withRefurb = new BiogasCostCalculator(plant, result).calculate();

		// maintenance and repair are applied to the full investment and the
		// insurance is based on the full investment value as well, so the
		// operation costs are the same as for a normal investment
		assertEquals(withNormal.operationCosts, withRefurb.operationCosts, 1e-6);
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

	private ProductCosts costs(double investment) {
		var costs = new ProductCosts();
		costs.investment = investment;
		return costs;
	}
}
