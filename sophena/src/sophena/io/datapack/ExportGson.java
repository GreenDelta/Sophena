package sophena.io.datapack;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
import sophena.model.Project;
import sophena.model.ProjectFolder;
import sophena.model.RootEntity;
import sophena.model.SolarCollector;
import sophena.model.TransferStation;
import sophena.model.WeatherStation;
import sophena.model.biogas.Substrate;

/// Creates a Gson instance configured for serializing JSON data sets.
/// Referenced data set entities are serialized as lightweight references.
/// Project-owned products are serialized inline when the root entity is a
/// project.
public class ExportGson {

	private ExportGson() {
	}

	public static Gson create(RootEntity root) {
		return new ExportGson().build(root);
	}

	private Gson build(RootEntity root) {
		var builder = new GsonBuilder();
		Class<?>[] refTypes = {
			Substrate.class,
			Boiler.class,
			BufferTank.class,
			BuildingState.class,
			Fuel.class,
			Pipe.class,
			Product.class,
			ProductGroup.class,
			WeatherStation.class,
			TransferStation.class,
			FlueGasCleaning.class,
			ProjectFolder.class,
			HeatRecovery.class,
			Manufacturer.class,
			SolarCollector.class,
			HeatPump.class
		};
		var serializer = new Serializer(root);
		for (Class<?> refType : refTypes) {
			if (refType.equals(root.getClass()))
				continue;
			builder.registerTypeAdapter(refType, serializer);
		}
		return builder.create();
	}

	private class Serializer implements JsonSerializer<RootEntity> {

		private final RootEntity root;

		Serializer(RootEntity root) {
			this.root = root;
		}

		@Override
		public JsonElement serialize(
			RootEntity entity, Type type, JsonSerializationContext context) {
			if (entity == null) return null;
			return entity instanceof Product product
				? handleProduct(product)
				: createRef(entity);
		}

		private JsonElement handleProduct(Product product) {
			if (product.projectId == null) {
				return createRef(product);
			}
			if (!(root instanceof Project))
				return createRef(product);
			Gson gson = build(product);
			return gson.toJsonTree(product);
		}

		private JsonElement createRef(RootEntity entity) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id", entity.id);
			obj.addProperty("name", entity.name);
			return obj;
		}
	}
}
