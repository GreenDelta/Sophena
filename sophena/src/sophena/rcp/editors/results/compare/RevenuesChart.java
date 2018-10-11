package sophena.rcp.editors.results.compare;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.swtchart.Chart;

import sophena.calc.Comparison;
import sophena.calc.CostResult.FieldSet;
import sophena.rcp.utils.Colors;

class RevenuesChart {

	private final Comparison comparison;

	RevenuesChart(Comparison comparison) {
		this.comparison = comparison;
	}

	void render(Composite body, FormToolkit tk) {
		Chart chart = BarCharts.init(body, tk, "Erlöse");
		BarCharts.createAxes(chart, comparison, "EUR/a");
		heatRevenues(chart);
		electricityRevenues(chart);
		chart.getAxisSet().adjustRange();
	}

	private void heatRevenues(Chart chart) {
		double[] data = Arrays.stream(comparison.results)
				.mapToDouble(r -> {
					FieldSet costs = r.costResultFunding.dynamicTotal;
					return costs.revenuesHeat;
				}).toArray();
		BarCharts.stackSeries(chart,
				"Wärmeerlöse",
				Colors.get("#455A64"),
				data);
	}

	private void electricityRevenues(Chart chart) {
		double[] data = Arrays.stream(comparison.results)
				.mapToDouble(r -> {
					FieldSet costs = r.costResultFunding.dynamicTotal;
					return costs.revenuesElectricity;
				}).toArray();
		BarCharts.stackSeries(chart,
				"Stromerlöse",
				Colors.get("#90A4AE"),
				data);
	}
}
