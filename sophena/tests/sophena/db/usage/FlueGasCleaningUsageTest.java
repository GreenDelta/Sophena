package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;

public class FlueGasCleaningUsageTest {
    
    private Database db = Tests.getDb();
    private Dao<FlueGasCleaning> dao = new Dao<>(FlueGasCleaning.class, db);
    private FlueGasCleaning cleaning;
    
    @Before
    public void setUp() {
        cleaning = new FlueGasCleaning();
        cleaning.id = UUID.randomUUID().toString();
        cleaning.name = "FlueGasCleaning 123";
    }
    
    @After
	public void tearDown() {
		dao.delete(cleaning);
	}
    
    @Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(cleaning);
		Assert.assertTrue(list.isEmpty());
	}
    
    @Test
	public void testUsed() {
        
        Project p = new Project();
        p.id = UUID.randomUUID().toString();
        p.name = "Project 123";
        ProjectDao pDao = new ProjectDao(db);
        pDao.insert(p);
        
        FlueGasCleaningEntry e = new FlueGasCleaningEntry();
        e.product = cleaning;
        p.flueGasCleaningEntries.add(cleaning);
        pDao.update(p);
        
        List<SearchResult> list = new UsageSearch(db).of(cleaning);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(p.id, r.id);
		Assert.assertEquals(p.name, r.name);
		Assert.assertEquals(ModelType.PROJECT, r.type);
		pDao.delete(p);            
    }    
}