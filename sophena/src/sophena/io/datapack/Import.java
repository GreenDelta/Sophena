package sophena.io.datapack;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.AbstractEntity;
import sophena.model.Boiler;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.ModelType;
import sophena.model.WeatherStation;

public class Import implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Database db;
	private File packFile;
	private DataPack pack;

	public Import(File packFile, Database db) {
		this.packFile = packFile;
		this.db = db;
	}

	@Override
	public void run() {
		try {
			pack = DataPack.open(packFile);
			importEntities(ModelType.FUEL, Fuel.class);
			importEntities(ModelType.WEATHER_STATION, WeatherStation.class);
			importEntities(ModelType.BOILER, Boiler.class);
			importEntities(ModelType.COST_SETTINGS, CostSettings.class);
			pack.close();
		} catch (Exception e) {
			log.error("failed to import data pack " + pack, e);
		}
	}

	// for non-cyclic entities
	private <T extends AbstractEntity> void importEntities(ModelType type,
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
