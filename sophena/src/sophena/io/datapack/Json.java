package sophena.io.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sophena.model.RootEntity;

class Json {

	private Json() {
	}

	static boolean getBool(JsonObject obj, String member, boolean defaultVal) {
		if (obj == null || member == null)
			return defaultVal;
		JsonElement elem = obj.get(member);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		return elem.getAsBoolean();
	}

	static String getString(JsonObject obj, String member) {
		if (obj == null || member == null)
			return null;
		JsonElement elem = obj.get(member);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		return elem.getAsString();
	}

	static String getRefID(JsonObject obj, String member) {
		if (obj == null || member == null)
			return null;
		JsonElement elem = obj.get(member);
		if (elem == null || !elem.isJsonObject())
			return null;
		return getString(elem.getAsJsonObject(), "id");
	}

	static void putRef(JsonObject obj, String member, RootEntity e) {
		if (obj == null || member == null || e == null)
			return;
		JsonObject ref = new JsonObject();
		obj.add(member, ref);
		ref.addProperty("id", e.id);
		ref.addProperty("name", e.name);
	}

}
