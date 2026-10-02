package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.BiogasRefurbishmentEntry;

/// Tests the investment cost calculation and the cloning of the investment
/// related entries of a biogas plant.
public class BiogasPlantInvestmentsTest {

	@Test
	public void sumOfNewInvestments() {
		var plant = new BiogasPlant();
		plant.newInvestmentEntries.add(newInvestment(20_000));
		plant.newInvestmentEntries.add(newInvestment(5_000));
		assertEquals(25_000, BiogasPlants.newInvestmentOf(plant), 1e-10);
		assertEquals(0, BiogasPlants.refurbishmentInvestmentOf(plant), 1e-10);
		assertEquals(25_000, BiogasPlants.totalInvestment(plant), 1e-10);
	}

	@Test
	public void refurbishmentUsesOnlyTheGivenShare() {
		var plant = new BiogasPlant();
		plant.refurbishmentEntries.add(refurbishment(10_000, 30));
		assertEquals(0, BiogasPlants.newInvestmentOf(plant), 1e-10);
		assertEquals(3_000, BiogasPlants.refurbishmentInvestmentOf(plant), 1e-10);
		assertEquals(3_000, BiogasPlants.totalInvestment(plant), 1e-10);
	}

	@Test
	public void totalInvestmentCombinesBoilersAndEntries() {
		var plant = new BiogasPlant();
		var boiler = new BiogasPlantBoiler();
		boiler.costs = costs(50_000);
		plant.boilers.add(boiler);
		plant.newInvestmentEntries.add(newInvestment(20_000));
		plant.refurbishmentEntries.add(refurbishment(10_000, 25));
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
		var newEntry = newInvestment(20_000);
		newEntry.costs.operation = 40;
		plant.newInvestmentEntries.add(newEntry);
		var refurb = refurbishment(10_000, 25);
		refurb.costs.operation = 10;
		plant.refurbishmentEntries.add(refurb);
		assertEquals(150, BiogasPlants.totalOperationHours(plant), 1e-10);
	}

	@Test
	public void copyClonesEntriesAndRemapsOwnProducts() {
		var plant = new BiogasPlant();
		plant.id = "plant-1";
		plant.name = "Plant";

		var own = new Product();
		own.id = "own-1";
		own.projectId = plant.id;
		own.name = "Own product";
		plant.ownProducts.add(own);

		var newEntry = newInvestment(20_000);
		newEntry.id = "new-1";
		newEntry.product = own;
		plant.newInvestmentEntries.add(newEntry);

		var refurb = refurbishment(10_000, 30);
		refurb.id = "refurb-1";
		refurb.product = own;
		plant.refurbishmentEntries.add(refurb);

		var copy = plant.copy();

		assertNotEquals(plant.id, copy.id);
		assertEquals("Plant", copy.name);

		assertEquals(1, copy.ownProducts.size());
		var copiedProduct = copy.ownProducts.get(0);
		assertNotEquals(own.id, copiedProduct.id);
		assertEquals(copy.id, copiedProduct.projectId);

		assertEquals(1, copy.newInvestmentEntries.size());
		var copiedNew = copy.newInvestmentEntries.get(0);
		assertNotEquals(newEntry.id, copiedNew.id);
		assertSame(copiedProduct, copiedNew.product);
		assertEquals(20_000, copiedNew.costs.investment, 1e-10);

		assertEquals(1, copy.refurbishmentEntries.size());
		var copiedRefurb = copy.refurbishmentEntries.get(0);
		assertNotEquals(refurb.id, copiedRefurb.id);
		assertSame(copiedProduct, copiedRefurb.product);
		assertEquals(30, copiedRefurb.refurbishmentShare, 1e-10);
	}

	@Test
	public void copyKeepsGlobalProducts() {
		var plant = new BiogasPlant();
		var global = new Product();
		global.id = "global-1";
		global.name = "Global";
		var entry = newInvestment(20_000);
		entry.product = global;
		plant.newInvestmentEntries.add(entry);

		var copy = plant.copy();
		assertSame(global, copy.newInvestmentEntries.get(0).product);
	}

	@Test
	public void capitalCostsIncludeTheInvestmentEntries() {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.interestRate = 0;
		plant.settings.investmentFactor = 1.0;
		var result = BiogasPlantResult.calculate(plant).orElseThrow();

		var before = new BiogasCostCalculator(plant, result).calculate();

		var entry = newInvestment(10_000);
		entry.costs.duration = 20;
		plant.newInvestmentEntries.add(entry);
		var after = new BiogasCostCalculator(plant, result).calculate();

		// a duration that equals the observation period gives A / T
		assertEquals(500, after.capitalCosts - before.capitalCosts, 1e-6);
		assertEquals(10_000, after.investments - before.investments, 1e-10);

		// a refurbishment only spends the given share
		var refurb = refurbishment(10_000, 25);
		refurb.costs.duration = 20;
		plant.refurbishmentEntries.add(refurb);
		var withRefurb = new BiogasCostCalculator(plant, result).calculate();
		assertEquals(125, withRefurb.capitalCosts - after.capitalCosts, 1e-6);
		assertEquals(2_500, withRefurb.investments - after.investments, 1e-10);
	}

	private ProductEntry newInvestment(double investment) {
		var entry = new ProductEntry();
		entry.costs = costs(investment);
		return entry;
	}

	private BiogasRefurbishmentEntry refurbishment(
		double investment, double share
	) {
		var entry = new BiogasRefurbishmentEntry();
		entry.costs = costs(investment);
		entry.refurbishmentShare = share;
		return entry;
	}

	private ProductCosts costs(double investment) {
		var costs = new ProductCosts();
		costs.investment = investment;
		return costs;
	}
}
