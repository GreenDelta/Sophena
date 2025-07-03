package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.db.daos.ProducerDao;
import sophena.model.AbstractEntity;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.LoadProfile;
import sophena.model.Location;
import sophena.model.Manufacturer;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.ProducerProfile;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductGroup;
import sophena.model.Project;
import sophena.model.WeatherStation;

public class EntityTest {

	@SuppressWarnings("unchecked")
	private final Class<? extends AbstractEntity>[] classes = new Class[]{
			Boiler.class,
			BufferTank.class,
			BuildingState.class,
			Consumer.class,
			CostSettings.class,
			Fuel.class,
			FuelConsumption.class,
			LoadProfile.class,
			Location.class,
			Manufacturer.class,
			Pipe.class,
			Producer.class,
			ProducerProfile.class,
			Product.class,
			ProductEntry.class,
			ProductGroup.class,
			Project.class,
			WeatherStation.class,
	};

	@Test
	public void testAll() throws Exception {
		for (Class<? extends AbstractEntity> clazz : classes) {
			test(clazz);
		}
	}

	private <T extends AbstractEntity> void test(Class<T> clazz)
			throws Exception {
		T entity = clazz.getDeclaredConstructor().newInstance();
		Dao<T> dao = new Dao<>(clazz, Tests.getDb());
		entity.id = UUID.randomUUID().toString();
		dao.insert(entity);
		T clone = dao.get(entity.id);
		Assert.assertEquals(entity, clone);
		dao.delete(entity);
		clone = dao.get(entity.id);
		Assert.assertNull(clone);
	}

	@Test
	public void testInitEmbedded() {
		Producer p = new Producer();
		p.id = UUID.randomUUID().toString();
		p.costs = new ProductCosts();
		p.heatRecoveryCosts = new ProductCosts();
		ProducerDao dao = new ProducerDao(Tests.getDb());
		dao.insert(p);
		dao.update(p);
		p = dao.get(p.id);
		Assert.assertNotNull(p.costs);
		Assert.assertNotNull(p.heatRecoveryCosts);
	}
}
