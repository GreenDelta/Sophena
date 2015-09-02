package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
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
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.WeatherStation;

public class EntityTest {

	@SuppressWarnings("unchecked")
	private Class<? extends AbstractEntity>[] classes = new Class[] {
			Boiler.class,
			BufferTank.class,
			BuildingState.class,
			Consumer.class,
			CostSettings.class,
			Fuel.class,
			FuelConsumption.class,
			LoadProfile.class,
			Location.class,
			Pipe.class,
			Producer.class,
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
		T entity = clazz.newInstance();
		Dao<T> dao = new Dao<>(clazz, Tests.getDb());
		entity.id = UUID.randomUUID().toString();
		dao.insert(entity);
		T clone = dao.get(entity.id);
		Assert.assertEquals(entity, clone);
		dao.delete(entity);
		clone = dao.get(entity.id);
		Assert.assertNull(clone);
	}
}
