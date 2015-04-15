package sophena.io.datapack;

import java.lang.reflect.Type;

import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.RootEntity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

class EntityDeserializer<T extends RootEntity> implements JsonDeserializer<T> {

	private Database db;
	private Class<T> type;

	EntityDeserializer(Class<T> type, Database db) {
		this.type = type;
		this.db = db;
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
