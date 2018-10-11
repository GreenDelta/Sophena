package sophena.io.datapack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sophena.Labels;
import sophena.db.Database;
import sophena.db.daos.BoilerDao;
import sophena.db.daos.FuelDao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.LoadProfile;
import sophena.model.ModelType;
import sophena.model.Stats;

class Upgrade2 implements Upgrade {

	private final Database db;
	private Logger log = LoggerFactory.getLogger(getClass());

	Upgrade2(Database db) {
		this.db = db;
	}

	@Override
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
			obj.addProperty("ashContent", 1.0);
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
		JsonObject costSettings = obj.getAsJsonObject("costSettings");
		if (costSettings != null) {
			costSettings.addProperty("heatRevenuesFactor", 1.02);
			costSettings.addProperty("electricityRevenuesFactor", 1.00);
			JsonObject ue = new JsonObject();
			ue.addProperty("id", "97032c3b-aba3-4f2e-9f15-73370d394735");
			ue.addProperty("name", "Strom (Strommix)"); // just for info
			costSettings.add("usedElectricity", ue);
		}
		JsonArray consumers = obj.getAsJsonArray("consumers");
		for (JsonObject profile : pullConsumerLoadProfiles(consumers)) {
			consumers.add(profile);
		}
	}

	private void upgradeHeatNet(JsonObject obj) {
		if (obj == null)
			return;
		obj.addProperty("bufferLambda", 0.04);
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

	private List<JsonObject> pullConsumerLoadProfiles(JsonArray consumers) {
		if (consumers == null || consumers.size() == 0)
			return Collections.emptyList();
		List<JsonObject> profiles = new ArrayList<>();
		for (JsonElement e : consumers) {
			if (!e.isJsonObject())
				continue;
			JsonObject consumer = e.getAsJsonObject();
			JsonArray array = consumer.getAsJsonArray("loadProfiles");
			if (array == null || array.size() == 0)
				continue;
			consumer.remove("loadProfiles");
			for (JsonElement p : array) {
				if (!p.isJsonObject())
					continue;
				JsonObject profile = p.getAsJsonObject();
				upgradeConsumerLoadProfile(profile);
				profiles.add(profile);
			}
		}
		return profiles;
	}

	private void upgradeConsumerLoadProfile(JsonObject obj) {
		if (obj == null)
			return;
		LoadProfile profile = new Gson().fromJson(obj, LoadProfile.class);
		if (profile.dynamicData == null) {
			profile.dynamicData = new double[Stats.HOURS];
		}
		if (profile.staticData == null) {
			profile.staticData = new double[Stats.HOURS];
		}
		profile.id = UUID.randomUUID().toString();
		obj.add("profile", new Gson().toJsonTree(profile));
		obj.remove("dynamicData");
		obj.remove("staticData");
		double[] loadCurve = profile.calculateTotal();
		double maxLoad = Stats.max(loadCurve);
		obj.addProperty("heatingLoad", maxLoad);
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
		if (boiler.isCoGenPlant) {
			JsonObject pe = new JsonObject();
			pe.addProperty("id", "905c55bc-00ab-4fd1-8993-94e1ad83ba0f");
			pe.addProperty("name", "Strom (Verdrängungsstrommix)");
			obj.add("producedElectricity", pe);
		}
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
