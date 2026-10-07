package sophena.calc.costs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.biogas.BiogasInvestmentEntry;

public class InvestmentItemTest {

	@Test
	public void emptyItemHasNoInvestments() {
		var biogas = InvestmentItem.of((BiogasInvestmentEntry) null);
		assertEquals(0, biogas.investment(), 1e-10);
		assertEquals(0, biogas.initialInvestment(), 1e-10);

		var product = InvestmentItem.of((ProductEntry) null);
		assertEquals(0, product.investment(), 1e-10);
	}

	@Test
	public void productItemIsAlwaysFullyInvested() {
		var entry = new ProductEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 1_000;
		entry.costs.duration = 10;
		var item = InvestmentItem.of(entry);
		assertEquals(1_000, item.investment(), 1e-10);
		assertEquals(1_000, item.initialInvestment(), 1e-10);
		assertEquals(10, item.duration());
	}

	@Test
	public void biogasRefurbishmentUsesAShareInitially() {
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 10_000;
		entry.refurbishmentShare = 30d;
		var item = InvestmentItem.of(entry);
		assertEquals(10_000, item.investment(), 1e-10);
		assertEquals(3_000, item.initialInvestment(), 1e-10);
	}

	@Test
	public void maintenanceBaseUsesTheFullInvestment() {
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 10_000;
		entry.costs.repair = 1;
		entry.costs.maintenance = 2;
		entry.refurbishmentShare = 30d;
		var item = InvestmentItem.of(entry);
		assertEquals(300, Investments.maintenanceBase(item), 1e-10);
	}

	@Test
	public void capitalCostsUseInitialAndReplacementAmounts() {
		assertEquals(
			CapitalCosts.calculate(3_000, 10_000, 8, 20, 1.02, 1.03),
			Investments.capitalCosts(3_000, 10_000, 8, 20, 2, 1.03),
			1e-10);
	}
}
