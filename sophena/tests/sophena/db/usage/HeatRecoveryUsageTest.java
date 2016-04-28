package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.db.daos.ProducerDao;
import sophena.model.HeatRecovery;
import sophena.model.ModelType;
import sophena.model.Producer;

public class HeatRecoveryUsageTest {

	private Database db = Tests.getDb();
	private Dao<HeatRecovery> dao = new Dao<>(HeatRecovery.class, db);
	private HeatRecovery recovery;

	@Before
	public void setUp() {
		recovery = new HeatRecovery();
		recovery.id = UUID.randomUUID().toString();
		recovery.name = "HeatRecovery 123";
		dao.insert(recovery);
	}

	@After
	public void tearDown() {
		dao.delete(recovery);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(recovery);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {

		Producer p = new Producer();
		p.id = UUID.randomUUID().toString();
		p.name = "Producer 123";
		p.heatRecovery = recovery;
		ProducerDao pDao = new ProducerDao(db);
		pDao.insert(p);

		List<SearchResult> list = new UsageSearch(db).of(recovery);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(p.id, r.id);
		Assert.assertEquals(p.name, r.name);
		Assert.assertEquals(ModelType.PRODUCER, r.type);
		pDao.delete(p);
	}
}