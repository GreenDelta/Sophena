package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.EnergyResult;
import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.Stats;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class ElectricityChart {

	private EnergyResult result;

	private XYGraph chart;

	ElectricityChart(EnergyResult result) {
		this.result = result;
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Stromerzeugung");
		Actions.bind(section, ImageExport.forXYGraph("Stromerzeugung.jpg",
				() -> chart));
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		Canvas canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 250;
		LightweightSystem lws = new LightweightSystem(canvas);
		chart = createGraph(lws);
		renderChart();
	}

	private XYGraph createGraph(LightweightSystem lws) {
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		Axis x = g.primaryXAxis;
		x.setRange(0, Stats.HOURS);
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		x.setTitleFont(x.getFont());
		x.setTitle("Jahresstunden [h]");
		Axis y = g.primaryYAxis;
		y.setTitle("Leistung [kW]");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		return g;
	}

	private void renderChart() {
		double[] top = new double[Stats.HOURS];
		List<Plant> plants = collectPlants(top);
		double max = Stats.nextStep(top[0], 5);
		chart.primaryYAxis.setRange(0, max);
		Collections.reverse(plants);
		for (Plant p : plants) {
			Trace t = makeTrace(p, top);
			chart.addTrace(t);
			substract(top, p.values);
		}
	}

	private Trace makeTrace(Plant plant, double[] data) {
		CircularBufferDataProvider dp = new CircularBufferDataProvider(true);
		dp.setBufferSize(Stats.HOURS);
		dp.setConcatenate_data(true);
		dp.setCurrentYDataArray(data);
		Trace t = new Trace(plant.name, chart.primaryXAxis, chart.primaryYAxis,
				dp);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setAreaAlpha(255);
		t.setTraceColor(Colors.getForChart(plant.idx));
		return t;
	}

	private List<Plant> collectPlants(double[] total) {
		List<Plant> list = new ArrayList<>();
		for (int i = 0; i < result.producers.length; i++) {
			Producer p = result.producers[i];
			if (p.boiler == null || !p.boiler.isCoGenPlant)
				continue;
			Plant plant = new Plant();
			list.add(plant);
			plant.idx = i;
			plant.name = p.name;
			double heat = result.totalHeat(p);
			int hours = getFullLoadHours(p, heat);
			double power = p.boiler.maxPowerElectric;
			if (hours <= 0)
				continue;
			for (int h = 0; h < hours; h++) {
				plant.values[h] = power;
				total[h] += power;
			}
		}
		return list;
	}

	private int getFullLoadHours(Producer p, double producedHeat) {
		if (p == null || p.boiler == null)
			return 0;
		double maxPower = Producers.maxPower(p);
		return (int) Math.round(producedHeat / maxPower);
	}

	private void substract(double[] top, double[] result) {
		for (int k = 0; k < Stats.HOURS; k++) {
			top[k] -= result[k];
			if (top[k] < 0)
				top[k] = 0;
		}
	}

	private class Plant {
		int idx;
		String name;
		double[] values = new double[Stats.HOURS];
	}

}
