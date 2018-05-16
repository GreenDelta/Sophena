package sophena.rcp.editors.results.compare;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.IBarSeries.BarWidthStyle;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

import sophena.model.Stats;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BarChart {

	private final String title;
	private String unit = "";
	private Chart chart;

	private final List<String> labels = new ArrayList<>();
	private final List<Double> values = new ArrayList<>();
	private final List<Color> colors = new ArrayList<>();

	private BarChart(String title) {
		this.title = title;
	}

	static BarChart of(String title) {
		return new BarChart(title);
	}

	BarChart unit(String unit) {
		this.unit = unit;
		return this;
	}

	BarChart addBar(String label, double value) {
		Color color = Colors.getForChart(labels.size());
		return addBar(label, value, color);
	}

	BarChart addBar(String label, double value, Color color) {
		colors.add(color);
		labels.add(label);
		values.add(value);
		return this;
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, title);
		Actions.bind(section, ImageExport.forChart(
				title + ".jpg", () -> chart));
		Composite comp = UI.sectionClient(section, tk);
		chart = new Chart(comp, SWT.NONE);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 275;
		data.minimumWidth = 750;
		chart.setOrientation(SWT.HORIZONTAL);
		chart.setLayoutData(data);
		setTitle(chart);
		ISeriesSet set = chart.getSeriesSet();
		for (int i = 0; i < labels.size(); i++) {
			IBarSeries s = (IBarSeries) set.createSeries(
					SeriesType.BAR, labels.get(i));
			s.setYSeries(new double[] { values.get(i) });
			s.setBarColor(colors.get(i));
			s.setBarWidthStyle(BarWidthStyle.FIXED);
			s.setBarWidth(125);
		}
		formatX(chart);
		formatY(chart);
	}

	private void setTitle(Chart chart) {
		// we set a white title just to fix the problem that the y-axis is cut
		// sometimes
		chart.getTitle().setText(".");
		chart.getTitle().setFont(UI.defautlFont());
		chart.getTitle().setForeground(Colors.getWhite());
		chart.getTitle().setVisible(true);
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
		IAxisTick tick = y.getTick();
		tick.setForeground(Colors.getBlack());
		tick.setFormat(Num.getIntFormat());
		tick.setTickMarkStepHint(50);
		tick.setTickLabelAngle(45);
		tick.setVisible(true);
		y.getTitle().setForeground(Colors.getBlack());
		y.getTitle().setText(unit);
		y.getTitle().setFont(UI.defautlFont());
		y.setRange(getYRange());
	}

	private Range getYRange() {
		if (values.isEmpty())
			return new Range(0, 0);
		double min = values.get(0);
		double max = values.get(0);
		for (int i = 1; i < values.size(); i++) {
			min = Math.min(min, values.get(i));
			max = Math.max(max, values.get(i));
		}
		if (min > 0) {
			min = 0;
		} else {
			min = -Stats.nextStep(Math.abs(min));
		}
		if (max < 0) {
			max = 0;
		} else {
			max = Stats.nextStep(max);
		}
		return new Range(min, max);
	}

}
