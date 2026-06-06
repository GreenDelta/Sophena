package sophena.io.datapack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import sophena.db.Database;
import sophena.model.AbstractEntity;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.CostSettings;
import sophena.model.FlueGasCleaning;
import sophena.model.Fuel;
import sophena.model.HeatPump;
import sophena.model.HeatRecovery;
import sophena.model.Manufacturer;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.model.Project;
import sophena.model.ProjectFolder;
import sophena.model.SolarCollector;
import sophena.model.TransferStation;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.model.biogas.Substrate;

public class Import implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Database db;
	private final File packFile;
	private DataPack pack;

	private final List<Upgrade> upgrades = new ArrayList<>();

	public Import(File packFile, Database db) {
		this.packFile = packFile;
		this.db = db;
	}

	@Override
	public void run() {
		try (var pack = DataPack.open(packFile)) {
			this.pack = pack;
			var info = pack.readInfo();
			if (info.version < 2) {
				upgrades.add(new Upgrade2(db));
			}

			// order is important for resolving dataset references
			importAll(ModelType.PRODUCT_GROUP, ProductGroup.class);
			importAll(ModelType.MANUFACTURER, Manufacturer.class);
			importAll(ModelType.PIPE, Pipe.class);
			importAll(ModelType.PRODUCT, Product.class);
			importAll(ModelType.FUEL, Fuel.class);
			importAll(ModelType.BUFFER, BufferTank.class);
			importAll(ModelType.BOILER, Boiler.class);
			importAll(ModelType.HEAT_PUMP, HeatPump.class);
			importAll(ModelType.SOLAR_COLLECTOR, SolarCollector.class);
			importAll(ModelType.BUILDING_STATE, BuildingState.class);
			importAll(ModelType.COST_SETTINGS, CostSettings.class);
			importAll(ModelType.WEATHER_STATION, WeatherStation.class);
			importAll(ModelType.TRANSFER_STATION, TransferStation.class);
			importAll(ModelType.FLUE_GAS_CLEANING, FlueGasCleaning.class);
			importAll(ModelType.HEAT_RECOVERY, HeatRecovery.class);
			importAll(ModelType.ELECTRICITY_PRICE_CURVE, ElectricityPriceCurve.class);
			importAll(ModelType.BIOGAS_SUBSTRATE, Substrate.class);
			importAll(ModelType.BIOGAS_PLANT, BiogasPlant.class);
			importAll(ModelType.PROJECT_FOLDER, ProjectFolder.class);
			importAll(ModelType.PROJECT, Project.class);

		} catch (Exception e) {
			log.error("failed to import data pack {}", pack, e);
		}
	}

	private <T extends AbstractEntity> void importAll(
		ModelType type, Class<T> clazz
	) {
		var gson = ImportGson.create(db, clazz);
		for (String id : pack.getIds(type)) {
			if (db.contains(clazz, id)) {
				log.info("{} with id={} is already exists: not imported", clazz, id);
				continue;
			}
			var json = pack.get(type, id);
			checkUpgrades(type, json);
			T instance = gson.fromJson(json, clazz);
			db.insert(instance);
		}
	}

	private void checkUpgrades(ModelType type, JsonObject obj) {
		if (obj == null || upgrades.isEmpty())
			return;
		for (Upgrade upgrade : upgrades) {
			upgrade.on(type, obj);
		}
	}
}
