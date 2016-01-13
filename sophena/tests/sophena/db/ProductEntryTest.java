package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ProductDao;
import sophena.db.daos.ProjectDao;
import sophena.model.AbstractEntity;
import sophena.model.Product;
import sophena.model.ProductEntry;
import sophena.model.Project;
import sophena.model.RootEntity;

public class ProductEntryTest {

	private ProjectDao projDao = new ProjectDao(Tests.getDb());
	private ProductDao prodDao = new ProductDao(Tests.getDb());

	@Test
	public void testDeletePrivateProduct() {
		Project project = mk(Project.class);
		Product product = mk(Product.class);
		product.projectId = project.id;
		project.ownProducts.add(product);
		ProductEntry entry = mk(ProductEntry.class);
		entry.product = product;
		project.productEntries.add(entry);
		projDao.insert(project);
		checkFind(product, true);
		projDao.delete(project);
		checkFind(product, false);
	}

	@Test
	public void testNotDeleteGlobalProduct() {
		Product product = mk(Product.class);
		prodDao.insert(product);
		checkFind(product, true);
		Project project = mk(Project.class);
		ProductEntry entry = mk(ProductEntry.class);
		entry.product = product;
		project.productEntries.add(entry);
		projDao.insert(project);
		checkFind(product, true);
		projDao.delete(project);
		checkFind(product, true);
		prodDao.delete(product);
		checkFind(product, false);
	}

	private void checkFind(Product product, boolean shouldBePresent) {
		if (shouldBePresent)
			Assert.assertTrue(prodDao.getAll().contains(product));
		else
			Assert.assertFalse(prodDao.getAll().contains(product));
	}

	private <T extends AbstractEntity> T mk(Class<T> type) {
		try {
			T t = type.newInstance();
			t.id = UUID.randomUUID().toString();
			if (t instanceof RootEntity) {
				RootEntity re = (RootEntity) t;
				re.name = "Test " + type + " " + t.id.substring(0, 4);
			}
			return t;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
