package sophena.db;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ProjectDao;
import sophena.model.CostSettings;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;

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
	public void testGetDescriptor() {
		Project p = makeProject();
		List<ProjectDescriptor> list = dao.getDescriptors();
		Assert.assertTrue(list.contains(p.toDescriptor()));
		dao.delete(p);
	}

	@Test
	public void testGetVariantDescriptors() {
		Project p = makeProject();
		List<ProjectDescriptor> list = dao.getVariantDescriptors(p
				.toDescriptor());
		Assert.assertEquals(1, list.size());
		dao.delete(p);
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
		CostSettings settings = new CostSettings();
		settings.setId(UUID.randomUUID().toString());
		p.setCostSettings(settings);
		p.getVariants().add(p.clone());
		return dao.insert(p);
	}

}
