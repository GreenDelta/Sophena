package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.db.daos.ConsumerDao;
import sophena.db.daos.Dao;
import sophena.model.Consumer;
import sophena.model.ModelType;
import sophena.model.TransferStation;

public class TransferStationUsageTest {

	private Database db = Tests.getDb();
	private Dao<TransferStation> dao = new Dao<>(TransferStation.class, db);
	private TransferStation station;

	@Before
	public void setUp() {
		station = new TransferStation();
		station.id = UUID.randomUUID().toString();
		station.name = "TransferStation 123";
		dao.insert(station);
	}

	@After
	public void tearDown() {
		dao.delete(station);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(station);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		Consumer c = new Consumer();
		c.id = UUID.randomUUID().toString();
		c.transferStation = station;
		ConsumerDao cDao = new ConsumerDao(db);
		cDao.insert(c);
		List<SearchResult> list = new UsageSearch(db).of(station);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(c.id, r.id);
		Assert.assertEquals(c.name, r.name);
		Assert.assertEquals(ModelType.CONSUMER, r.type);
		cDao.delete(c);
	}
}
