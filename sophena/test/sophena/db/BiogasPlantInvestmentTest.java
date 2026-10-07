package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.model.ProductCosts;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;

/// Tests that the investment entries of a biogas plant are persisted and that
/// they are deleted together with the plant.
public class BiogasPlantInvestmentTest {

	private final Database db = Tests.getDb();

	@Test
	public void persistsAndLoadsInvestments() {
		var plant = withInvestments();
		var loaded = db.get(BiogasPlant.class, plant.id);

		Assert.assertNotNull(loaded);
		Assert.assertEquals(1, loaded.investments.size());
		Assert.assertEquals(20_000,
			loaded.investments.get(0).costs.investment, 1e-10);
		Assert.assertEquals(30,
			loaded.investments.get(0).refurbishmentShare, 1e-10);

		db.delete(plant);
		Assert.assertNull(db.get(BiogasPlant.class, plant.id));
	}

	@Test
	public void deletesInvestmentEntriesWithThePlant() {
		var plant = withInvestments();
		var entryId = plant.investments.get(0).id;
		Assert.assertNotNull(db.get(BiogasInvestmentEntry.class, entryId));

		db.delete(plant);
		Assert.assertNull(db.get(BiogasInvestmentEntry.class, entryId));
	}

	private BiogasPlant withInvestments() {
		var plant = new BiogasPlant();
		plant.id = UUID.randomUUID().toString();
		plant.name = "Test plant";
		plant.duration = 20;

		var entry = new BiogasInvestmentEntry();
		entry.id = UUID.randomUUID().toString();
		entry.name = "Behälter";
		entry.costs = costs(20_000);
		entry.refurbishmentShare = 30d;
		plant.investments.add(entry);

		db.insert(plant);
		return plant;
	}

	private ProductCosts costs(double investment) {
		var costs = new ProductCosts();
		costs.investment = investment;
		return costs;
	}
}
