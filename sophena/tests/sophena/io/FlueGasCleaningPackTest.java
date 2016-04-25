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
       FlueGasCleaning cleaning = new FlueGasCleaning();
       cleaning.id = UUID.randomUUID().toString();
       cleaningDao.insert(cleaning);
       Project project = new Project();
       project.id = UUID.randomUUID().toString();
       FlueGasCleaningEntry entry = new FlueGasCleaningEntry();
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
		Files.delete(path);
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
    