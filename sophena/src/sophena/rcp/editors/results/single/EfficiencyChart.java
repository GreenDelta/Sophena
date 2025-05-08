package sophena.rcp.editors.results.single;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.IBarSeries.BarWidthStyle;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.Range;

import sophena.math.energetic.EfficiencyResult;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.utils.Actions;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

class EfficiencyChart  {

	private final EfficiencyResult result;
	private final ColorConfig colors = ColorConfig.get();
	private Chart chart;

	private EfficiencyChart(EfficiencyResult result) {
		this.result = result;
	}

	public static void create(EfficiencyResult result, Composite comp,
			FormToolkit tk) {
		new EfficiencyChart(result).render(comp, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Erzeugte Wärme");
		var comp = UI.sectionClient(section, tk);
		chart = new Chart(comp, SWT.NONE);
		var data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 250;
		data.minimumWidth = 650;
		chart.setLayoutData(data);
		chart.setBackground(Colors.getWhite());
		chart.setOrientation(SWT.VERTICAL);
		chart.getTitle().setVisible(false);
		var set = chart.getSeriesSet();
		Actions.bind(section, ImageExport.forChart(
				"Brennstoffenergie.jpg", () -> chart));

		addSeries(set, ColorKey.USED_HEAT);
		addSeries(set, ColorKey.LOSSES_BUFFER);
		addSeries(set, ColorKey.LOSSES_DISTRIBUTION);

		formatX(chart);
		formatY(chart);
	}

	private void addSeries(ISeriesSet set, ColorKey key) {
		var s = (IBarSeries<?>) set.createSeries(SeriesType.BAR, labelOf(key));
		s.setYSeries(valueOf(key));
		s.setBarColor(Colors.of(colors.get(key)));
		s.setBarWidthStyle(BarWidthStyle.FIXED);
		s.setBarWidth(80);
		s.enableStack(true);
	}

	private String labelOf(ColorKey key) {
		return switch (key) {
			case USED_HEAT -> "Genutzte Wärme";
			case LOSSES_BUFFER -> "Pufferspeicherverluste";
			case LOSSES_DISTRIBUTION -> "Verteilungsverluste";
			default -> "?";
		};
	}

	private double[] valueOf(ColorKey key) {
		double val = switch (key) {
			case USED_HEAT -> result.usedHeat;
			case LOSSES_BUFFER -> result.bufferLoss;
			case LOSSES_DISTRIBUTION -> result.distributionLoss;
			default -> 0;
		};
		double rounded = (double) Math.abs(Math.round(val));
		return new double[]{rounded};
	}

	private void formatX(Chart chart) {
		IAxis x = chart.getAxisSet().getXAxis(0);
		if (x == null)
			return;
		x.getTick().setVisible(false);
		x.setCategorySeries(new String[]{""});
		x.enableCategory(true);
		x.getTitle().setVisible(false);
	}

	private void formatY(Chart chart) {
		IAxis y = chart.getAxisSet().getYAxis(0);
		if (y == null)
			return;
		y.getTick().setForeground(Colors.getBlack());
		y.getTitle().setForeground(Colors.getBlack());
		y.getTitle().setText("kWh");
		y.getTitle().setFont(UI.defaultFont());
		double max = result.usedHeat
				+ result.producedElectrictiy
				+ result.bufferLoss
				+ result.distributionLoss;
		if (max == 0) {
			max = 1;
		}
		y.setRange(new Range(0, max));
	}
}
