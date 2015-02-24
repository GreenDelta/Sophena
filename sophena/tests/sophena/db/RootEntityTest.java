package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.Project;
import sophena.model.RootEntity;
import sophena.model.WeatherStation;

public class RootEntityTest {

	@SuppressWarnings("unchecked")
	private Class<? extends RootEntity>[] classes = new Class[] {
			BuildingState.class,
			BuildingType.class,
			Consumer.class,
			Fuel.class,
			Project.class,
			WeatherStation.class
	};

	@Test
	public void testAll() throws Exception {
		for (Class<? extends RootEntity> clazz : classes) {
			test(clazz);
		}
	}

	private <T extends RootEntity> void test(Class<T> clazz) throws Exception {
		T entity = clazz.newInstance();
		Dao<T> dao = new Dao<>(clazz, Tests.getDb());
		entity.setId(UUID.randomUUID().toString());
		entity.setName("test");
		dao.insert(entity);
		T clone = dao.get(entity.getId());
		Assert.assertEquals(entity, clone);
		dao.delete(entity);
		clone = dao.get(entity.getId());
		Assert.assertNull(clone);
	}
}
