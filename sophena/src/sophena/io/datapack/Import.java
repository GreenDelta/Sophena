package sophena.io.datapack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.ModelType;
import sophena.model.RootEntity;
import sophena.model.WeatherStation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

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
			importEntities(ModelType.BOILER, Boiler.class);
		} catch (Exception e) {
			log.error("failed to import data pack " + pack, e);
		}
	}

	// for non-cyclic entities
	private <T extends RootEntity> void importEntities(ModelType type,
			Class<T> clazz) {
		try {
			Gson gson = getGson(clazz);
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
		} catch (Exception e) {
			log.error("failed to import instances of " + clazz, e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Gson getGson(Class<?> rootType) {
		GsonBuilder builder = new GsonBuilder();
		Class<?>[] refTypes = {
				Fuel.class
		};
		for (Class<?> refType : refTypes) {
			if (refType.equals(rootType))
				continue;
			EntityDeserializer<?> ed = new EntityDeserializer(refType, db);
			builder.registerTypeAdapter(refType, ed);
		}
		return builder.create();
	}
}
