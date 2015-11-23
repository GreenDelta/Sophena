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
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Project;

public class PipeUsageTest {

	private Database db = Tests.getDb();
	private Dao<Pipe> dao = new Dao<>(Pipe.class, Tests.getDb());
	private Pipe pipe;

	@Before
	public void setUp() {
		pipe = new Pipe();
		pipe.id = UUID.randomUUID().toString();
		dao.insert(pipe);
	}

	@After
	public void tearDown() {
		dao.delete(pipe);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(pipe);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		Project project = new Project();
		project.id = UUID.randomUUID().toString();
		HeatNet net = new HeatNet();
		project.heatNet = net;
		net.id = UUID.randomUUID().toString();
		HeatNetPipe netPipe = new HeatNetPipe();
		net.pipes.add(netPipe);
		netPipe.id = UUID.randomUUID().toString();
		netPipe.pipe = pipe;
		ProjectDao projectDao = new ProjectDao(db);
		projectDao.insert(project);
		List<SearchResult> list = new UsageSearch(db).of(pipe);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(project.id, r.id);
		Assert.assertEquals(ModelType.PROJECT, r.type);
		projectDao.delete(project);
	}

}
