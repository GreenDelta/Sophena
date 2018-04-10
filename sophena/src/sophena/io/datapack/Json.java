package sophena.io.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

}
