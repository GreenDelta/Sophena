package sophena.db;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ProducerDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProducerDescriptor;

public class ProducerDescriptorTest {

	private ProjectDao dao = new ProjectDao(Tests.getDb());
	private Project project;

	@Before
	public void setUp() {
		project = new Project();
		project.id = UUID.randomUUID().toString();
		project.name = "Test project";
		dao.insert(project);
	}

	@After
	public void tearDown() {
		dao.delete(project);
	}

	@Test
	public void testFindWithRanks() {
		addProducer("P 41", 41);
		addProducer("P 42", 42);
		ProducerDao pDao = new ProducerDao(Tests.getDb());
		List<ProducerDescriptor> list = pDao.getDescriptors(project.toDescriptor());
		Assert.assertEquals(2, list.size());
		for (ProducerDescriptor d : list) {
			if (d.rank == 41) {
				Assert.assertEquals("P 41", d.name);
			} else if (d.rank == 42) {
				Assert.assertEquals("P 42", d.name);
			} else {
				Assert.fail("Unexpected rank: " + d.rank);
			}
		}
	}

	private void addProducer(String name, int rank) {
		Producer p = new Producer();
		p.id = UUID.randomUUID().toString();
		p.name = name;
		p.rank = rank;
		project.producers.add(p);
		project = dao.update(project);
	}

}
