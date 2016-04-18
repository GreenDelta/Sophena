package sophena.rcp.editors.results.compare;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.rcp.utils.UI;

class FxBarChart {

	private Comparison comparison;
	private Val val;

	private FxBarChart(Comparison comparison, Val val) {
		this.comparison = comparison;
		this.val = val;
	}

	static void create(Comparison comparison, Composite comp, Val val) {
		new FxBarChart(comparison, val).render(comp);
	}

	private void render(Composite composite) {
		FXCanvas canvas = new FXCanvas(composite, SWT.NONE);
		UI.gridData(canvas, true, true).minimumHeight = 300;
		CategoryAxis x = new CategoryAxis();
		NumberAxis y = new NumberAxis();
		BarChart<String, Number> chart = new BarChart<>(x, y);
		y.setLabel(val.unit());
		Series<String, Number> series = new XYChart.Series<>();
		for (int i = 0; i < comparison.projects.length; i++) {
			ProjectResult pr = comparison.results[i];
			String name = comparison.projects[i].name;
			series.getData().add(new Data<>(name, val.get(pr.costResult)));
			series.getData().add(new Data<>(name + " - mit Förderung", val.get(pr.costResultFunding)));
		}
		chart.getData().add(series);
		chart.setLegendVisible(false);
		x.setTickLabelsVisible(false);
		x.setTickMarkVisible(false);
		Scene scene = new Scene(chart);
		String css = getClass().getResource("FxBarChart.css").toExternalForm();
		scene.getStylesheets().add(css);
		chart.applyCss();
		canvas.setScene(scene);
	}

	interface Val {

		String unit();

		double get(CostResult result);

	}

}
