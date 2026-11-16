package sophena.rcp.editors.heatnets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.openlca.commons.Res;

import sophena.calc.ProjectResult;
import sophena.calc.load.ProjectLoad;
import sophena.db.Database;
import sophena.model.BufferTank;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.rcp.app.App;
import sophena.rcp.utils.MsgBox;

class BufferEstimator {

	private final Project project;
	private final List<BufferTank> buffers;

	private BufferEstimator(Project project, List<BufferTank> buffers) {
		this.project = project;
		this.buffers = buffers;
	}

	/// Calculates the project with each buffer tank from the database that has
	/// a purchase price and lets the user select one. The given project should
	/// be the current project, see `HeatNetEditor.currentProject`.
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
		if (costs == null) // an error message was already shown
			return Optional.empty();
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
			var variant = project.copy();
			prepare(variant, buffer);
			var res = ProjectResult.calculate(variant);
			if (res.isError()) {
				var name = buffer.name != null ? buffer.name : "(ohne Namen)";
				return res.wrapError("Die Berechnung für den Pufferspeicher \""
					+ name + "\" ist fehlgeschlagen.");
			}
			var result = res.value();
			double costs = result.costResultFunding.dynamicTotal.heatGenerationCosts;
			results.add(new BufferCosts(
				buffer, costs, result.energyResult.totalUncoveredLoad()));
		}

		// variants that cover the load come first; the uncovered heat is
		// rounded to whole kWh, so that rounding errors do not matter; then
		// the variants are sorted by their heat generation costs, which can be
		// lower when a part of the load is not covered
		results.sort(Comparator
			.comparingLong((BufferCosts bc) -> Math.round(bc.uncoveredHeat()))
			.thenComparingDouble(BufferCosts::costs));
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
		// the same maximum unload power as when the buffer tank is selected
		// in the editor, see `BufferTankSection.applyBufferTank`
		net.maximumPerformance = ProjectLoad.of(variant).simultaneousMax();
	}
}
