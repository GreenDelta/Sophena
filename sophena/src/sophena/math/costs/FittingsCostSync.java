package sophena.math.costs;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.openlca.commons.Res;
import sophena.db.Database;
import sophena.model.CostSettings;
import sophena.model.HeatNetPipe;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;

/// Updates the cost entries for fittings of a project. It will not update
/// the project in the database, this has to be done after the sync.
public class FittingsCostSync {

	private final Project project;
	private final ProductGroup group;
	private final CostSettings settings;

	private final Mode mode;
	private final List<HeatNetPipe> pipes;
	private final double count;

	private FittingsCostSync(Config config, ProductGroup group) {
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

	private void run() {
		handleCount();
		handleSurcharge();
	}

	private void handleCount() {
		var entry = FittingsEntry.of(project, EntryType.COUNT);
		double p = settings.pricePerFitting;
		if (count <= 0 || p <= 0) {
			if (mode == Mode.REPLACE) {
				entry.remove();
			}
			return;
		}

		var e = entry.ensure(group);
		e.count = mode == Mode.REPLACE ? count : e.count + count;
		e.pricePerPiece = p;
		e.costs.investment += count * p; // TODO: not sure with this
		if (mode == Mode.REPLACE) {
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
		double invest = 0;
		for (var pipe : pipes) {
			if (pipe.costs == null) continue;
			double i = pipe.costs.investment;
			if (i <= 0) continue;
			invest += f * i;
		}

		e.count = 1;
		if (mode == Mode.APPEND) {
			e.pricePerPiece += invest;
			e.costs.investment = e.pricePerPiece;
		} else {
			e.pricePerPiece = invest;
			e.costs.investment = invest;
			avgCostParams(e);
		}
	}

	/// For other cost parameters than investment costs, calculate them
	/// as a weighted average based on the pipe lengths. This should be
	/// only called in replace mode.
	private void avgCostParams(ProductEntry e) {
		if (pipes.isEmpty()) return;

		double repair = 0;
		double maintenance = 0;
		double operation = 0;
		double duration = 0;
		double totalLen = 0;

		for (var pipe : pipes) {
			if (pipe.costs == null || pipe.length == 0) {
				continue;
			}
			var c = pipe.costs;
			double len = pipe.length;
			totalLen += len;
			repair += (c.repair * len);
			maintenance += (c.maintenance * len);
			operation += (c.operation * len);
			duration += (c.duration * len);
		}

		e.costs.repair = repair / totalLen;
		e.costs.maintenance = maintenance / totalLen;
		e.costs.operation = operation / totalLen;
		e.costs.duration = (int) Math.round(duration / totalLen);
	}

	private enum EntryType {
		COUNT,
		SURCHARGE,
	}

	private record FittingsEntry(
		ProductEntry value,
		EntryType type,
		Project project,
		boolean exists
	) {
		static FittingsEntry of(Project project, EntryType type) {
			var salt = "27a3d81b-0056-45e4-a13c-688296858c53";
			var keyBytes = (project.id + salt + type.toString()).getBytes(
				StandardCharsets.UTF_8
			);
			var id = UUID.nameUUIDFromBytes(keyBytes).toString();

			for (var e : project.productEntries) {
				if (Objects.equals(id, e.id)) {
					return new FittingsEntry(e, type, project, true);
				}
			}

			var e = new ProductEntry();
			e.id = id;
			return new FittingsEntry(e, type, project, false);
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
					(value.id + "/product").getBytes(StandardCharsets.UTF_8)
				).toString();
				p.projectId = project.id;
				p.name = type == EntryType.COUNT ? "Formteile" : "Formteile - Zuschlag";
				p.type = ProductType.HEATING_NET_CONSTRUCTION;
				p.group = group;
				value.product = p;
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
				if (group == null) {
					return Res.error(
						"Die Produktgruppe 'Formteile' wurde nicht gefunden"
					);
				}

				new FittingsCostSync(this, group).run();
				return Res.ok();
			} catch (Exception e) {
				return Res.error("An unexpected error occured", e);
			}
		}
	}
}
