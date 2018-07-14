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
import sophena.model.FlueGasCleaning;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.Project;

public class FlueGasCleaningPackTest {

	private Path path;
	private Database db = Tests.getDb();
	private RootEntityDao<FlueGasCleaning> cleaningDao = new RootEntityDao<>(FlueGasCleaning.class, db);
	private ProjectDao projectDao = new ProjectDao(db);

	private FlueGasCleaning cleaning;
	private Project project;

	@Before
	public void setUp() throws Exception {
		path = Files.createTempFile("test_fgc_pack_", ".sophena");
		Files.delete(path);
		cleaning = new FlueGasCleaning();
		cleaning.id = UUID.randomUUID().toString();
		cleaningDao.insert(cleaning);
		project = new Project();
		project.id = UUID.randomUUID().toString();
		FlueGasCleaningEntry entry = new FlueGasCleaningEntry();
		entry.id = UUID.randomUUID().toString();
		entry.product = cleaning;
		project.flueGasCleaningEntries.add(entry);
		projectDao.insert(project);
	}

	@After
	public void tearDown() throws Exception {
		if (project != null) {
			projectDao.delete(project);
		}
		if (cleaning != null) {
			cleaningDao.delete(cleaning);
		}
		Files.deleteIfExists(path);
	}

	@Test
	public void testModel() {
		Project p = projectDao.get(project.id);
		Assert.assertEquals(1, p.flueGasCleaningEntries.size());
		FlueGasCleaningEntry e = p.flueGasCleaningEntries.get(0);
		Assert.assertEquals(cleaning.id, e.product.id);
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
		cleaningDao.delete(cleaning);
		Assert.assertNull(projectDao.get(project.id));
		Assert.assertNull(cleaningDao.get(cleaning.id));
	}
}
