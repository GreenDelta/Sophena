package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasRefurbishmentEntry;

/// Tests that the investment entries of a biogas plant are persisted and that
/// plant-private products are deleted together with the plant.
public class BiogasPlantInvestmentTest {

	private final Database db = Tests.getDb();

	@Test
	public void persistsAndLoadsInvestments() {
		var plant = withInvestments();
		var loaded = db.get(BiogasPlant.class, plant.id);

		Assert.assertNotNull(loaded);
		Assert.assertEquals(1, loaded.newInvestmentEntries.size());
		Assert.assertEquals(20_000,
			loaded.newInvestmentEntries.get(0).costs.investment, 1e-10);
		Assert.assertEquals(1, loaded.refurbishmentEntries.size());
		Assert.assertEquals(30,
			loaded.refurbishmentEntries.get(0).refurbishmentShare, 1e-10);

		db.delete(plant);
		Assert.assertNull(db.get(BiogasPlant.class, plant.id));
	}

	@Test
	public void deletesPrivateProductsWithThePlant() {
		var plant = withInvestments();
		var productId = plant.ownProducts.get(0).id;
		var refurbishmentId = plant.refurbishmentEntries.get(0).id;
		Assert.assertNotNull(db.get(Product.class, productId));

		db.delete(plant);
		Assert.assertNull(db.get(Product.class, productId));
		Assert.assertNull(db.get(
			BiogasRefurbishmentEntry.class, refurbishmentId));
	}

	private BiogasPlant withInvestments() {
		var plant = new BiogasPlant();
		plant.id = UUID.randomUUID().toString();
		plant.name = "Test plant";
		plant.duration = 20;

		var product = new Product();
		product.id = UUID.randomUUID().toString();
		product.name = "Behälter";
		product.projectId = plant.id;
		plant.ownProducts.add(product);

		var newEntry = new ProductEntry();
		newEntry.id = UUID.randomUUID().toString();
		newEntry.costs = costs(20_000);
		newEntry.product = product;
		plant.newInvestmentEntries.add(newEntry);

		var refurb = new BiogasRefurbishmentEntry();
		refurb.id = UUID.randomUUID().toString();
		refurb.costs = costs(10_000);
		refurb.refurbishmentShare = 30;
		refurb.product = product;
		plant.refurbishmentEntries.add(refurb);

		db.insert(plant);
		return plant;
	}

	private ProductCosts costs(double investment) {
		var costs = new ProductCosts();
		costs.investment = investment;
		return costs;
	}
}
