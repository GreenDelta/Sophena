package sophena.io.datapack;

import java.io.File;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.AbstractEntity;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.LoadProfile;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.Project;
import sophena.model.RootEntity;
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
			// order is important for reference resolving
			importEntities(ModelType.PRODUCT_GROUP, ProductGroup.class);
			importEntities(ModelType.FUEL, Fuel.class);
			importEntities(ModelType.BOILER, Boiler.class);
			importEntities(ModelType.BUFFER, BufferTank.class);
			importEntities(ModelType.BUILDING_STATE, BuildingState.class);
			importEntities(ModelType.COST_SETTINGS, CostSettings.class);
			importEntities(ModelType.PIPE, Pipe.class);
			importEntities(ModelType.WEATHER_STATION, WeatherStation.class);
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
				Boiler.class, BufferTank.class, BuildingState.class,
				Consumer.class, Fuel.class, LoadProfile.class, Pipe.class,
				Producer.class, Product.class, ProductGroup.class,
				Project.class, WeatherStation.class,
		};
		for (Class<?> refType : refTypes) {
			if (refType.equals(rootType))
				continue;
			Deserializer<?> ed = new Deserializer(refType);
			builder.registerTypeAdapter(refType, ed);
		}
		return builder.create();
	}

	private class Deserializer<T extends RootEntity>
			implements JsonDeserializer<T> {

		private Class<T> type;

		Deserializer(Class<T> type) {
			this.type = type;
		}

		@Override
		public T deserialize(JsonElement json, Type type,
				JsonDeserializationContext context) throws JsonParseException {
			if (json == null || !json.isJsonObject())
				return null;
			JsonElement idElem = json.getAsJsonObject().get("id");
			if (idElem == null)
				return null;
			Dao<T> dao = new Dao<>(this.type, db);
			return dao.get(idElem.getAsString());
		}
	}
}
