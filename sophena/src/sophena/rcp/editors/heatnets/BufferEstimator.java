package sophena.rcp.editors.heatnets;

import org.openlca.commons.Res;
import sophena.db.Database;
import sophena.model.BufferTank;
import sophena.model.Project;
import sophena.rcp.utils.MsgBox;

import java.util.List;
import java.util.Optional;

class BufferEstimator {

	private final Project project;
	private final List<BufferTank> buffers;

	private BufferEstimator(Project project, List<BufferTank> buffers) {
		this.project = project;
		this.buffers = buffers;
	}

	static Optional<BufferTank> run(Project project, Database db) {
		if (project == null || db == null)
			return Optional.empty();

		// it only works when there are consumers and producers
		// in the project.
		if (project.consumers.isEmpty()) {
			MsgBox.info("Keine Abnehmer definiert",
				"In dem Projekt sind noch keine Abnehmer definiert.");
			return Optional.empty();
		}
		if (project.producers.isEmpty()) {
			MsgBox.info("Keine Wärmeerzeuger definiert",
				"In dem Projekt sind noch keine Wärmeerzeuger definiert.");
			return Optional.empty();
		}

		// select the buffers that have costs assigned
		var buffers = db.getAll(BufferTank.class)
			.stream()
			.filter(b -> b.purchasePrice != null && b.purchasePrice > 0)
			.toList();
		if (buffers.isEmpty()) {
			MsgBox.info("Keine Pufferspeicherkosten gefunden",
				"Die Produktdatenbank enthält keine Pufferspeicher mit "
					+ "Kostenangaben.");
			return Optional.empty();
		}

		var estimator = new BufferEstimator(project.copy(), buffers);
		// TODO run in non-ui thread
		var costs = estimator.estimate();
		if (costs.isError()) {
			MsgBox.error("Fehler in der Projektberechnung", costs.error());
			return Optional.empty();
		}

		return BufferEstimationDialog.open(costs.value());
	}

	private Res<List<BufferCosts>> estimate() {
		return Res.error("Not yet implemented");
	}

}
