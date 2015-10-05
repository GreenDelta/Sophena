package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.EnergyResult;
import sophena.model.Producer;
import sophena.model.Stats;
import sophena.rcp.Images;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class BoilerChart {

	private EnergyResult result;
	private boolean sorted = false;

	private XYGraph chart;
	private Trace loadTrace;

	public BoilerChart(EnergyResult result) {
		this.result = result;
	}

	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	public void render(Composite body, FormToolkit tk) {
		String title = sorted ? "Geordnete Jahresdauerlinie"
				: "Ungeordnete Jahresdauerlinie";
		Section section = UI.section(body, tk, title);
		Actions.bind(section, new LoadTraceSwitch());
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		Canvas canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 250;
		LightweightSystem lws = new LightweightSystem(canvas);
		chart = createGraph(lws);
		addZoom(canvas);
		fillData();
	}

	private void addZoom(Canvas canvas) {
		CtrlKey ctrlKey = new CtrlKey();
		canvas.addKeyListener(ctrlKey);
		canvas.addMouseWheelListener(e -> {
			if (!ctrlKey.pressed)
				return;
			Axis xAxis = chart.primaryXAxis;
			double valuePos = xAxis.getPositionValue(e.x, false);
			double factor = e.count < 0 ? -0.1 : 0.1;
			chart.primaryXAxis.zoomInOut(valuePos, factor);
		});
	}

	private XYGraph createGraph(LightweightSystem lws) {
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		Axis x = g.primaryXAxis;
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		Axis y = g.primaryYAxis;
		y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		return g;
	}

	private void fillData() {
		if (result == null)
			return;
		if (sorted)
			result = result.sort();
		renderChart(result);
	}

	private void renderChart(EnergyResult pr) {

		double[] supTop = Arrays.copyOf(pr.suppliedPower, Stats.HOURS);
		double supMax = Stats.nextStep(Stats.max(supTop), 5);
		double reqMax = Stats.nextStep(Stats.max(pr.loadCurve), 5);
		chart.primaryYAxis.setRange(0, Math.max(supMax, reqMax));

		Producer[] producers = pr.producers;
		double[][] results = pr.producerResults;

		List<Trace> traces = new ArrayList<>();

		// top area for buffer result
		int idx = producers.length;
		Trace bufferTrace = makeSuplierTrace("Pufferspeicher", supTop);
		bufferTrace.setTraceColor(Colors.getForChart(idx));
		traces.add(bufferTrace);
		substract(supTop, pr.suppliedBufferHeat);

		for (int i = producers.length - 1; i >= 0; i--) {
			String label = producers[i].name;
			Trace boilerTrace = makeSuplierTrace(label, supTop);
			boilerTrace.setTraceColor(Colors.getForChart(i));
			traces.add(boilerTrace);
			substract(supTop, results[i]);
		}

		for (Trace trace : traces)
			chart.addTrace(trace);
	}

	private void substract(double[] top, double[] result) {
		for (int k = 0; k < Stats.HOURS; k++) {
			top[k] -= result[k];
			if (top[k] < 0)
				top[k] = 0;
		}
	}

	private Trace makeSuplierTrace(String label, double[] data) {
		CircularBufferDataProvider d = createDataProvider(data);
		Trace t = new Trace(label, chart.primaryXAxis, chart.primaryYAxis, d);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setAreaAlpha(255);
		return t;
	}

	private Trace makeLoadTrace(double[] load) {
		CircularBufferDataProvider d = createDataProvider(load);
		Trace t = new Trace("Req", chart.primaryXAxis, chart.primaryYAxis, d);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.SOLID_LINE);
		t.setTraceColor(Colors.getSystemColor(SWT.COLOR_RED));
		return t;
	}

	private CircularBufferDataProvider createDataProvider(double[] data) {
		CircularBufferDataProvider d = new CircularBufferDataProvider(true);
		d.setBufferSize(Stats.HOURS);
		d.setConcatenate_data(true);
		d.setCurrentYDataArray(data);
		return d;
	}

	private class LoadTraceSwitch extends Action {

		private boolean active = false;

		public LoadTraceSwitch() {
			setImageDescriptor(Images.REQUIRED_LOAD_16.des());
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

	private class CtrlKey implements KeyListener {

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