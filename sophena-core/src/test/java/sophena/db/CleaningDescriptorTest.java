package sophena.db;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.db.daos.ProjectDao;
import sophena.model.FlueGasCleaning;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.Project;
import sophena.model.descriptors.CleaningDescriptor;

public class CleaningDescriptorTest {

	private Database db = Tests.getDb();
	private Dao<FlueGasCleaning> cDao = new Dao<>(FlueGasCleaning.class, db);
	private ProjectDao pDao = new ProjectDao(db);

	private Project project;
	private FlueGasCleaning cleaning;

	@Before
	public void setUp() {
		cleaning = new FlueGasCleaning();
		cleaning.id = UUID.randomUUID().toString();
		cleaning.name = "FlueGasCleaning 123";
		cDao.insert(cleaning);
		project = new Project();
		project.id = UUID.randomUUID().toString();
		project.name = "Project 123";
		pDao.insert(project);
	}

	@After
	public void tearDown() {
		pDao.delete(project);
		cDao.delete(cleaning);
	}

	@Test
	public void testNoDescriptors() {
		List<CleaningDescriptor> list = pDao.getCleaningDescriptors(
				project.toDescriptor());
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testFindDescriptor() {
		FlueGasCleaningEntry e = new FlueGasCleaningEntry();
		e.id = UUID.randomUUID().toString();
		e.product = cleaning;
		project.flueGasCleaningEntries.add(e);
		project = pDao.update(project);
		List<CleaningDescriptor> list = pDao.getCleaningDescriptors(
				project.toDescriptor());
		Assert.assertEquals(1, list.size());
		CleaningDescriptor d = list.get(0);
		Assert.assertEquals(e.id, d.id);
		Assert.assertEquals(cleaning.name, d.name);
	}

}
