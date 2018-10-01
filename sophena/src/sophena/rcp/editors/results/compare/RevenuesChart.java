package sophena.rcp.editors.results.compare;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.IBarSeries.BarWidthStyle;
import org.swtchart.ISeries.SeriesType;

import sophena.calc.Comparison;
import sophena.calc.CostResult.FieldSet;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class RevenuesChart {

	private final Comparison comparison;

	RevenuesChart(Comparison comparison) {
		this.comparison = comparison;
	}

	void render(Composite body, FormToolkit tk) {
		Chart chart = BarChart.initChart(body, tk, "Erlöse");
		createAxes(chart);
		heatRevenues(chart);
		electricityRevenues(chart);
		chart.getAxisSet().adjustRange();
	}

	private void heatRevenues(Chart chart) {
		IBarSeries bars = (IBarSeries) chart.getSeriesSet()
				.createSeries(SeriesType.BAR, "Wärmeerlöse");
		double[] data = Arrays.stream(comparison.results)
				.mapToDouble(r -> {
					FieldSet costs = r.costResultFunding.dynamicTotal;
					return costs.revenuesHeat;
				}).toArray();
		bars.setYSeries(data);
		bars.setBarColor(Colors.get("#455A64"));
		formatBars(bars);
	}

	private void electricityRevenues(Chart chart) {
		IBarSeries bars = (IBarSeries) chart.getSeriesSet()
				.createSeries(SeriesType.BAR, "Stromerlöse");
		double[] data = Arrays.stream(comparison.results)
				.mapToDouble(r -> {
					FieldSet costs = r.costResultFunding.dynamicTotal;
					return costs.revenuesElectricity;
				}).toArray();
		bars.setYSeries(data);
		bars.setBarColor(Colors.get("#90A4AE"));
		formatBars(bars);
	}

	private void createAxes(Chart chart) {
		IAxis x = chart.getAxisSet().getXAxis(0);
		x.getTitle().setVisible(false);
		x.enableCategory(true);
		x.getTick().setForeground(Colors.getBlack());
		String[] categories = Arrays.stream(comparison.projects)
				.map(it -> it.name)
				.toArray(String[]::new);
		x.setCategorySeries(categories);

		IAxis y = chart.getAxisSet().getYAxis(0);
		y.getTitle().setFont(UI.defaultFont());
		y.getTitle().setText("EUR/a");
		y.getTitle().setForeground(Colors.getBlack());
		y.getTick().setForeground(Colors.getBlack());
	}

	private void formatBars(IBarSeries bars) {
		bars.enableStack(true);
		bars.setBarWidthStyle(BarWidthStyle.FIXED);
		bars.setBarWidth(65);
	}
}
