package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.Stats;
import sophena.rcp.utils.Colors;

public class LoadCurveChart {

	private CircularBufferDataProvider chartData;
	private XYGraph graph;

	public LoadCurveChart(Composite parent) {
		chartData = new CircularBufferDataProvider(true);
		chartData.setBufferSize(Stats.HOURS);
		chartData.setConcatenate_data(false);
		Canvas canvas = new Canvas(parent, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
	}

	public void setData(double[] data) {
		chartData.setCurrentYDataArray(data);
		double max = Stats.nextStep(Stats.max(data), 5);
		Axis y = graph.primaryYAxis;
		y.setRange(0, max);
	}

	private XYGraph createGraph(LightweightSystem lws) {
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		Trace trace = new Trace("Data", g.primaryXAxis, g.primaryYAxis,
				chartData);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.AREA);
		trace.setTraceColor(Colors.getChartBlue());
		g.addTrace(trace);
		Axis x = g.primaryXAxis;
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		Axis y = g.primaryYAxis;
		y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		return g;
	}

}
