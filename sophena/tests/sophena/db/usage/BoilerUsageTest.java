package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
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

	@Test
	public void testNotUsed() {
		Boiler b = new Boiler();
		b.id = UUID.randomUUID().toString();
		dao.insert(b);
		List<SearchResult> list = new UsageSearch(db).of(b);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		Boiler b = new Boiler();
		b.id = UUID.randomUUID().toString();
		dao.insert(b);
		Producer p = new Producer();
		p.id = UUID.randomUUID().toString();
		p.boiler = b;
		ProducerDao pDao = new ProducerDao(db);
		pDao.insert(p);
		List<SearchResult> list = new UsageSearch(db).of(b);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(p.id, r.id);
		Assert.assertEquals(p.name, r.name);
		Assert.assertEquals(ModelType.PRODUCER, r.type);
	}

}
