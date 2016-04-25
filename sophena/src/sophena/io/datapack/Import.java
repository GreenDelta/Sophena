package sophena.io.datapack;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Objects;

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
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.Project;
import sophena.model.RootEntity;
import sophena.model.TransferStation;
import sophena.model.WeatherStation;
import sophena.rcp.utils.Strings;

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
		try (DataPack pack = DataPack.open(packFile)) {
			this.pack = pack;
			// order is important for reference resolving
			importEntities(ModelType.PRODUCT_GROUP, ProductGroup.class);
			importEntities(ModelType.PIPE, Pipe.class);
			importEntities(ModelType.PRODUCT, Product.class);
			importEntities(ModelType.FUEL, Fuel.class);
			importEntities(ModelType.BUFFER, BufferTank.class);
			importEntities(ModelType.BOILER, Boiler.class);
			importEntities(ModelType.BUILDING_STATE, BuildingState.class);
			importEntities(ModelType.COST_SETTINGS, CostSettings.class);
			importEntities(ModelType.WEATHER_STATION, WeatherStation.class);
			importEntities(ModelType.TRANSFER_STATION, TransferStation.class);
			importEntities(ModelType.FLUE_GAS_CLEANING, FlueGlassCleaning.class);
			importEntities(ModelType.HEAT_RECOVERY, HeatRecovery.class);
			importEntities(ModelType.PROJECT, Project.class);
		} catch (Exception e) {
			log.error("failed to import data pack " + pack, e);
		}
	}

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
				Fuel.class, Pipe.class, Product.class, ProductGroup.class,
				WeatherStation.class, TransferStation.class, FlueGlassCleaning.class,
				HeatRecovery.class
		};
		for (Class<?> refType : refTypes) {
			if (refType.equals(rootType))
				continue;
			Deserializer<?> ed = new Deserializer(refType);
			builder.registerTypeAdapter(refType, ed);
		}
		return builder.create();
	}

	private class Deserializer<T extends RootEntity> implements JsonDeserializer<T> {

		private Class<T> type;

		Deserializer(Class<T> type) {
			this.type = type;
		}

		@Override
		public T deserialize(JsonElement json, Type type,
				JsonDeserializationContext context) throws JsonParseException {
			if (json == null || !json.isJsonObject())
				return null;
			if (Objects.equals(this.type, Product.class))
				return handleProduct(json);
			else
				return loadReference(json);
		}

		/**
		 * See also the documentation in the export for more information about
		 * how products are stored.
		 */
		private T handleProduct(JsonElement json) {
			JsonElement projectIdElem = json.getAsJsonObject().get("projectId");
			if (projectIdElem == null)
				return loadReference(json);
			String projectId = projectIdElem.getAsString();
			if (Strings.nullOrEmpty(projectId))
				return loadReference(json);
			Gson gson = getGson(type);
			return gson.fromJson(json, type);
		}

		private T loadReference(JsonElement json) {
			JsonElement idElem = json.getAsJsonObject().get("id");
			if (idElem == null)
				return null;
			Dao<T> dao = new Dao<>(this.type, db);
			return dao.get(idElem.getAsString());
		}
	}
}
