package sophena.rcp.editors.results.single;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.IBarSeries.BarWidthStyle;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.Range;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.CO2Result;
import sophena.model.Stats;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.UI;

class EmissionChart {

	private final CO2Result result;
	private final ColorConfig colors = ColorConfig.get();
	private Chart chart;

	private EmissionChart(CO2Result result) {
		this.result = result;
	}

	public static void create(CO2Result result, Composite body, FormToolkit tk) {
		new EmissionChart(result).render(body, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Vergleich Treibhausgasemissionen");
		Actions.bind(section, ImageExport.forChart(
				"Treibhausgasemissionen.jpg", () -> chart));
		var comp = UI.sectionClient(section, tk);
		chart = new Chart(comp, SWT.NONE);
		var data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 250;
		data.minimumWidth = 650;
		chart.setLayoutData(data);
		chart.getTitle().setVisible(false);
		chart.setBackground(Colors.getWhite());
		var set = chart.getSeriesSet();
		addSeries(set, ColorKey.EMISSIONS);
		addSeries(set, ColorKey.EMISSIONS_GAS);
		addSeries(set, ColorKey.EMISSIONS_OIL);
		formatX(chart);
		formatY(chart);
	}

	private void addSeries(ISeriesSet set, ColorKey key) {
		var label = switch (key) {
			case EMISSIONS -> "Emissionen Wärmenetz";
			case EMISSIONS_GAS -> "Emissionen Erdgas";
			case EMISSIONS_OIL -> "Emissionen Heizöl";
			default -> "?";
		};
		double value = switch (key) {
			case EMISSIONS -> result.total;
			case EMISSIONS_GAS -> result.variantNaturalGas;
			case EMISSIONS_OIL -> result.variantOil;
			default -> 0;
		};

		var s = (IBarSeries<?>) set.createSeries(SeriesType.BAR, label);
		s.setYSeries(new double[]{(double) Math.round(value)});
		s.setBarColor(Colors.of(colors.get(key)));
		s.setBarWidthStyle(BarWidthStyle.FIXED);
		s.setBarWidth(180);
	}

	private void formatX(Chart chart) {
		var x = chart.getAxisSet().getXAxis(0);
		if (x == null)
			return;
		x.getTick().setVisible(false);
		x.setCategorySeries(new String[]{""});
		x.enableCategory(true);
		x.getTitle().setVisible(false);
	}

	private void formatY(Chart chart) {
		var y = chart.getAxisSet().getYAxis(0);
		if (y == null)
			return;
		y.getTick().setForeground(Colors.getBlack());
		y.getTitle().setForeground(Colors.getBlack());
		y.getTitle().setText("kg CO2 eq.");
		y.getTitle().setFont(UI.defaultFont());
		y.setRange(getYRange());
	}

	private Range getYRange() {
		double[] vals = {result.total, result.variantNaturalGas, result.variantOil};
		double min = Math.min(Math.min(vals[0], vals[1]), vals[2]);
		if (min > 0) {
			min = 0;
		}
		double max = Math.max(Math.max(vals[0], vals[1]), vals[2]);
		return new Range(min, Stats.nextStep(max, 50));
	}
}
