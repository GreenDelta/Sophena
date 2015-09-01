package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.db.daos.ProjectDao;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Project;

public class HeatNetPipeTest {

	private ProjectDao dao = new ProjectDao(Tests.getDb());
	private Dao<HeatNetPipe> pDao = new Dao<>(HeatNetPipe.class, Tests.getDb());

	@Test
	public void testCrud() throws Exception {
		Project p = insertProject();
		HeatNetPipe clone = pDao.get(p.id);
		Assert.assertEquals(42, clone.length, 1e-16);
		dao.delete(p);
		clone = pDao.get(p.id);
		Assert.assertNull(clone);
	}

	@Test
	public void testDeletePipe() throws Exception {
		Project p = insertProject();
		Assert.assertEquals(42, p.heatNet.pipes.get(0).length, 1e-16);
		p.heatNet.pipes.remove(0);
		dao.update(p);
		p = dao.get(p.id);
		Assert.assertTrue(p.heatNet.pipes.isEmpty());
		dao.delete(p);
	}

	private Project insertProject() {
		Project p = new Project();
		p.id = UUID.randomUUID().toString();
		dao.insert(p);
		p = dao.get(p.id);
		p.heatNet = new HeatNet();
		HeatNetPipe pipe = new HeatNetPipe();
		p.heatNet.pipes.add(pipe);
		pipe.id = p.id;
		pipe.length = 42;
		p = dao.update(p);
		return p;
	}

}
