package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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

		addZoom(g, canvas);

		return g;
	}

	private static void addZoom(XYGraph chart, Canvas canvas) {
		var ctrlKey = new CtrlKey();
		canvas.addKeyListener(ctrlKey);
		canvas.addMouseWheelListener(e -> {
			if (!ctrlKey.pressed)
				return;
			var xAxis = chart.getPrimaryXAxis();
			double valuePos = xAxis.getPositionValue(e.x, false);
			double factor = e.count < 0 ? -0.1 : 0.1;
			chart.getPrimaryXAxis().zoomInOut(valuePos, factor);
		});
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

	private static class CtrlKey implements KeyListener {

		boolean pressed;

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == SWT.CTRL) {
				pressed = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.CTRL) {
				pressed = false;
			}
		}
	}

}
