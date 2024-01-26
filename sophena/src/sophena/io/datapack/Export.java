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
import sophena.model.HeatRecovery;
import sophena.model.Manufacturer;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.Project;
import sophena.model.ProjectFolder;
import sophena.model.RootEntity;
import sophena.model.SolarCollector;
import sophena.model.TransferStation;
import sophena.model.WeatherStation;

public class Export {

	private DataPack pack;

	public Export(DataPack pack) {
		this.pack = pack;
		pack.writeInfo();
	}

	public void write(RootEntity root) {
		if (root == null)
			return;
		Gson gson = getGson(root);
		ModelType modelType = ModelType.forModelClass(root.getClass());
		String json = gson.toJson(root);
		pack.put(modelType, root.id, json);
	}

	private Gson getGson(RootEntity root) {
		GsonBuilder builder = new GsonBuilder();
		Class<?>[] refTypes = {
				Boiler.class, BufferTank.class, BuildingState.class,
				Fuel.class, Pipe.class, Product.class, ProductGroup.class,
				WeatherStation.class, TransferStation.class,
				FlueGasCleaning.class, ProjectFolder.class,
				HeatRecovery.class, Manufacturer.class, SolarCollector.class
		};
		Serializer ed = new Serializer(root);
		for (Class<?> refType : refTypes) {
			if (refType.equals(root.getClass()))
				continue;
			builder.registerTypeAdapter(refType, ed);
		}
		return builder.create();
	}

	private class Serializer implements JsonSerializer<RootEntity> {

		RootEntity root;

		Serializer(RootEntity root) {
			this.root = root;
		}

		@Override
		public JsonElement serialize(RootEntity entity, Type type,
				JsonSerializationContext context) {
			if (entity == null)
				return null;
			if (entity instanceof Product)
				return handleProduct((Product) entity);
			write(entity);
			return createRef(entity);
		}

		/**
		 * The handling of general products is a bit special: project own
		 * products are directly serialized into the JSON tree of the project
		 * while shared products are serialized into the `products` folder.
		 */
		private JsonElement handleProduct(Product product) {
			if (product.projectId == null) { // shared product
				write(product);
				return createRef(product);
			}
			if (!(root instanceof Project))
				return createRef(product); // product entries
			// write owned product into project
			Gson gson = getGson(product);
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
