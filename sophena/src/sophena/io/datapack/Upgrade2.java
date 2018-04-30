package sophena.io.datapack;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import sophena.db.Database;
import sophena.db.daos.BoilerDao;
import sophena.db.daos.FuelDao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.ModelType;
import sophena.rcp.Labels;

class Upgrade2 implements Upgrade {

	private final Database db;
	private Logger log = LoggerFactory.getLogger(getClass());

	Upgrade2(Database db) {
		this.db = db;
	}

	public void on(ModelType type, JsonObject obj) {
		if (type == null || obj == null)
			return;
		switch (type) {
		case FUEL:
			upgradeFuel(obj);
			break;
		case PROJECT:
			upgradeProject(obj);
			break;
		default:
			break;
		}
	}

	private void upgradeFuel(JsonObject obj) {
		if (obj == null)
			return;
		if (Json.getBool(obj, "wood", false)) {
			obj.remove("wood");
			obj.addProperty("calorificValue",
					Json.getDouble(obj, "calorificValue", 0.0) * 1000.0);
			obj.addProperty("unit", "t atro");
			obj.addProperty("group", FuelGroup.WOOD.name());
		} else {
			// try to find a fuel group; at least for the
			// standard fuels we can try to infer it from the name
			String name = Json.getString(obj, "name");
			if (name == null)
				return;
			for (FuelGroup g : FuelGroup.values()) {
				String label = Labels.get(g);
				if (label == null)
					continue;
				if (name.equalsIgnoreCase(label)) {
					obj.addProperty("group", g.name());
					break;
				}
			}
		}
	}

	private void upgradeProject(JsonObject obj) {
		if (obj == null)
			return;
		JsonObject heatNet = obj.getAsJsonObject("heatNet");
		if (heatNet != null) {
			upgradeHeatNet(heatNet);
		}
		JsonArray producers = obj.getAsJsonArray("producers");
		if (producers != null) {
			producers.forEach(p -> upgradeProducer(p.getAsJsonObject()));
		}
	}

	private void upgradeHeatNet(JsonObject obj) {
		if (obj == null)
			return;
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

	private void upgradeProducer(JsonObject obj) {
		if (obj == null)
			return;
		String boilerID = Json.getRefID(obj, "boiler");
		Boiler boiler = new BoilerDao(db).get(boilerID);
		if (boiler == null) {
			log.warn("Could not find boiler {}", boilerID);
			return;
		}
		Json.putRef(obj, "productGroup", boiler.group);
		upgradeFuelSpec(boiler, obj.getAsJsonObject("fuelSpec"));
	}

	private void upgradeFuelSpec(Boiler boiler, JsonObject obj) {
		if (boiler == null || obj == null)
			return;
		String woodFuelID = Json.getRefID(obj, "woodFuel");
		Fuel fuel = null;
		if (woodFuelID == null) {
			fuel = getDefaultFuel(boiler);
		} else {
			fuel = new FuelDao(db).get(woodFuelID);
		}
		if (fuel == null) {
			log.warn("Could not load fuel for {}", boiler);
			return;
		}
		Json.putRef(obj, "fuel", fuel);
		obj.remove("woodFuel");
		if (fuel.isWood()) {
			obj.addProperty("woodAmountType", "CHIPS");
		}
	}

	private Fuel getDefaultFuel(Boiler boiler) {
		if (boiler == null || boiler.group == null)
			return null;
		FuelGroup group = boiler.group.fuelGroup;
		Fuel candidate = null;
		for (Fuel fuel : new FuelDao(db).getAll()) {
			if (fuel.group != group)
				continue;
			if (fuel.isProtected)
				return fuel;
			candidate = fuel;
		}
		return candidate;
	}

}
