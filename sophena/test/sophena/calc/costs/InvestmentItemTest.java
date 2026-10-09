package sophena.calc.costs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;

/// Tests the mapping of the different model structures into [InvestmentItem]s
/// via the `allOf` methods.
public class InvestmentItemTest {

	@Test
	public void emptyStructuresHaveNoItems() {
		assertEquals(0, InvestmentItem.allOf((Project) null).size());
		assertEquals(0, InvestmentItem.allOf(new Project()).size());
		assertEquals(0, InvestmentItem.allOf((BiogasPlant) null).size());
		assertEquals(0, InvestmentItem.allOf(new BiogasPlant()).size());
	}

	@Test
	public void productEntryBecomesAFullyInvestedItem() {
		var product = new Product();
		product.name = "Test product";
		product.type = ProductType.BUFFER_TANK;

		var entry = new ProductEntry();
		entry.product = product;
		entry.costs = new ProductCosts();
		entry.costs.investment = 1_000;
		entry.costs.duration = 10;

		var project = new Project();
		project.productEntries.add(entry);

		var items = InvestmentItem.allOf(project);
		assertEquals(1, items.size());
		var item = items.getFirst();
		assertEquals("Test product", item.asset());
		assertEquals(ProductType.BUFFER_TANK, item.productType());
		assertEquals(1_000, item.investment(), 1e-10);
		assertEquals(1_000, item.initialInvestment(), 1e-10);
		assertEquals(10, item.lifetime());
	}

	@Test
	public void biogasRefurbishmentUsesAShareInitially() {
		var plant = new BiogasPlant();
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 10_000;
		entry.refurbishmentShare = 30d;
		plant.investments.add(entry);

		var items = InvestmentItem.allOf(plant);
		assertEquals(1, items.size());
		var item = items.getFirst();
		assertEquals(10_000, item.investment(), 1e-10);
		assertEquals(3_000, item.initialInvestment(), 1e-10);
	}

	@Test
	public void biogasBoilerBecomesAnItem() {
		var boiler = new Boiler();
		boiler.name = "Test boiler";
		boiler.type = ProductType.BIOMASS_BOILER;

		var plantBoiler = new BiogasPlantBoiler();
		plantBoiler.boiler = boiler;
		plantBoiler.costs = new ProductCosts();
		plantBoiler.costs.investment = 50_000;
		plantBoiler.costs.duration = 15;

		var plant = new BiogasPlant();
		plant.boilers.add(plantBoiler);

		var items = InvestmentItem.allOf(plant);
		assertEquals(1, items.size());
		var item = items.getFirst();
		assertEquals("Test boiler", item.asset());
		assertEquals(ProductType.BIOMASS_BOILER, item.productType());
		assertEquals(50_000, item.investment(), 1e-10);
		assertEquals(50_000, item.initialInvestment(), 1e-10);
		assertEquals(15, item.lifetime());
	}

	@Test
	public void maintenanceBaseUsesTheFullInvestment() {
		var plant = new BiogasPlant();
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 10_000;
		entry.costs.repair = 1;
		entry.costs.maintenance = 2;
		entry.refurbishmentShare = 30d;
		plant.investments.add(entry);

		var item = InvestmentItem.allOf(plant).getFirst();
		assertEquals(300, Investments.maintenanceBaseOf(item), 1e-10);
	}

	@Test
	public void capitalCostsUseInitialAndReplacementAmounts() {
		assertEquals(
			CapitalCosts.calculate(3_000, 10_000, 8, 20, 1.02, 1.03),
			Investments.capitalCosts(3_000, 10_000, 8, 20, 2, 1.03),
			1e-10);
	}
}
