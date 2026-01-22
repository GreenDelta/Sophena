package sophena.rcp.editors.costs;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.openlca.commons.Res;
import sophena.db.Database;
import sophena.model.CostSettings;
import sophena.model.HeatNetPipe;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;

/// Updates the cost entries for fittings of a project. It will not update
/// the project in the database, this has to be done after the sync.
class FittingsSync {

	private final Project project;
	private final ProductGroup group;
	private final CostSettings settings;

	private final Mode mode;
	private final List<HeatNetPipe> pipes;
	private final double count;

	private FittingsSync(Config config, ProductGroup group) {
		this.project = config.project;
		this.group = group;
		this.settings = config.project.costSettings;

		this.mode = config.mode != null ? config.mode : Mode.REPLACE;
		this.pipes = config.pipes != null ? config.pipes : project.heatNet.pipes;
		this.count = config.count != null ? config.count : 1.1 * this.pipes.size();
	}

	public static Config of(Project project, Database db) {
		return new Config(project, db);
	}

	private Res<Void> run() {
		if (settings.pricePerFitting <= 0 && settings.fittingSurchargeCost <= 0) {
			if (mode == Mode.REPLACE) {
				removeEntry();
			}
			return Res.ok();
		}
		return settings.pricePerFitting > 0
			? runPricePerFitting()
			: runSurcharge();
	}

	private void handleCount() {
		var entry = FittingsEntry.of(project, EntryType.SURCHARGE);
		double p = settings.pricePerFitting;
		if (count <= 0 || p <= 0) {
			if (mode == Mode.REPLACE) {
				entry.remove();
			}
			return;
		}

		var e = entry.ensure(group);
		e.count = mode == Mode.REPLACE ? count : e.count + count;
		entry.pricePerPiece = p;
		entry.costs.investment += count * p;  // TODO: not sure with this
		if (mode == Model.REPLACE) {
			avgCostParams(e);
		}
	}

	private void handleSurcharge() {
		var entry = FittingsEntry.of(project, EntryType.SURCHARGE);
		double f = settings.fittingSurchargeCost;
		if (pipes.isEmpty() || f <= 1e-7) {
			if (mode == Mode.REPLACE) {
				entry.remove();
			}
			return;
		}

		var e = entry.ensure(group);
		double n = pipes.size();
		double totalInvest = 0;
		double repairSum = 0;
		double maintenanceSum = 0;
		double operationSum = 0;
		double durationSum = 0;

		for (var pipe : pipes) {
			var c = pipe.costs;
			if (pipe.costs == null) {
				continue;
			}
			double invest = c.investment;
			if (invest <= 0)
				continue;
			totalInvest += f * invest;
			repairSum += c.repair;
			maintenanceSum += c.maintenance;
			operationSum += c.operation;
			durationSum += c.duration;
		}

		e.count = 1;
		if (mode == Mode.APPEND) {
			e.pricePerPiece += totalInvest;
			e.costs.investment = e.pricePerPiece;
		} else {
			e.pricePerPiece = totalInvest;
			e.costs.investment = totalInvest;
			e.costs.repair = repairSum / n;
			e.costs.maintenance = maintenanceSum / n;
			e.costs.operation = operationSum / n;
			e.costs.duration = (int) Math.round(durationSum / n);
		}
	}

	private enum EntryType {
		COUNT, SURCHARGE
	}

	private record FittingsEntry(
		ProductEntry value, EntryType type, Project project, boolean exists
	) {

		static FittingsEntry of(Project project, EntryType type) {
			var salt = "27a3d81b-0056-45e4-a13c-688296858c53";
			var keyBytes = (project.id + salt + type.toString()).getBytes(StandardCharsets.UTF_8);
			var id = UUID.nameUUIDFromBytes(keyBytes).toString();

			for (var e : project.productEntries) {
				if (Objects.equals(id, e.id)) {
					return new FittingsEntry(e, type, project, true);
				}
			}

			var e = new ProductEntry();
			entry.id = id;
		}

		ProductEntry ensure(ProductGroup group) {
			if (!exists) {
				project.productEntries.add(value);
			}
			if (value.costs == null) {
				value.costs = new ProductCosts();
				ProductCosts.copy(group, value.costs);
			}
			if (value.product == null) {
				var p = new Product();
				p.id = UUID.nameUUIDFromBytes(
					(value.id + "/product").getBytes(StandardCharsets.UTF_8)).toString();
				p.projectId = project.id;
				p.name = type == COUNT
				  ? "Formteile"
					: "Formteile - Zuschlag";
				p.type = ProductType.HEATING_NET_CONSTRUCTION;
				p.group = group;
				project.ownProducts.add(p);
			}
			return value;
		}

		void remove() {
			if (!exists) {
				return;
			}
			project.productEntries.remove(value);
			if (value.product != null) {
				project.ownProducts.remove(value.product);
			}
		}

	}

	public enum Mode {
		REPLACE,

		APPEND,
	}

	public static class Config {

		private final Database db;
		private final Project project;
		private Mode mode;
		private List<HeatNetPipe> pipes;
		private Double count;

		private Config(Project project, Database db) {
			this.project = project;
			this.db = db;
		}

		/// Set the pipes for which the sync is applied. If no pipes are set,
		/// it is applied on all pipes of the project.
		public Config withPipes(List<HeatNetPipe> pipes) {
			this.pipes = pipes;
			return this;
		}

		/// Set the number of fittings. If this is not set, a standard factor
		/// is applied on the number of pipes to estimate the number of fittings.
		public Config withCount(double count) {
			this.count = count;
			return this;
		}

		/// Set the update mode for the fittings cost entries: `replace` will
		/// replace the current cost entries, `append` will add values related
		/// to a set of new pipes to existing cost entries, creating them if
		/// they do not exist yet.
		public Config withUpdate(Mode mode) {
			this.mode = mode;
			return this;
		}

		public Res<Void> run() {
			if (db == null || project == null) {
				return Res.error("No valid database or project provided");
			}
			// nothing to do when there are no pipes or cost settings
			if (project.heatNet == null || project.costSettings == null) {
				return Res.ok();
			}

			try {
				// find the correct group
				var group = db
					.getAll(ProductGroup.class)
					.stream()
					.filter(
						g ->
							g.type == ProductType.HEATING_NET_CONSTRUCTION &&
							g.name != null &&
							g.name.equalsIgnoreCase("Formteile")
					)
					.findAny()
					.orElse(null);

				return group == null
					? Res.error("Die Produktgruppe 'Formteile' wurde nicht gefunden")
					: new FittingsSync(this, group).run();
			} catch (Exception e) {
				return Res.error("An unexpected error occured", e);
			}
		}
	}
}
