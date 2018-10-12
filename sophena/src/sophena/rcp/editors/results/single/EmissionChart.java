package sophena.rcp.editors.results.single;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.IBarSeries.BarWidthStyle;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

import sophena.calc.CO2Result;
import sophena.model.Stats;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class EmissionChart {

	private CO2Result result;
	private Chart chart;

	private EmissionChart(CO2Result result) {
		this.result = result;
	}

	public static void create(CO2Result result, Composite body,
			FormToolkit tk) {
		new EmissionChart(result).render(body, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk,
				"Vergleich Treibhausgasemissionen");
		Actions.bind(section, ImageExport.forChart(
				"Treibhausgasemissionen.jpg", () -> chart));
		Composite comp = UI.sectionClient(section, tk);
		chart = new Chart(comp, SWT.NONE);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 250;
		data.minimumWidth = 650;
		chart.setLayoutData(data);
		chart.getTitle().setVisible(false);
		ISeriesSet set = chart.getSeriesSet();
		addSeries(set, "Emissionen Wärmenetz", result.total, 0);
		addSeries(set, "Emissionen Erdgas", result.variantNaturalGas, 1);
		addSeries(set, "Emissionen Heizöl", result.variantOil, 2);
		formatX(chart);
		formatY(chart);
	}

	private void addSeries(ISeriesSet set, String label, double value, int colorId) {
		IBarSeries s = (IBarSeries) set.createSeries(SeriesType.BAR, label);
		s.setYSeries(new double[] { (double) Math.round(value) });
		s.setBarColor(Colors.getForChart(colorId));
		s.setBarWidthStyle(BarWidthStyle.FIXED);
		s.setBarWidth(180);
	}

	private void formatX(Chart chart) {
		IAxis x = chart.getAxisSet().getXAxis(0);
		if (x == null)
			return;
		x.getTick().setVisible(false);
		x.setCategorySeries(new String[] { "" });
		x.enableCategory(true);
		x.getTitle().setVisible(false);
	}

	private void formatY(Chart chart) {
		IAxis y = chart.getAxisSet().getYAxis(0);
		if (y == null)
			return;
		y.getTick().setForeground(Colors.getBlack());
		y.getTitle().setForeground(Colors.getBlack());
		y.getTitle().setText("kg CO2 eq.");
		y.getTitle().setFont(UI.defaultFont());
		y.setRange(getYRange());
	}

	private Range getYRange() {
		double[] vals = { result.total, result.variantNaturalGas, result.variantOil };
		double min = Math.min(Math.min(vals[0], vals[1]), vals[2]);
		if (min > 0) {
			min = 0;
		}
		double max = Math.max(Math.max(vals[0], vals[1]), vals[2]);
		return new Range(min, Stats.nextStep(max, 50));
	}
}
