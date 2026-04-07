package sophena.rcp.editors.heatnets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.openlca.commons.Res;

import sophena.calc.ProjectResult;
import sophena.db.Database;
import sophena.model.BufferTank;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.app.App;
import sophena.rcp.utils.MsgBox;

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
				"Es sind keine Pufferspeicher mit Kostenangaben in der "
					+ "Produktdatenbank enthalten.");
			return Optional.empty();
		}

		var estimator = new BufferEstimator(project.copy(), buffers);
		var costs = App.exec("Berechne Varianten", estimator::estimate);
		if (costs.isError()) {
			MsgBox.error("Fehler in der Projektberechnung", costs.error());
			return Optional.empty();
		}

		return BufferEstimationDialog.open(costs.value());
	}

	private Res<List<BufferCosts>> estimate() {
		if (project.heatNet == null)
			return Res.error("Das Projekt enthält kein Wärmenetz.");

		var results = new ArrayList<BufferCosts>();
		for (var buffer : buffers) {
			try {
				var variant = project.copy();
				prepare(variant, buffer);
				var result = ProjectResult.calculate(variant);
				double costs = result.costResultFunding.dynamicTotal.heatGenerationCosts;
				double uncoveredHeat = uncoveredHeat(result);
				results.add(new BufferCosts(buffer, costs, uncoveredHeat));
			} catch (Exception e) {
				var name = buffer != null && buffer.name != null
					? buffer.name
					: "(ohne Namen)";
				return Res.error("Die Berechnung für den Pufferspeicher \""
					+ name + "\" ist fehlgeschlagen.", e);
			}
		}

		results.sort(Comparator
			.comparingDouble(BufferCosts::costs)
			.thenComparingDouble(BufferCosts::uncoveredHeat));
		return Res.ok(results);
	}

	private void prepare(Project variant, BufferTank buffer) {
		var net = variant.heatNet;
		net.bufferTank = buffer;
		if (net.bufferTankCosts == null) {
			net.bufferTankCosts = new ProductCosts();
		}
		ProductCosts.copy(buffer, net.bufferTankCosts);
		if (buffer.purchasePrice != null) {
			net.bufferTankCosts.investment = buffer.purchasePrice;
		}
	}

	private double uncoveredHeat(ProjectResult result) {
		if (result == null || result.energyResult == null)
			return 0;
		var energy = result.energyResult;
		double uncovered = 0;
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			double load = Stats.get(energy.loadCurve, hour);
			double supplied = Stats.get(energy.suppliedPower, hour);
			uncovered += Math.max(0, load - supplied);
		}
		return uncovered;
	}

}
