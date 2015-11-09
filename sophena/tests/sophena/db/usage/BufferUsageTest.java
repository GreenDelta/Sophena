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
import sophena.db.daos.ProjectDao;
import sophena.model.BufferTank;
import sophena.model.HeatNet;
import sophena.model.ModelType;
import sophena.model.Project;

public class BufferUsageTest {

	private Database db = Tests.getDb();
	private Dao<BufferTank> dao = new Dao<>(BufferTank.class, Tests.getDb());
	private BufferTank tank;

	@Before
	public void setUp() {
		tank = new BufferTank();
		tank.id = UUID.randomUUID().toString();
		dao.insert(tank);
	}

	@After
	public void tearDown() {
		dao.delete(tank);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(tank);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		Project p = new Project();
		p.id = UUID.randomUUID().toString();
		p.heatNet = new HeatNet();
		p.heatNet.id = UUID.randomUUID().toString();
		p.heatNet.bufferTank = tank;
		ProjectDao pDao = new ProjectDao(db);
		pDao.insert(p);
		List<SearchResult> list = new UsageSearch(db).of(tank);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(p.id, r.id);
		Assert.assertEquals(p.name, r.name);
		Assert.assertEquals(ModelType.PROJECT, r.type);
		pDao.delete(p);
	}

}
