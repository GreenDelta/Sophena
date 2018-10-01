package sophena.rcp.editors.results.compare;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.IBarSeries.BarWidthStyle;
import org.swtchart.ISeries.SeriesType;

import sophena.calc.Comparison;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BarCharts {

	static Chart init(Composite body, FormToolkit tk, String title) {
		Section section = UI.section(body, tk, title);
		Composite comp = UI.sectionClient(section, tk);
		Chart chart = new Chart(comp, SWT.NONE);
		Actions.bind(section, ImageExport.forChart(
				title + ".jpg", () -> chart));
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 275;
		data.minimumWidth = 750;
		chart.setOrientation(SWT.HORIZONTAL);
		chart.setLayoutData(data);

		// we set a white title just to fix the problem that the y-axis is cut
		// sometimes
		chart.getTitle().setText(".");
		chart.getTitle().setFont(UI.defaultFont());
		chart.getTitle().setForeground(Colors.getWhite());
		chart.getTitle().setVisible(true);
		return chart;
	}

	static IBarSeries series(Chart chart, String name, Color color,
			double[] data) {
		IBarSeries bars = (IBarSeries) chart.getSeriesSet()
				.createSeries(SeriesType.BAR, name);
		bars.setYSeries(data);
		bars.setBarColor(color);
		bars.enableStack(true);
		bars.setBarWidthStyle(BarWidthStyle.FIXED);
		bars.setBarWidth(65);
		return bars;
	}

	static void createAxes(Chart chart, Comparison comparison, String unit) {
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
		y.getTitle().setText(unit);
		y.getTitle().setForeground(Colors.getBlack());
		y.getTick().setForeground(Colors.getBlack());
		y.getTick().setFormat(Num.getIntFormat());
	}
}
