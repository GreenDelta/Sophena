package sophena.db.usage;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.model.ModelType;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.ElectricityPriceCurve;

public class ElectricityPriceCurveUsageTest {

	private final Database db = Tests.getDb();
	private ElectricityPriceCurve curve;

	@Before
	public void setUp() {
		curve = new ElectricityPriceCurve();
		curve.id = UUID.randomUUID().toString();
		db.insert(curve);
	}

	@After
	public void tearDown() {
		db.delete(curve);
	}

	@Test
	public void testNotUsed() {
		var list = new UsageSearch(db).of(curve);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		var plant = new BiogasPlant();
		plant.id = UUID.randomUUID().toString();
		plant.electricityPrices = curve;
		db.insert(plant);

		var list = new UsageSearch(db).of(curve);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.getFirst();
		Assert.assertEquals(plant.id, r.id);
		Assert.assertEquals(plant.name, r.name);
		Assert.assertEquals(ModelType.BIOGAS_PLANT, r.type);

		db.delete(plant);
	}
}
