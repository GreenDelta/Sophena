package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.db.daos.BoilerDao;
import sophena.db.daos.ProducerDao;
import sophena.model.Boiler;
import sophena.model.ModelType;
import sophena.model.Producer;

public class BoilerUsageTest {

	private Database db = Tests.getDb();
	private BoilerDao dao = new BoilerDao(Tests.getDb());
	private Boiler boiler;

	@Before
	public void setUp() {
		boiler = new Boiler();
		boiler.id = UUID.randomUUID().toString();
		dao.insert(boiler);
	}

	@After
	public void tearDown() {
		dao.delete(boiler);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(boiler);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		Producer p = new Producer();
		p.id = UUID.randomUUID().toString();
		p.boiler = boiler;
		ProducerDao pDao = new ProducerDao(db);
		pDao.insert(p);
		List<SearchResult> list = new UsageSearch(db).of(boiler);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(p.id, r.id);
		Assert.assertEquals(p.name, r.name);
		Assert.assertEquals(ModelType.PRODUCER, r.type);
		pDao.delete(p);
	}

}
