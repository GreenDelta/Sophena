package sophena.io.datapack;

import java.util.UUID;

import com.google.gson.JsonObject;

import sophena.model.ModelType;

class Upgrade2 implements Upgrade {

	public void on(ModelType type, JsonObject obj) {
		if (type == null || obj == null)
			return;
		switch (type) {
		case PROJECT:
			upgradeProject(obj);
			break;

		default:
			break;
		}
	}

	private void upgradeProject(JsonObject obj) {
		JsonObject heatNet = obj.getAsJsonObject("heatNet");
		if (heatNet != null) {
			upgradeHeatNet(heatNet);
		}
	}

	private void upgradeHeatNet(JsonObject obj) {
		obj.addProperty("bufferLoss", 0.15);
		if (Json.getBool(obj, "withInterruption", false)) {
			obj.remove("withInterruption");
			JsonObject time = new JsonObject();
			obj.add("interruption", time);
			time.addProperty("id", UUID.randomUUID().toString());
			time.addProperty("start", Json.getString(obj, "interruptionStart"));
			time.addProperty("end", Json.getString(obj, "interruptionEnd"));
			obj.remove("interruptionStart");
			obj.remove("interruptionEnd");
		}
	}

}
