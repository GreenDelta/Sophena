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
import sophena.db.daos.FuelDao;
import sophena.db.daos.ProducerDao;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.FuelSpec;
import sophena.model.ModelType;
import sophena.model.Producer;

public class FuelUsageTest {

	private Database db = Tests.getDb();
	private FuelDao dao = new FuelDao(Tests.getDb());
	private Fuel fuel;

	@Before
	public void setUp() {
		fuel = new Fuel();
		fuel.id = UUID.randomUUID().toString();
		dao.insert(fuel);
	}

	@After
	public void tearDown() {
		dao.delete(fuel);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(fuel);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsedInConsumer() {
		Consumer consumer = new Consumer();
		consumer.id = id();
		FuelConsumption consumption = new FuelConsumption();
		consumer.fuelConsumptions.add(consumption);
		consumption.id = id();
		consumption.fuel = fuel;
		ConsumerDao cDao = new ConsumerDao(db);
		cDao.insert(consumer);
		List<SearchResult> list = new UsageSearch(db).of(fuel);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(consumer.id, r.id);
		Assert.assertEquals(ModelType.CONSUMER, r.type);
		cDao.delete(consumer);
	}

	@Test
	public void testUsedInProducer() {
		Producer p = new Producer();
		p.id = id();
		FuelSpec spec = new FuelSpec();
		p.fuelSpec = spec;
		spec.fuel = fuel;
		ProducerDao pDao = new ProducerDao(db);
		pDao.insert(p);
		List<SearchResult> list = new UsageSearch(db).of(fuel);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(p.id, r.id);
		Assert.assertEquals(ModelType.PRODUCER, r.type);
		pDao.delete(p);
	}

	private String id() {
		return UUID.randomUUID().toString();
	}

}
