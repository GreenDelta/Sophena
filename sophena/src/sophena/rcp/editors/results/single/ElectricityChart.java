package sophena.rcp.editors.results.single;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.calc.EnergyResult;
import sophena.math.energetic.Producers;
import sophena.model.Stats;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ElectricityChart {

	private final EnergyResult result;
	private final ResultColors colors;
	private XYGraph chart;

	ElectricityChart(EnergyResult result, ResultColors colors) {
		this.result = result;
		this.colors = colors;
	}

	void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Stromerzeugung");
		Actions.bind(section,
				ImageExport.forXYGraph("Stromerzeugung.jpg", () -> chart));
		UI.gridData(section, true, false);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var canvas = new Canvas(comp, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 250;
		var lws = new LightweightSystem(canvas);
		chart = createGraph(lws);
		renderChart();
	}

	private XYGraph createGraph(LightweightSystem lws) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);

		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		x.setTitleFont(x.getFont());
		x.setTitle("Jahresstunden [h]");

		var y = g.getPrimaryYAxis();
		y.setTitle("Leistung [kW]");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");
		return g;
	}

	private void renderChart() {
		double[] top = new double[Stats.HOURS];
		var plants = collectPlants(top);
		double max = Stats.nextStep(top[0], 5);
		chart.getPrimaryYAxis().setRange(0, max);
		Collections.reverse(plants);
		for (var p : plants) {
			var trace = makeTrace(p, top);
			chart.addTrace(trace);
			subtract(top, p.values);
		}
	}

	private Trace makeTrace(Plant plant, double[] data) {
		var buffer = new CircularBufferDataProvider(true);
		buffer.setBufferSize(Stats.HOURS);
		buffer.setConcatenate_data(true);
		buffer.setCurrentYDataArray(data);
		var trace = new Trace(plant.name, chart.getPrimaryXAxis(),
				chart.getPrimaryYAxis(), buffer);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.AREA);
		trace.setAreaAlpha(255);
		trace.setTraceColor(plant.color);
		return trace;
	}

	private List<Plant> collectPlants(double[] total) {
		var list = new ArrayList<Plant>();
		for (int i = 0; i < result.producers.length; i++) {
			var p = result.producers[i];
			double power = Producers.electricPower(p);
			if (power <= 0)
				continue;
			var plant = Plant.of(p.name, colors.of(p));
			list.add(plant);
			double heat = result.totalHeat(p);
			int hours = (int) Producers.fullLoadHours(p, heat);
			if (hours <= 0)
				continue;
			for (int h = 0; h < hours; h++) {
				plant.values[h] = power;
				total[h] += power;
			}
		}
		return list;
	}

	private void subtract(double[] top, double[] result) {
		for (int k = 0; k < Stats.HOURS; k++) {
			top[k] -= result[k];
			if (top[k] < 0)
				top[k] = 0;
		}
	}

	private record Plant(String name, Color color, double[] values) {
		static Plant of(String name, Color color) {
			return new Plant(name, color, new double[Stats.HOURS]);
		}
	}

}
