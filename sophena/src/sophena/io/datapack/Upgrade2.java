package sophena.io.datapack;

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
	}

}
