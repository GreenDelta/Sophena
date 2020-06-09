package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.db.daos.ProductDao;
import sophena.db.daos.ProjectDao;
import sophena.model.ModelType;
import sophena.model.Product;
import sophena.model.ProductEntry;
import sophena.model.Project;

public class ProductUsageTest {

	private Database db = Tests.getDb();
	private ProductDao dao = new ProductDao(Tests.getDb());
	private Product product;

	@Before
	public void setUp() {
		product = new Product();
		product.id = UUID.randomUUID().toString();
		dao.insert(product);
	}

	@After
	public void tearDown() {
		dao.delete(product);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(product);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		Project project = new Project();
		project.id = UUID.randomUUID().toString();
		ProductEntry entry = new ProductEntry();
		entry.id = UUID.randomUUID().toString();
		entry.product = product;
		project.productEntries.add(entry);
		ProjectDao projectDao = new ProjectDao(db);
		projectDao.insert(project);
		List<SearchResult> list = new UsageSearch(db).of(product);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(project.id, r.id);
		Assert.assertEquals(ModelType.PROJECT, r.type);
		projectDao.delete(project);
	}

}
