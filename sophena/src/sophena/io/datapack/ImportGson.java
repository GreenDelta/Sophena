package sophena.io.datapack;

import java.lang.reflect.Type;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.FlueGasCleaning;
import sophena.model.Fuel;
import sophena.model.HeatPump;
import sophena.model.HeatRecovery;
import sophena.model.Manufacturer;
import sophena.model.Pipe;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.ProjectFolder;
import sophena.model.RootEntity;
import sophena.model.SolarCollector;
import sophena.model.TransferStation;
import sophena.model.WeatherStation;
import sophena.utils.Strings;

/// Creates a Gson instance configured for deserializing JSON data sets.
/// References to data set entities (like Fuels, Boilers, etc.) are resolved by
/// looking them up in the given database instead of deserializing them from
/// JSON.
public class ImportGson {

	private final Database db;

	private ImportGson(Database db) {
		this.db = db;
	}

	public static Gson create(Database db) {
		return new ImportGson(db).build(null);
	}

	public static Gson create(Database db, Class<?> rootType) {
		return new ImportGson(db).build(rootType);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Gson build(Class<?> rootType) {
		var builder = new GsonBuilder();
		Class<?>[] refTypes = {
			Boiler.class, BufferTank.class, BuildingState.class,
			Fuel.class, Pipe.class, Product.class, ProductGroup.class,
			WeatherStation.class, TransferStation.class,
			FlueGasCleaning.class, ProjectFolder.class,
			HeatRecovery.class, Manufacturer.class, SolarCollector.class,
			HeatPump.class
		};
		for (Class<?> refType : refTypes) {
			if (refType.equals(rootType))
				continue;
			var ed = new Deserializer(refType);
			builder.registerTypeAdapter(refType, ed);
		}
		return builder.create();
	}

	private class Deserializer<T extends RootEntity>
		implements JsonDeserializer<T> {

		private final Class<T> type;

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
			Gson gson = build(type);
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
