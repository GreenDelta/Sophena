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
import sophena.db.daos.Dao;
import sophena.db.daos.ProjectDao;
import sophena.io.datapack.DataPack;
import sophena.io.datapack.Export;
import sophena.io.datapack.Import;
import sophena.model.Consumer;
import sophena.model.Manufacturer;
import sophena.model.Project;
import sophena.model.TransferStation;

public class ManufacturerPackTest {

	private Path path;
	private Database db = Tests.getDb();

	private Dao<Manufacturer> manufacturerDao = new Dao<>(Manufacturer.class, db);
	private Dao<TransferStation> stationDao = new Dao<>(TransferStation.class, db);
	private ProjectDao projectDao = new ProjectDao(db);

	private Manufacturer manufacturer;
	private TransferStation station;
	private Project project;

	@Before
	public void setUp() throws Exception {
		path = Files.createTempFile("test_fgc_pack_", ".sophena");
		Files.delete(path);
		manufacturer = new Manufacturer();
		manufacturer.id = UUID.randomUUID().toString();
		manufacturerDao.insert(manufacturer);
		station = new TransferStation();
		station.id = UUID.randomUUID().toString();
		station.manufacturer = manufacturer;
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
		manufacturerDao.delete(manufacturer);
		Files.deleteIfExists(path);
	}

	@Test
	public void testModel() {
		Project p = projectDao.get(project.id);
		Assert.assertEquals(1, p.consumers.size());
		Consumer c = p.consumers.get(0);
		Assert.assertEquals(station.id, c.transferStation.id);
		Manufacturer m = c.transferStation.manufacturer;
		Assert.assertEquals(manufacturer.id, m.id);
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
		manufacturerDao.delete(manufacturer);
		Assert.assertNull(projectDao.get(project.id));
		Assert.assertNull(stationDao.get(station.id));
		Assert.assertNull(manufacturerDao.get(manufacturer.id));
	}

}
