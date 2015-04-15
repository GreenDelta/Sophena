package sophena.io.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.Fuel;
import sophena.model.ModelType;
import sophena.model.RootEntity;
import sophena.model.WeatherStation;

public class Import implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Database db;
	private DataPack pack;

	public Import(DataPack pack, Database db) {
		this.pack = pack;
		this.db = db;
	}

	@Override
	public void run() {
		try {
			importEntities(ModelType.FUEL, Fuel.class);
			importEntities(ModelType.WEATHER_STATION, WeatherStation.class);
		} catch (Exception e) {
			log.error("failed to import data pack " + pack, e);
		}
	}

	private <T extends RootEntity> void importEntities(ModelType type,
			Class<T> clazz) {
		try {
			Gson gson = new Gson();
			Dao<T> dao = new Dao<>(clazz, db);
			for (String id : pack.getIds(type)) {
				if (dao.contains(id)) {
					log.info("{} with id={} is already exists: not imported",
							clazz, id);
					continue;
				}
				JsonObject json = pack.get(type, id);
				T instance = gson.fromJson(json, clazz);
				dao.insert(instance);
			}
		} catch(Exception e) {
			log.error("failed to import instances of " + clazz, e);
		}
	}
}
