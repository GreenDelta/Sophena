package sophena.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import sophena.model.RootEntity;

public class Json {

	private Json() {
	}

	public static boolean getBool(JsonObject obj, String member, boolean defaultVal) {
		var prim = getPrimitive(obj, member);
		return prim != null && prim.isBoolean()
			? prim.getAsBoolean()
			: defaultVal;
	}

	public static double getDouble(JsonObject obj, String member, double defaultVal) {
		var prim = getPrimitive(obj, member);
		return prim != null && prim.isNumber()
			? prim.getAsDouble()
			: defaultVal;
	}

	public static long getLong(JsonObject obj, String member, long defaultVal) {
		var prim = getPrimitive(obj, member);
		return prim != null && prim.isNumber()
			? prim.getAsLong()
			: defaultVal;
	}

	public static String getString(JsonObject obj, String member) {
		var prim = getPrimitive(obj, member);
		return prim != null && prim.isString()
			? prim.getAsString()
			: null;
	}

	private static JsonPrimitive getPrimitive(JsonObject obj, String member) {
		if (obj == null || member == null)
			return null;
		var elem = obj.get(member);
		return elem != null && elem.isJsonPrimitive()
			? elem.getAsJsonPrimitive()
			: null;
	}

	public static JsonArray getArray(JsonObject obj, String member) {
		var elem = obj.get(member);
		return elem != null && elem.isJsonArray()
			? elem.getAsJsonArray()
			: null;
	}

	public static String getRefId(JsonObject obj, String member) {
		if (obj == null || member == null)
			return null;
		var elem = obj.get(member);
		return elem != null && elem.isJsonObject()
			? getString(elem.getAsJsonObject(), "id")
			: null;
	}

	public static void putRef(JsonObject obj, String member, RootEntity e) {
		if (obj == null || member == null || e == null)
			return;
		var ref = new JsonObject();
		ref.addProperty("id", e.id);
		ref.addProperty("name", e.name);
		obj.add(member, ref);
	}
}
