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
import sophena.db.daos.ProductDao;
import sophena.db.daos.ProjectDao;
import sophena.io.datapack.DataPack;
import sophena.io.datapack.Export;
import sophena.io.datapack.Import;
import sophena.model.Product;
import sophena.model.ProductEntry;
import sophena.model.Project;

/**
 * Test the import and export of shared and private project products.
 */
public class ProductsPackTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Path path;
	private ProjectDao projectDao = new ProjectDao(Tests.getDb());
	private ProductDao productDao = new ProductDao(Tests.getDb());

	private Project project;

	@Before
	public void setUp() throws Exception {
		path = Files.createTempFile("test_products_pack_", ".sophena");
		Files.delete(path);
		project = createModel();
	}

	@After
	public void tearDown() throws Exception {
		if (project != null) {
			projectDao.delete(project);
		}
		Files.deleteIfExists(path);
	}

	/**
	 * Creates the test model: a project with references to a shared product and
	 * a project private product.
	 */
	private Project createModel() {

		// shared product
		Product shared = new Product();
		shared.id = UUID.randomUUID().toString();
		shared.name = "Shared product";
		productDao.insert(shared);

		// project
		Project project = new Project();
		project.id = UUID.randomUUID().toString();
		project.name = "Project";

		// own product
		Product own = new Product();
		own.id = UUID.randomUUID().toString();
		own.name = "Own product";
		own.projectId = project.id;
		project.ownProducts.add(own);

		// product entries
		ProductEntry sharedEntry = new ProductEntry();
		sharedEntry.id = UUID.randomUUID().toString();
		sharedEntry.product = shared;
		sharedEntry.count = 24;
		project.productEntries.add(sharedEntry);

		ProductEntry ownEntry = new ProductEntry();
		ownEntry.id = UUID.randomUUID().toString();
		ownEntry.product = own;
		ownEntry.count = 42;
		project.productEntries.add(ownEntry);

		return projectDao.insert(project);
	}

	@Test
	public void testModel() {
		testModel(project);
	}

	@Test
	public void testClone() {
		Project clone = project.clone();
		projectDao.insert(clone);
		testModel(clone);
		Assert.assertNotEquals(project.ownProducts.get(0),
				clone.ownProducts.get(0));
		projectDao.delete(clone);
	}

	@Test
	public void testPackIO() throws Exception {
		try (DataPack pack = new DataPack(path.toFile())) {
			log.trace("Created data package: {}", path);
			Export export = new Export(pack);
			export.write(project);
		}
		projectDao.delete(project);
		Assert.assertNull(projectDao.get(project.id));
		Assert.assertNull(productDao.get(project.ownProducts.get(0).id));
		Import packImport = new Import(path.toFile(), Tests.getDb());
		packImport.run();
		project = projectDao.get(project.id);
		testModel(project);
	}

	private void testModel(Project project) {
		Assert.assertEquals(1, project.ownProducts.size());
		Product own = project.ownProducts.get(0);
		Assert.assertEquals("Own product", own.name);
		Assert.assertEquals(2, project.productEntries.size());
		for (ProductEntry entry : project.productEntries) {
			if (Objects.equals(own, entry.product)) {
				Assert.assertEquals(42, entry.count, 1e-15);
			} else {
				Assert.assertEquals("Shared product", entry.product.name);
				Assert.assertEquals(24, entry.count, 1e-15);
			}
		}
	}

}
