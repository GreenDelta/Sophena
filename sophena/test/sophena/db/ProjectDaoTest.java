package sophena.db;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ProjectDao;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;

public class ProjectDaoTest {

	private ProjectDao dao = new ProjectDao(Tests.getDb());

	@Test
	public void testGet() {
		Project p = makeProject();
		Project o = dao.get(p.id);
		Assert.assertEquals(p, o);
		dao.delete(o);
	}

	@Test
	public void testGetDescriptor() {
		Project p = makeProject();
		List<ProjectDescriptor> list = dao.getDescriptors();
		Assert.assertTrue(list.contains(p.toDescriptor()));
		dao.delete(p);
	}

	@Test
	public void testUpdate() {
		Project p = makeProject();
		p.name = "name 2";
		dao.update(p);
		Project o = dao.get(p.id);
		Assert.assertEquals("name 2", o.name);
		dao.delete(o);
	}

	@Test
	public void testDelete() {
		Project p = makeProject();
		dao.delete(p);
		Project o = dao.get(p.id);
		Assert.assertNull(o);
	}

	private Project makeProject() {
		Project p = new Project();
		p.id = UUID.randomUUID().toString();
		p.name = "A project";
		CostSettings settings = new CostSettings();
		settings.id = UUID.randomUUID().toString();
		p.costSettings = settings;
		HeatNet net = new HeatNet();
		net.id = UUID.randomUUID().toString();
		p.heatNet = net;
		return dao.insert(p);
	}

}
