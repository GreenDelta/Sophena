package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public class LoadProfileChart {

	private final CircularBufferDataProvider sumData;
	private final CircularBufferDataProvider staticData;
	public final XYGraph graph;

	public LoadProfileChart(Composite parent, int height) {
		sumData = Charts.dataProvider();
		staticData = Charts.dataProvider();
		Canvas canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = height;
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
	}

	public void setData(LoadProfile profile) {
		if (profile == null)
			return;
		double[] total = profile.calculateTotal();
		double[] stat = profile.staticData;
		if (staticData == null)
			stat = new double[Stats.HOURS];
		sumData.setCurrentYDataArray(total);
		staticData.setCurrentYDataArray(stat);
		double max = Stats.nextStep(Stats.max(total), 5);
		Axis y = graph.primaryYAxis;
		y.setRange(0, max);
	}

	private XYGraph createGraph(LightweightSystem lws) {
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		addSumTrace(g);
		addStaticTrace(g);
		Axis x = g.primaryXAxis;
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		Axis y = g.primaryYAxis;
		y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");
		return g;
	}

	private void addSumTrace(XYGraph g) {
		Trace trace = new Trace("Total", g.primaryXAxis, g.primaryYAxis,
				sumData);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.AREA);
		trace.setTraceColor(Colors.getChartBlue());
		g.addTrace(trace);
	}

	private void addStaticTrace(XYGraph g) {
		Trace trace = new Trace("Static", g.primaryXAxis, g.primaryYAxis,
				staticData);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.AREA);
		trace.setTraceColor(Colors.getLinkBlue());
		g.addTrace(trace);
	}

}
