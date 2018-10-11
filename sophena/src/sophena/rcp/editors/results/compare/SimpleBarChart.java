package sophena.rcp.editors.results.compare;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.swtchart.Chart;

import sophena.calc.Comparison;
import sophena.calc.ProjectResult;
import sophena.rcp.utils.Colors;

class SimpleBarChart {

	private final String title;
	private final Comparison comp;

	private String unit = "";
	private double[] data;

	private SimpleBarChart(String title, Comparison comp) {
		this.title = title;
		this.comp = comp;
	}

	static SimpleBarChart of(String title, Comparison comp) {
		return new SimpleBarChart(title, comp);
	}

	SimpleBarChart unit(String unit) {
		this.unit = unit;
		return this;
	}

	SimpleBarChart data(ToDoubleFunction<ProjectResult> fn) {
		data = Arrays.stream(comp.results)
				.mapToDouble(fn)
				.toArray();
		return this;
	}

	void render(Composite body, FormToolkit tk) {
		Chart chart = BarCharts.init(body, tk, title);
		BarCharts.createAxes(chart, comp, unit);
		BarCharts.series(chart, title, Colors.get("#81c784"), data);
		chart.getAxisSet().adjustRange();
	}
}
