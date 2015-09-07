package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.model.Boiler;
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

}
