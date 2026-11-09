package sophena.rcp.editors.producers.biogas;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.FermenterHeatDemand;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;

/// A chart that shows the hourly heat demand of the fermenters of a biogas
/// plant.
public record HeatDemandChart(XYGraph graph, CircularBufferDataProvider buffer) {

	public static HeatDemandChart create(Composite comp) {
		var graph = Charts.initHoursGraph(comp, 250);
		graph.getPrimaryYAxis().setTitle("kWh");
		var buffer = Charts.dataProvider();
		var color = Colors.of(
			ColorConfig.get().get(ColorKey.LOAD_STATIC));
		var trace = Charts.lineTraceOf(graph, "demand", color, buffer);
		trace.setTraceType(Trace.TraceType.STEP_VERTICALLY);
		return new HeatDemandChart(graph, buffer);
	}

	/// Updates the chart and returns the annual heat demand in kWh.
	public double update(Project project, BiogasPlant plant) {
		return update(FermenterHeatDemand.of(project, plant));
	}

	/// Shows the given hourly heat demand in kW and returns the annual heat
	/// demand in kWh. An empty chart is shown when the data are `null`.
	public double update(double[] data) {
		if (data == null) {
			data = new double[Stats.HOURS];
		}
		buffer.setCurrentYDataArray(data);
		double top = Stats.nextStep(Stats.max(data));
		graph.getPrimaryYAxis().setRange(0, top);
		return Stats.sum(data);
	}
}
