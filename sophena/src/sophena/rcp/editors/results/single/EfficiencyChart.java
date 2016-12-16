package sophena.rcp.editors.results.single;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.IBarSeries.BarWidthStyle;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

import sophena.math.energetic.EfficiencyResult;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class EfficiencyChart {

	private EfficiencyResult result;

	private EfficiencyChart(EfficiencyResult result) {
		this.result = result;
	}

	public static void create(EfficiencyResult result, Composite comp) {
		new EfficiencyChart(result).render(comp);
	}

	private void render(Composite comp) {
		Chart chart = new Chart(comp, SWT.NONE);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 250;
		data.minimumWidth = 650;
		chart.setLayoutData(data);
		chart.setOrientation(SWT.VERTICAL);
		chart.getTitle().setVisible(false);
		ISeriesSet set = chart.getSeriesSet();
		addSeries(set, "Genutzte Wärme", result.usedHeat, 0);
		if (result.producedElectrictiy > 0)
			addSeries(set, "Erzeugter Strom", result.producedElectrictiy, 1);
		addSeries(set, "Konversionsverluste", result.conversionLoss, 2);
		addSeries(set, "Verteilungsverluste", result.distributionLoss, 3);
		formatX(chart);
		formatY(chart);
	}

	private void addSeries(ISeriesSet set, String label, double value, int colorId) {
		IBarSeries s = (IBarSeries) set.createSeries(SeriesType.BAR, label);
		s.setYSeries(new double[] { (double) Math.abs(Math.round(value)) }); // TODO
		s.setBarColor(Colors.getForChart(colorId));
		s.setBarWidthStyle(BarWidthStyle.FIXED);
		s.setBarWidth(80);
		s.enableStack(true);
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
		y.getTitle().setText("kWh");
		y.getTitle().setFont(UI.defautlFont());
		double max = result.usedHeat
				+ result.producedElectrictiy
				+ result.conversionLoss
				+ result.distributionLoss;
		if (max == 0) {
			max = 1;
		}
		y.setRange(new Range(0, max));
	}

}
