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
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

public class SubstrateUsageTest {

	private final Database db = Tests.getDb();
	private Substrate substrate;

	@Before
	public void setUp() {
		substrate = new Substrate();
		substrate.id = UUID.randomUUID().toString();
		db.insert(substrate);
	}

	@After
	public void tearDown() {
		db.delete(substrate);
	}

	@Test
	public void testNotUsed() {
		var list = new UsageSearch(db).of(substrate);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		var profile = new SubstrateProfile();
		profile.id = UUID.randomUUID().toString();
		profile.substrate = substrate;

		var plant = new BiogasPlant();
		plant.id = UUID.randomUUID().toString();
		plant.substrateProfiles.add(profile);
		db.insert(plant);

		var list = new UsageSearch(db).of(substrate);
		Assert.assertEquals(1, list.size());
		var result = list.getFirst();
		Assert.assertEquals(plant.id, result.id);
		Assert.assertEquals(plant.name, result.name);
		Assert.assertEquals(ModelType.BIOGAS_PLANT, result.type);

		db.delete(plant);
	}
}
