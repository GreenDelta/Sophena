package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.db.daos.ProductDao;
import sophena.model.Boiler;
import sophena.model.Product;
import sophena.model.ProductType;

public class ProductTest {

	@Test
	public void testBoilers() throws Exception {
		Dao<Boiler> dao = new Dao<>(Boiler.class, Tests.getDb());
		Boiler b = new Boiler();
		b.id = UUID.randomUUID().toString();
		b.type = ProductType.BIOMASS_BOILER;
		dao.insert(b);
		b = dao.get(b.id);
		Assert.assertTrue(b.type == ProductType.BIOMASS_BOILER);
		dao.delete(b);
	}

	@Test
	public void testGetProductForType() throws Exception {
		ProductDao dao = new ProductDao(Tests.getDb());
		Product p1 = createProduct(ProductType.BIOMASS_BOILER, dao);
		Assert.assertTrue(dao.getAll().contains(p1));
		Assert.assertTrue(dao.getAllGlobal(ProductType.BIOMASS_BOILER).contains(p1));
		Assert.assertFalse(dao.getAllGlobal(ProductType.BUFFER_TANK).contains(p1));
		dao.delete(p1);
	}

	private Product createProduct(ProductType type, ProductDao dao) {
		Product p = new Product();
		p.id = UUID.randomUUID().toString();
		p.type = type;
		return dao.insert(p);
	}

}
