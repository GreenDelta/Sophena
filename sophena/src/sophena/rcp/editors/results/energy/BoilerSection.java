package sophena.rcp.editors.results.energy;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.ProjectResult;
import sophena.model.Producer;
import sophena.model.Stats;
import sophena.rcp.Images;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class BoilerSection {

	private XYGraph chart;
	private List<Trace> traces = new ArrayList<>();
	private boolean sorted = false;
	private ProjectResult result;
	private ProjectResult sortedResult;

	BoilerSection(Composite body, FormToolkit tk) {
		render(body, tk);
	}

	private void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Kesselbelegung");
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
		section.setLayoutData(gridData);
		gridData.heightHint = 250;
		gridData.minimumHeight = 250;
		Composite composite = UI.sectionClient(section, tk);
		composite.setLayout(new FillLayout());
		Canvas canvas = new Canvas(composite, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		chart = createGraph(lws);
		Actions.bind(section, new SortAction());
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

	public void setResult(ProjectResult pr) {
		this.result = pr;
		this.sortedResult = null;
		if (!sorted)
			renderChart(result);
		else {
			sortedResult = pr.sort();
			renderChart(sortedResult);
		}
	}

	private void renderChart(ProjectResult pr) {
		for (Trace trace : traces)
			chart.removeTrace(trace);

		double[] top = Arrays.copyOf(pr.getSuppliedPower(), Stats.HOURS);
		double max = Stats.nextStep(Stats.max(top), 5);
		chart.primaryYAxis.setRange(0, max);

		Producer[] producers = pr.getProducers();
		double[][] results = pr.getProducerResults();

		// top area for buffer result
		int idx = producers.length;
		Trace bufferTrace = createTrace("Pufferspeicher", top);
		bufferTrace.setTraceColor(Colors.getForChart(idx));
		chart.addTrace(bufferTrace);
		traces.add(bufferTrace);
		substract(top, pr.getSuppliedBufferHeat());

		for (int i = producers.length - 1; i >= 0; i--) {
			String label = producers[i].getName();
			Trace boilerTrace = createTrace(label, top);
			boilerTrace.setTraceColor(Colors.getForChart(i));
			chart.addTrace(boilerTrace);
			traces.add(boilerTrace);
			substract(top, results[i]);
		}
	}

	private void substract(double[] top, double[] result) {
		for (int k = 0; k < Stats.HOURS; k++) {
			top[k] -= result[k];
			if (top[k] < 0)
				top[k] = 0;
		}
	}

	private Trace createTrace(String label, double[] data) {
		CircularBufferDataProvider d = new CircularBufferDataProvider(true);
		d.setBufferSize(Stats.HOURS);
		d.setConcatenate_data(true);
		d.setCurrentYDataArray(data);
		Trace t = new Trace(label, chart.primaryXAxis, chart.primaryYAxis, d);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setAreaAlpha(255);
		return t;
	}

	private class SortAction extends Action {

		public SortAction() {
			setText("Unsortiert");
			setImageDescriptor(Images.SORTING_16.des());
		}

		@Override
		public void run() {
			if (sorted) {
				sorted = false;
				setText("Sortiert");
				renderChart(result);
			} else {
				sorted = true;
				setText("Unsortiert");
				if (sortedResult == null)
					sortedResult = result.sort();
				renderChart(sortedResult);
			}
		}
	}

}
