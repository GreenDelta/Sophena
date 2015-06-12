package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ProjectDao;
import sophena.model.Project;

public class ProjectDaoTest {

	private ProjectDao dao = new ProjectDao(Tests.getDb());

	@Test
	public void testGet() {
		Project p = makeProject();
		Project o = dao.get(p.getId());
		Assert.assertEquals(p, o);
		dao.delete(o);
	}

	@Test
	public void testUpdate() {
		Project p = makeProject();
		p.setName("name 2");
		dao.update(p);
		Project o = dao.get(p.getId());
		Assert.assertEquals("name 2", o.getName());
		dao.delete(o);
	}

	@Test
	public void testDelete() {
		Project p = makeProject();
		dao.delete(p);
		Project o = dao.get(p.getId());
		Assert.assertNull(o);
	}

	private Project makeProject() {
		Project p = new Project();
		p.setId(UUID.randomUUID().toString());
		p.setName("A project");
		return dao.insert(p);
	}

}
