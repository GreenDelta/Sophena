package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.Dao;
import sophena.model.Stats;
import sophena.model.WeatherStation;

public class WeatherDataIOTest {

	private Dao<WeatherStation> dao = new Dao<>(WeatherStation.class,
			Tests.getDb());

	@Test
	public void testWriteRead() {
		String id = UUID.randomUUID().toString();
		WeatherStation station = new WeatherStation();
		station.id = id;
		double[] data = new double[Stats.HOURS];
		for (int i = 0; i < data.length; i++) {
			data[i] = 10 * Math.random();
		}
		station.setData(data);
		dao.insert(station);
		WeatherStation copy = dao.get(id);
		Assert.assertArrayEquals(station.getData(), copy.getData(), 1e-10);
		dao.delete(station);
	}

}
