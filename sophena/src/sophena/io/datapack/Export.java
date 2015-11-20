package sophena.io.datapack;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import sophena.model.AbstractEntity;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.Fuel;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.RootEntity;
import sophena.model.WeatherStation;

public class Export {

	private DataPack pack;

	public Export(DataPack pack) {
		this.pack = pack;
	}

	public void write(AbstractEntity entity) {
		if (entity == null)
			return;
		Class<?> type = entity.getClass();
		Gson gson = getGson(type);
		ModelType modelType = ModelType.forModelClass(type);
		String json = gson.toJson(entity);
		pack.put(modelType, entity.id, json);
	}

	private Gson getGson(Class<?> rootType) {
		GsonBuilder builder = new GsonBuilder();
		Class<?>[] refTypes = {
				Boiler.class, BufferTank.class, BuildingState.class,
				Fuel.class, Pipe.class, Product.class, ProductGroup.class,
				WeatherStation.class,
		};
		Serializer ed = new Serializer();
		for (Class<?> refType : refTypes) {
			if (refType.equals(rootType))
				continue;
			builder.registerTypeAdapter(refType, ed);
		}
		return builder.create();
	}

	private class Serializer implements JsonSerializer<RootEntity> {
		@Override
		public JsonElement serialize(RootEntity entity, Type type,
				JsonSerializationContext context) {
			if (entity == null)
				return null;
			write(entity);
			JsonObject obj = new JsonObject();
			obj.addProperty("id", entity.id);
			obj.addProperty("name", entity.name);
			return obj;
		}
	}

}
