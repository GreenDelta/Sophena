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
import sophena.db.daos.ProducerDao;
import sophena.db.daos.RootEntityDao;
import sophena.io.datapack.DataPack;
import sophena.io.datapack.Export;
import sophena.io.datapack.Import;
import sophena.model.HeatRecovery;
import sophena.model.Producer;

public class HeatRecoveryPackTest {

	private Path path;
	private Database db = Tests.getDb();
	private RootEntityDao<HeatRecovery> recoveryDao = new RootEntityDao<>(HeatRecovery.class, db);
	private ProducerDao producerDao = new ProducerDao(db);

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
		producerDao.insert(producer);
	}

	@After
	public void tearDown() throws Exception {
		if (producer != null) {
			producerDao.delete(producer);
		}
		if (recovery != null) {
			recoveryDao.delete(recovery);
		}
		Files.delete(path);
	}

	@Test
	public void testModel() {
		Producer p = producerDao.get(producer.id);
		Assert.assertEquals(p.heatRecovery.id, recovery.id);
	}

	@Test
	public void testPackIO() throws Exception {
		try (DataPack pack = new DataPack(path.toFile())) {
			Export export = new Export(pack);
			export.write(producer);
		}
		deleteModel();
		Import packImport = new Import(path.toFile(), db);
		packImport.run();
		testModel();
	}

	private void deleteModel() {
		producerDao.delete(producer);
		recoveryDao.delete(recovery);
		Assert.assertNull(producerDao.get(producer.id));
		Assert.assertNull(recoveryDao.get(recovery.id));
	}
}