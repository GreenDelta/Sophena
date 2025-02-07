package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public class LoadProfileChart {

	private final CircularBufferDataProvider sumData;
	private final CircularBufferDataProvider staticData;
	private final ColorConfig colors = ColorConfig.get();
	public final XYGraph graph;

	public LoadProfileChart(Composite parent, int height) {
		sumData = Charts.dataProvider();
		staticData = Charts.dataProvider();
		var canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = height;
		var lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
	}

	public void setData(LoadProfile profile) {
		if (profile == null)
			return;
		double[] total = profile.calculateTotal();
		double[] stat = profile.staticData;
		if (stat == null) {
			stat = new double[Stats.HOURS];
		}
		sumData.setCurrentYDataArray(total);
		staticData.setCurrentYDataArray(stat);
		double max = Stats.nextStep(Stats.max(total), 5);
		var y = graph.getPrimaryYAxis();
		y.setRange(0, max);
	}

	private XYGraph createGraph(LightweightSystem lws) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		g.setBackgroundColor(Colors.getWhite());
		addSumTrace(g);
		addStaticTrace(g);

		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);

		var y = g.getPrimaryYAxis();
		y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");

		return g;
	}

	private void addSumTrace(XYGraph g) {
		var trace = new Trace(
				"Total", g.getPrimaryXAxis(), g.getPrimaryYAxis(), sumData);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.AREA);
		trace.setTraceColor(Colors.of(colors.get(ColorKey.LOAD_DYNAMIC)));
		trace.setAreaAlpha(255);
		g.addTrace(trace);
	}

	private void addStaticTrace(XYGraph g) {
		var trace = new Trace(
				"Static", g.getPrimaryXAxis(), g.getPrimaryYAxis(), staticData);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.AREA);
		trace.setTraceColor(Colors.of(colors.get(ColorKey.LOAD_STATIC)));
		trace.setAreaAlpha(255);
		g.addTrace(trace);
	}
}
