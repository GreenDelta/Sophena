package sophena.io.thermos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.commons.Res;

import sophena.db.Database;
import sophena.io.thermos.file.ThermosFile;
import sophena.io.thermos.pipes.PipeSum;
import sophena.io.thermos.pipes.PipeSync;
import sophena.io.thermos.pipes.PipeSyncResult;
import sophena.calc.costs.FittingsCostSync;
import sophena.calc.costs.FittingsCostSync.Mode;
import sophena.model.Consumer;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.TransferStation;

/// Imports the data of a BioHeating-Tool file into a project.
///
/// The import runs in two phases: in the planning phase everything that can
/// fail is calculated without modifying the project. Only in the commit phase,
/// when the import is known to be executable, the project is modified and
/// finally updated in the database.
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
			var res = plan();
			if (res.isError()) {
				error = res.error();
				return;
			}
			if (!commit(res.value()))
				return;
			db.update(project);
		} catch (Exception e) {
			error = "Unerwarteter Fehler im Import: " + e.getMessage();
		}
	}

	/// An update of an existing consumer with the data of the import file.
	private record ConsumerUpdate(Consumer existing, Consumer update) {}

	/// Everything that is needed to apply the import to the project. It is
	/// calculated in the planning phase, before the project is modified.
	private record Plan(
		List<TransferStation> stations,
		PipeSync pipeSync,
		PipeSum pipeSum,
		List<Consumer> newConsumers,
		List<ConsumerUpdate> updates,
		Set<String> fileConsumerIds
	) {}

	/// Calculates the import without modifying the project.
	private Res<Plan> plan() {
		var stations = new ArrayList<TransferStation>();
		if (config.isWithStations()) {
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

		PipeSync pipeSync = null;
		PipeSum pipeSum = null;
		if (config.isWithPipes() && !config.isSkipPipes()) {
			pipeSync = new PipeSync(db, config);
			var res = pipeSync.plan();
			if (res.isError())
				return Res.error(res.error());
			pipeSum = res.value();
		}

		var newConsumers = new ArrayList<Consumer>();
		var updates = new ArrayList<ConsumerUpdate>();
		var fileIds = new HashSet<String>();
		if (config.isWithConsumers()) {
			var existing = new HashMap<String, Consumer>();
			for (var c : project.consumers) {
				existing.put(c.id, c);
			}
			for (var c : file.consumers()) {
				fileIds.add(c.id);
				var old = existing.get(c.id);
				if (old == null) {
					newConsumers.add(c);
				} else if (config.isUpdateExisting()) {
					updates.add(new ConsumerUpdate(old, c));
				}
			}
		}

		return Res.ok(new Plan(
			stations,
			pipeSync,
			pipeSum,
			newConsumers,
			updates,
			fileIds
		));
	}

	/// Applies the given plan to the project. Returns `false` if the import
	/// could not be applied; in this case the error is set.
	private boolean commit(Plan plan) {
		if (config.isWithConsumers()) {
			for (var c : plan.newConsumers()) {
				if (config.isWithStations()) {
					assignStation(c, plan.stations());
				}
				project.consumers.add(c);
			}
			for (var update : plan.updates()) {
				updateConsumer(
					update.existing(), update.update(), plan.stations());
			}
			if (config.isUpdateExisting()) {
				project.consumers.removeIf(
					c -> !plan.fileConsumerIds().contains(c.id));
			}
		}

		if (plan.pipeSum() != null) {
			var res = plan.pipeSync().apply(plan.pipeSum());
			if (res.isError()) {
				error = res.error();
				return false;
			}
			syncFittingsCosts(res.value());
		}

		return true;
	}

	private void updateConsumer(
		Consumer c, Consumer update, List<TransferStation> stations
	) {
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

	private void syncFittingsCosts(PipeSyncResult r) {
		if (project.heatNet == null || project.costSettings == null) {
			return;
		}
		var sync = FittingsCostSync.of(project, db)
			.withCount(r.fittingsCount());
		if (config.isUpdateExisting()) {
			sync.withUpdate(Mode.REPLACE);
		} else {
			sync.withUpdate(Mode.APPEND)
				.withPipes(r.pipeDiffs());
		}
		sync.run();
	}
}
