package sophena.io.thermos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import sophena.db.Database;
import sophena.model.Consumer;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.TransferStation;

public class ThermosImport implements Runnable {

	private final Database db;
	private final ThermosImportConfig config;
	private final ThermosFile file;
	private final Project project;
	private String error;
	private List<TransferStation> stations;

	public ThermosImport(Database db, ThermosImportConfig config) {
		this.db = Objects.requireNonNull(db);
		this.config = Objects.requireNonNull(config);
		this.file = Objects.requireNonNull(config.thermosFile());
		this.project = Objects.requireNonNull(config.project());
	}

	public boolean hasError() {
		return error != null;
	}

	public String error() {
		return error;
	}

	@Override
	public void run() {
		try {
			if (config.isWithStations()) {
				stations = new ArrayList<>();
				var manufacturer = config.stationManufacturer();
				var productLine = config.stationProductLine();
				for (var s : db.getAll(TransferStation.class)) {
					if (
						Objects.equals(s.manufacturer, manufacturer) &&
						Objects.equals(s.productLine, productLine)
					) {
						stations.add(s);
					}
				}
				stations.sort(Comparator.comparingDouble(s -> s.outputCapacity));
			}

			if (config.isWithConsumers()) {
				syncConsumers();
			}
			if (config.isWithPipes()) {
				new PipeSync(db, config).run();
			}

			db.update(project);
		} catch (Exception e) {
			error = "Unerwarteter Fehler im Import: " + e.getMessage();
		}
	}

	private void syncConsumers() {
		if (config.isUpdateExisting()) {
			syncConsumersInUpdateMode(file.consumers());
		} else {
			syncConsumersInAppendMode(file.consumers());
		}
	}

	private void syncConsumersInUpdateMode(List<Consumer> consumers) {
		var existingMap = new HashMap<String, Consumer>();
		for (var consumer : project.consumers) {
			existingMap.put(consumer.id, consumer);
		}

		var ids = new HashSet<String>();
		for (var c : consumers) {
			ids.add(c.id);
			var existing = existingMap.get(c.id);
			if (existing != null) {
				updateConsumer(existing, c);
			} else {
				addNewConsumer(c);
			}
		}

		// remove consumers that are not in file
		project.consumers.removeIf(consumer -> !ids.contains(consumer.id));
	}

	private void syncConsumersInAppendMode(List<Consumer> consumers) {
		var ids = new HashSet<String>();
		for (var old : project.consumers) {
			ids.add(old.id);
		}
		for (var c : consumers) {
			if (ids.contains(c.id)) continue;
			addNewConsumer(c);
		}
	}

	private void addNewConsumer(Consumer c) {
		if (config.isWithStations()) {
			assignStation(c, stations);
		}
		project.consumers.add(c);
	}

	private void updateConsumer(Consumer c, Consumer update) {
		boolean loadChanged = c.heatingLoad != update.heatingLoad;

		c.name = update.name;
		c.description = update.description;
		c.buildingState = update.buildingState;
		c.demandBased = update.demandBased;
		c.heatingLoad = update.heatingLoad;
		c.heatingLimit = update.heatingLimit;
		c.waterFraction = update.waterFraction;
		c.loadHours = update.loadHours;
		c.floorSpace = update.floorSpace;

		if (config.isWithStations() && (c.transferStation == null || loadChanged)) {
			assignStation(c, stations);
		}

		if (update.location != null) {
			if (c.location == null) {
				c.location = update.location.copy();
			} else {
				c.location.name = update.location.name;
				c.location.street = update.location.street;
				c.location.zipCode = update.location.zipCode;
				c.location.city = update.location.city;
				c.location.latitude = update.location.latitude;
				c.location.longitude = update.location.longitude;
			}
		}

		c.fuelConsumptions.clear();
		for (var fc : update.fuelConsumptions) {
			c.fuelConsumptions.add(fc.copy());
		}
	}

	private void assignStation(Consumer c, List<TransferStation> stations) {
		if (stations == null || stations.isEmpty()) return;
		TransferStation station = null;
		for (var s : stations) {
			if (s.outputCapacity >= c.heatingLoad) {
				station = s;
				break;
			}
		}
		if (station != null) {
			c.transferStation = station;
			if (c.transferStationCosts == null) {
				c.transferStationCosts = new ProductCosts();
			}
			ProductCosts.copy(station, c.transferStationCosts);
		}
	}
}
