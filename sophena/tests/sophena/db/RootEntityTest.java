package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.RootEntityDao;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.Project;
import sophena.model.RootEntity;
import sophena.model.WeatherStation;

public class RootEntityTest {

	@SuppressWarnings("unchecked")
	private Class<? extends RootEntity>[] classes = new Class[] {
			BuildingState.class,
			Consumer.class,
			Fuel.class,
			Project.class,
			WeatherStation.class,
			Boiler.class,
			Pipe.class,
			ProductGroup.class,
			BufferTank.class,
			Producer.class,
			Product.class
	};

	@Test
	public void testAll() throws Exception {
		for (Class<? extends RootEntity> clazz : classes) {
			test(clazz);
		}
	}

	private <T extends RootEntity> void test(Class<T> clazz) throws Exception {
		T entity = clazz.newInstance();
		RootEntityDao<T> dao = new RootEntityDao<>(clazz, Tests.getDb());
		entity.id = UUID.randomUUID().toString();
		entity.name = "test";
		dao.insert(entity);
		T clone = dao.get(entity.id);
		Assert.assertEquals(entity, clone);
		dao.delete(entity);
		clone = dao.get(entity.id);
		Assert.assertNull(clone);
	}
}
