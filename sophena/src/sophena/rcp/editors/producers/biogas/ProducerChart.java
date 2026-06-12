package sophena.rcp.editors.producers.biogas;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.model.ProducerProfile;
import sophena.model.Stats;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;

record ProducerChart(XYGraph graph, CircularBufferDataProvider buffer) {

	static ProducerChart create(Composite comp) {
		var graph = Charts.initHoursGraph(comp, 250);
		graph.getPrimaryYAxis().setTitle("kWh");
		var buffer = Charts.dataProvider();
		var color = Colors.of(
			ColorConfig.get().get(ColorKey.PRODUCER_PROFILE));
		var trace = Charts.lineTraceOf(graph, "power", color, buffer);
		trace.setTraceType(Trace.TraceType.STEP_VERTICALLY);
		return new ProducerChart(graph, buffer);
	}

	void update(ProducerProfile profile) {
		if (profile == null || profile.maxPower == null)
			return;
		var data = profile.maxPower;
		buffer.setCurrentYDataArray(data);
		double top = Stats.nextStep(Stats.max(data));
		graph.getPrimaryYAxis().setRange(0, top);
	}
}
