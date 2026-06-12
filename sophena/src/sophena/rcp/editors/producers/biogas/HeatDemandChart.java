package sophena.rcp.editors.producers.biogas;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;

record HeatDemandChart(XYGraph graph, CircularBufferDataProvider buffer) {

	static HeatDemandChart create(Composite comp) {
		var graph = Charts.initHoursGraph(comp, 250);
		graph.getPrimaryYAxis().setTitle("kWh");
		var buffer = Charts.dataProvider();
		var color = Colors.of(
			ColorConfig.get().get(ColorKey.LOAD_STATIC));
		var trace = Charts.lineTraceOf(graph, "demand", color, buffer);
		trace.setTraceType(Trace.TraceType.STEP_VERTICALLY);
		return new HeatDemandChart(graph, buffer);
	}

	void update(Project project, BiogasPlant plant) {
		var data = BiogasPlants.heatDemandOf(project, plant);
		buffer.setCurrentYDataArray(data);
		double top = Stats.nextStep(Stats.max(data));
		graph.getPrimaryYAxis().setRange(0, top);
	}
}
