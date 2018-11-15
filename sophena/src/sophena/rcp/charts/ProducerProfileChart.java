package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.ProducerProfile;
import sophena.model.Stats;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

public class ProducerProfileChart {

	public final XYGraph graph;
	private final CircularBufferDataProvider minData;
	private final CircularBufferDataProvider maxData;

	public ProducerProfileChart(Composite parent, int height) {
		minData = Charts.dataProvider();
		maxData = Charts.dataProvider();
		Canvas canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = height;
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
	}

	public void setData(ProducerProfile profile) {
		if (profile == null)
			return;
		double[] max = profile.maxPower;
		if (max == null)
			max = new double[Stats.HOURS];
		double[] min = profile.minPower;
		if (min == null)
			min = new double[Stats.HOURS];
		minData.setCurrentYDataArray(min);
		maxData.setCurrentYDataArray(max);

		// format the y-axis range
		double minMax = Stats.max(min);
		double maxMax = Stats.max(max);
		double top;
		if (minMax > 0) {
			top = Stats.nextStep(maxMax + minMax);
		} else {
			top = Stats.nextStep(Stats.max(max));
		}
		graph.getPrimaryYAxis().setRange(0, top);
	}

	private XYGraph createGraph(LightweightSystem lws) {
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		addMaxTrace(g);
		addMinTrace(g);
		Axis x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		Axis y = g.getPrimaryYAxis();
		y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");
		return g;
	}

	private void addMaxTrace(XYGraph g) {
		Trace t = new Trace("Max", g.getPrimaryXAxis(),
				g.getPrimaryYAxis(), maxData);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setTraceColor(Colors.get("#ff6b6b"));
		t.setAreaAlpha(255);
		g.addTrace(t);
	}

	private void addMinTrace(XYGraph g) {
		Trace t = new Trace("Min", g.getPrimaryXAxis(),
				g.getPrimaryYAxis(), minData);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setTraceColor(Colors.getWhite());
		t.setAreaAlpha(255);
		g.addTrace(t);
	}
}
