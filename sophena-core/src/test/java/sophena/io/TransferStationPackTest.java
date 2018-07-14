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
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.TransferStation;

public class TransferStationPackTest {

	private Path path;
	private Database db = Tests.getDb();
	private RootEntityDao<TransferStation> stationDao = new RootEntityDao<>(TransferStation.class, db);
	private ProjectDao projectDao = new ProjectDao(db);

	private TransferStation station;
	private Project project;

	@Before
	public void setUp() throws Exception {
		path = Files.createTempFile("test_fgc_pack_", ".sophena");
		Files.delete(path);
		station = new TransferStation();
		station.id = UUID.randomUUID().toString();
		stationDao.insert(station);
		project = new Project();
		project.id = UUID.randomUUID().toString();
		Consumer consumer = new Consumer();
		consumer.id = UUID.randomUUID().toString();
		consumer.transferStation = station;
		project.consumers.add(consumer);
		projectDao.insert(project);
	}

	@After
	public void tearDown() throws Exception {
		projectDao.delete(project);
		stationDao.delete(station);
		Files.deleteIfExists(path);
	}

	@Test
	public void testModel() {
		Project p = projectDao.get(project.id);
		Assert.assertEquals(1, p.consumers.size());
		Consumer c = p.consumers.get(0);
		Assert.assertEquals(station.id, c.transferStation.id);
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
		stationDao.delete(station);
		Assert.assertNull(projectDao.get(project.id));
		Assert.assertNull(stationDao.get(station.id));
	}

}