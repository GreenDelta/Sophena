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
		return Res.error("Not yet implemented");
	}

	private ProductEntry surchargeEntry() {
		var keyBytes = (
			project.id +
			"/27a3d81b-0056-45e4-a13c-688296858c53"
		).getBytes(StandardCharsets.UTF_8);
		var id = UUID.nameUUIDFromBytes(keyBytes).toString();

		for (var e : project.productEntries) {
			if (Objects.equals(e.id, id)) {
				return e;
			}
		}

		var e = new ProductEntry();
		e.id = id;
		e.count = 1;
		e.costs = new ProductCosts();

		// TODO
		return e;
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
