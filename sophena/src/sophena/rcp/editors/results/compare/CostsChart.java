package sophena.rcp.editors.results.compare;

import java.util.Arrays;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.Chart;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.rcp.colors.Colors;

class CostsChart {

	private final Comparison comparison;

	private final int CAPITAL_COSTS = 1;
	private final int CONSUMPTION_COSTS = 2;
	private final int OPERATIONS_COSTS = 4;
	private final int OTHER_COSTS = 8;

	public CostsChart(Comparison comparison) {
		this.comparison = comparison;
	}

	void render(Composite body, FormToolkit tk) {
		var chart = BarCharts.init(body, tk, "Kosten");
		BarCharts.createAxes(chart, comparison, "EUR/a");
		series(chart, OTHER_COSTS);
		series(chart, OPERATIONS_COSTS);
		series(chart, CONSUMPTION_COSTS);
		series(chart, CAPITAL_COSTS);
		chart.getAxisSet().adjustRange();
	}

	private void series(Chart chart, int type) {
		double[] data = Arrays.stream(comparison.results)
			.mapToDouble(r -> {
				var costs = r.costResultFunding.dynamicTotal;
				return switch (type) {
					case CAPITAL_COSTS -> costs.capitalCosts;
					case CONSUMPTION_COSTS -> costs.consumptionCosts;
					case OPERATIONS_COSTS -> costs.operationCosts;
					case OTHER_COSTS -> costs.otherAnnualCosts;
					default -> 0d;
				};
			}).toArray();
		BarCharts.stackSeries(chart, label(type), color(type), data);
	}

	private String label(int type) {
		return switch (type) {
			case CAPITAL_COSTS -> "Kapitalgebundene Kosten";
			case CONSUMPTION_COSTS -> "Bedarfsgebundene Kosten";
			case OPERATIONS_COSTS -> "Betriebsgebundene Kosten";
			case OTHER_COSTS -> "Sonstige Kosten";
			default -> "?";
		};
	}

	private Color color(int type) {
		return switch (type) {
			case CAPITAL_COSTS -> Colors.of("#81c784");
			case CONSUMPTION_COSTS -> Colors.of("#4caf50");
			case OPERATIONS_COSTS -> Colors.of("#388e3c");
			case OTHER_COSTS -> Colors.of("#1b5e20");
			default -> Colors.getErrorColor();
		};
	}

}
