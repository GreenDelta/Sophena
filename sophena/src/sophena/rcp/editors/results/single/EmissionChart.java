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

import sophena.math.energetic.CO2Emissions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class EmissionChart {

	private CO2Emissions result;

	private EmissionChart(CO2Emissions result) {
		this.result = result;
	}

	public static void create(CO2Emissions result, Composite comp) {
		new EmissionChart(result).render(comp);
	}

	private void render(Composite comp) {
		Chart chart = new Chart(comp, SWT.NONE);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 250;
		data.minimumWidth = 650;
		chart.setLayoutData(data);
		chart.getTitle().setVisible(false);
		ISeriesSet set = chart.getSeriesSet();
		addSeries(set, "Emissionen Wärmenetz", result.total, 0);
		addSeries(set, "Emissionen Erdgas", result.variantNaturalGas, 1);
		addSeries(set, "Emissionen Heizöl", result.variantOil, 2);
		chart.getAxisSet().adjustRange();
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
		y.getTitle().setFont(UI.defautlFont());
	}
}
