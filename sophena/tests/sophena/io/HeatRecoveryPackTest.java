package sophena.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.Tests;

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
			projectDao.delete(producer);
		}
        if (recovery != null) {
            recoveryDao.delete(recovery);
        }
		Files.delete(path);
	}    
    
    @Test
    public void testModel() {
        Producer p = producerDao.get(producer.id);
        Assert.assertEquals(p.recovery.id, recovery.id);
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
        recoveryDao.delete();
        Assert.assertNull(producerDao.get(producer.id));
        Assert.assertNull(recoveryDao.get(recovery.id));
    }    
}