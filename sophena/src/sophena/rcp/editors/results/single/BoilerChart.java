package sophena.rcp.editors.results.single;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.calc.EnergyResult;
import sophena.model.Producer;
import sophena.model.Stats;
import sophena.rcp.Icon;
import sophena.rcp.charts.Charts;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BoilerChart {

	private EnergyResult result;
	private boolean sorted = false;
	private final double maxY;
	private final double maxLoad;
	private final ResultColors colors;

	private XYGraph chart;
	private Trace loadTrace;

	BoilerChart(EnergyResult result, ResultColors colors, double maxY, double maxLoad) {
		this.result = result;
		this.colors = colors;
		this.maxY = maxY;
		this.maxLoad = maxLoad;
	}

	BoilerChart sorted(boolean sorted) {
		this.sorted = sorted;
		return this;
	}

	void render(Composite body, FormToolkit tk) {
		var title = sorted
				? "Geordnete Jahresdauerlinie"
				: "Ungeordnete Jahresdauerlinie";
		var section = UI.section(body, tk, title);
		Actions.bind(section, new LoadTraceSwitch(),
				ImageExport.forXYGraph("Jahresdauerlinie.jpg", () -> chart));
		UI.gridData(section, true, false);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var canvas = new Canvas(comp, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 250;
		var lws = new LightweightSystem(canvas);
		chart = createGraph(lws);
		addZoom(canvas);
		fillData();
	}

	private void addZoom(Canvas canvas) {
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

	private XYGraph createGraph(LightweightSystem lws) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		g.setBackgroundColor(Colors.getWhite());

		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		x.setTitle("");

		var y = g.getPrimaryYAxis();
		y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");

		double magnitude = Math.floor(Math.log10(maxY));
		double max = Math.pow(10, magnitude);
		double step = max / 10;
		double upper = step;
		while (step > 0 && upper <= maxY) {
			upper += step;
		}
		y.setRange(0, upper);

		// set annotation
		var a = new Annotation("ML", x, y);
		g.addAnnotation(a);
		a.setValues(5, maxLoad);
		a.setShowName(false);
		a.setShowPosition(false);
		a.setShowSampleInfo(false);
		a.setAnnotationColor(Colors.getChartRed());

		return g;
	}

	private void fillData() {
		if (result == null)
			return;
		if (sorted)
			result = result.sort();
		renderChart(result);
	}

	private void renderChart(EnergyResult r) {

		double[] supTop = Arrays.copyOf(r.suppliedPower, Stats.HOURS);
		Producer[] producers = r.producers;
		double[][] results = r.producerResults;

		List<Trace> traces = new ArrayList<>();

		// uncovered load
		double[] uncovered = new double[Stats.HOURS];
		boolean addUncovered = false;
		for (int i = 0; i < Stats.HOURS; i++) {
			double load = r.loadCurve[i];
			double supplied = r.suppliedPower[i];
			if ((load - supplied) > 1) {
				uncovered[i] = load;
				addUncovered = true;
			}
		}
		if (addUncovered) {
			var u = makeSuplierTrace("Ungedeckte Leistung", uncovered);
			u.setTraceColor(colors.of(ColorKey.UNCOVERED_LOAD));
			traces.add(u);
		}

		// buffer tank on top
		var bufferTrace = makeSuplierTrace("Pufferspeicher", supTop);
		bufferTrace.setTraceColor(colors.of(ColorKey.BUFFER_TANK));
		traces.add(bufferTrace);
		subtract(supTop, r.suppliedBufferHeat);

		// suppliers
		for (int i = producers.length - 1; i >= 0; i--) {
			var label = producers[i].name;
			var boilerTrace = makeSuplierTrace(label, supTop);
			boilerTrace.setTraceColor(colors.of(producers[i]));
			traces.add(boilerTrace);
			subtract(supTop, results[i]);
		}

		for (Trace trace : traces) {
			chart.addTrace(trace);
		}
	}

	private void subtract(double[] top, double[] result) {
		for (int k = 0; k < Stats.HOURS; k++) {
			top[k] -= result[k];
			if (top[k] < 0)
				top[k] = 0;
		}
	}

	private Trace makeSuplierTrace(String label, double[] data) {
		var d = Charts.dataProvider(data);
		var t = new Trace(
				label, chart.getPrimaryXAxis(), chart.getPrimaryYAxis(), d);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setAreaAlpha(255);
		return t;
	}

	private Trace makeLoadTrace(double[] load) {
		var d = Charts.dataProvider(load);
		var t = new Trace(
				"Req", chart.getPrimaryXAxis(), chart.getPrimaryYAxis(), d);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.SOLID_LINE);
		t.setTraceColor(Colors.getSystemColor(SWT.COLOR_BLACK));
		return t;
	}

	private class LoadTraceSwitch extends Action {

		private boolean active = false;

		public LoadTraceSwitch() {
			setImageDescriptor(Icon.REQUIRED_LOAD_16.des());
			setText("Lastkurve anzeigen");
		}

		@Override
		public void run() {
			if (active) {
				chart.removeTrace(loadTrace);
				setText("Lastkurve anzeigen");
			} else {
				if (loadTrace == null)
					loadTrace = makeLoadTrace(result.loadCurve);
				chart.addTrace(loadTrace);
				setText("Lastkurve entfernen");
			}
			active = !active;
		}
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
