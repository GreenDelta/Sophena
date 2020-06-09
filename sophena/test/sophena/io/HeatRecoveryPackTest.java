package sophena.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.db.daos.ProjectDao;
import sophena.db.daos.RootEntityDao;
import sophena.io.datapack.DataPack;
import sophena.io.datapack.Export;
import sophena.io.datapack.Import;
import sophena.model.HeatRecovery;
import sophena.model.Producer;
import sophena.model.Project;

public class HeatRecoveryPackTest {

	private Path path;
	private Database db = Tests.getDb();
	private RootEntityDao<HeatRecovery> recoveryDao = new RootEntityDao<>(HeatRecovery.class, db);
	private ProjectDao projectDao = new ProjectDao(db);

	private Project project;
	private Producer producer;
	private HeatRecovery recovery;

	@Before
	public void setUp() throws Exception {
		path = Files.createTempFile("test_hr_pack_", ".sophena");
		Files.delete(path);

		recovery = new HeatRecovery();
		recovery.id = UUID.randomUUID().toString();
		recoveryDao.insert(recovery);

		producer = new Producer();
		producer.id = UUID.randomUUID().toString();
		producer.heatRecovery = recovery;

		project = new Project();
		project.id = UUID.randomUUID().toString();
		project.producers.add(producer);

		projectDao.insert(project);
	}

	@After
	public void tearDown() throws Exception {
		if (project != null) {
			projectDao.delete(project);
		}
		if (recovery != null) {
			recoveryDao.delete(recovery);
		}
		Files.deleteIfExists(path);
	}

	@Test
	public void testModel() {
		Project proj = projectDao.get(project.id);
		Assert.assertNotNull(proj);
		Assert.assertEquals(1, proj.producers.size());
		Producer p = project.producers.get(0);
		Assert.assertEquals(p.heatRecovery.id, recovery.id);
	}

	@Test
	public void testPackIO() throws Exception {
		try (DataPack pack = new DataPack(path.toFile())) {
			Export export = new Export(pack);
			export.write(project);
		}
		deleteModel();
		Import packImport = new Import(path.toFile(), db);
		packImport.run();
		testModel();
	}

	private void deleteModel() {
		projectDao.delete(project);
		recoveryDao.delete(recovery);
		Assert.assertNull(projectDao.get(project.id));
		Assert.assertNull(recoveryDao.get(recovery.id));
	}
}