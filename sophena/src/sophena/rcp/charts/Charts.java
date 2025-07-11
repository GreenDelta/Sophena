package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.Stats;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public class Charts {

	private Charts() {
	}

	public static CircularBufferDataProvider dataProvider(double[] data) {
		var d = new CircularBufferDataProvider(true);
		d.setBufferSize(Stats.HOURS);
		d.setConcatenate_data(true);
		d.setCurrentYDataArray(data);
		return d;
	}

	public static CircularBufferDataProvider dataProvider() {
		var b = new CircularBufferDataProvider(true);
		b.setBufferSize(Stats.HOURS);
		b.setConcatenate_data(false);
		return b;
	}

	public static XYGraph initHoursGraph(Composite parent, int height) {
		var canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = height;
		var lws = new LightweightSystem(canvas);

		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		g.setBackgroundColor(Colors.getWhite());

		var x = g.getPrimaryXAxis();
		x.setTitleFont(x.getFont());
		x.setRange(0, Stats.HOURS);
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		x.setTitle("");

		var y = g.getPrimaryYAxis();
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");

		return g;
	}

	public static Trace areaTraceOf(
			XYGraph g, String name, Color color, CircularBufferDataProvider data
	) {
		var trace = new Trace(
				name, g.getPrimaryXAxis(), g.getPrimaryYAxis(), data);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(TraceType.AREA);
		trace.setAreaAlpha(255);
		trace.setTraceColor(color);
		g.addTrace(trace);
		return trace;
	}

	public static Trace lineTraceOf(
			XYGraph g, String name, Color color, CircularBufferDataProvider data
	) {
		var trace = new Trace(
				name, g.getPrimaryXAxis(), g.getPrimaryYAxis(), data);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(TraceType.SOLID_LINE);
		trace.setTraceColor(color);
		g.addTrace(trace);
		return trace;
	}

}
