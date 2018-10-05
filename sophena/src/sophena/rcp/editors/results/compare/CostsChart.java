package sophena.rcp.editors.results.compare;

import java.util.Arrays;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.swtchart.Chart;

import sophena.calc.Comparison;
import sophena.calc.CostResult.FieldSet;
import sophena.rcp.utils.Colors;

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
		Chart chart = BarCharts.init(body, tk, "Kosten");
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
					FieldSet costs = r.costResultFunding.dynamicTotal;
					switch (type) {
					case CAPITAL_COSTS:
						return costs.capitalCosts;
					case CONSUMPTION_COSTS:
						return costs.consumptionCosts;
					case OPERATIONS_COSTS:
						return costs.operationCosts;
					case OTHER_COSTS:
						return costs.otherAnnualCosts;
					default:
						return 0d;
					}
				}).toArray();
		BarCharts.series(chart, label(type), color(type), data);
	}

	private String label(int type) {
		switch (type) {
		case CAPITAL_COSTS:
			return "Kapitalgebundene Kosten";
		case CONSUMPTION_COSTS:
			return "Bedarfsgebundene Kosten";
		case OPERATIONS_COSTS:
			return "Betriebsgebundene Kosten";
		case OTHER_COSTS:
			return "Sonstige Kosten";
		default:
			return "?";
		}
	}

	private Color color(int type) {
		switch (type) {
		case CAPITAL_COSTS:
			return Colors.get("#90A4AE");
		case CONSUMPTION_COSTS:
			return Colors.get("#607D8B");
		case OPERATIONS_COSTS:
			return Colors.get("#455A64");
		case OTHER_COSTS:
			return Colors.get("#263238");
		default:
			return Colors.getErrorColor();
		}
	}

}