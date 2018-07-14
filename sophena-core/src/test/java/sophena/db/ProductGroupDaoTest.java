package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ProductGroupDao;
import sophena.model.ProductGroup;
import sophena.model.ProductType;

public class ProductGroupDaoTest {

	private ProductGroupDao dao = new ProductGroupDao(Tests.getDb());

	@Test
	public void testGetAllForType() {
		ProductGroup g = new ProductGroup();
		g.id = UUID.randomUUID().toString();
		g.type = ProductType.BIOMASS_BOILER;
		dao.insert(g);
		Assert.assertTrue(dao.getAll(ProductType.BIOMASS_BOILER).contains(g));
		Assert.assertFalse(dao.getAll(ProductType.BUILDING).contains(g));
		dao.delete(g);
	}

}
