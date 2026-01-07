package sophena.io.thermos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import sophena.db.Database;
import sophena.model.Consumer;
import sophena.model.Project;

public class ThermosImport implements Runnable {

	private final Database db;
	private final ThermosImportConfig config;
	private final ThermosFile file;
	private final Project project;
	private String error;

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
			if (config.isWithConsumers()) {
				syncConsumers();
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
				project.consumers.add(c);
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
			if (!ids.contains(c.id)) {
				project.consumers.add(c);
			}
		}
	}

	private void updateConsumer(Consumer c, Consumer update) {
		c.name = update.name;
		c.description = update.description;
		c.buildingState = update.buildingState;
		c.demandBased = update.demandBased;
		c.heatingLoad = update.heatingLoad;
		c.heatingLimit = update.heatingLimit;
		c.waterFraction = update.waterFraction;
		c.loadHours = update.loadHours;
		c.floorSpace = update.floorSpace;

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
}
