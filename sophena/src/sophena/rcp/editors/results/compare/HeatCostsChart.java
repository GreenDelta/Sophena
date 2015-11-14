package sophena.rcp.editors.results.compare;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.model.Stats;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class HeatCostsChart {

	private Comparison comparison;
	private XYGraph graph;

	HeatCostsChart(Comparison comparison) {
		this.comparison = comparison;
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Wärmegestehungskosten in EUR/MWh");
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		Canvas canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 250;
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = new XYGraph();
		lws.setContents(graph);
		graph.setShowTitle(false);
		configureAxis();
		for (int i = 0; i < comparison.projects.length; i++) {
			addNormalTrace(i);
			addFundingTrace(i);
		}
	}

	private void configureAxis() {
		Axis x = graph.primaryXAxis;
		Axis y = graph.primaryYAxis;
		x.setRange(0, comparison.projects.length * 30);
		x.setVisible(false);
		double max = 0;
		for (int i = 0; i < comparison.projects.length; i++) {
			ProjectResult pr = comparison.results[i];
			max = Math.max(max, value(pr.costResult));
			max = Math.max(max, value(pr.costResultFunding));
		}
		y.setRange(0, Stats.nextStep(max, 10));
		y.setTitleFont(y.getFont());
		y.setTitle("EUR/MWh");
		y.setShowMajorGrid(true);
	}

	private void addNormalTrace(int i) {
		double x = i * 30 + 10;
		double y = value(comparison.results[i].costResult);
		String label = comparison.projects[i].name;
		Trace trace = createTrace(label, x, y);
		trace.setAreaAlpha(255);
		trace.setTraceColor(Colors.getForChart(i));
		graph.addTrace(trace);
	}

	private void addFundingTrace(int i) {
		double x = i * 30 + 20;
		double y = value(comparison.results[i].costResultFunding);
		String label = comparison.projects[i].name + " - mit Förderung";
		Trace trace = createTrace(label, x, y);
		trace.setAreaAlpha(100);
		trace.setTraceColor(Colors.getForChart(i));
		graph.addTrace(trace);
	}

	private Trace createTrace(String label, double x, double y) {
		CircularBufferDataProvider data = new CircularBufferDataProvider(false);
		data.setBufferSize(1);
		data.setCurrentXData(x);
		data.setCurrentYData(y);
		Trace trace = new Trace(label, graph.primaryXAxis,
				graph.primaryYAxis, data);
		trace.setTraceType(TraceType.BAR);
		trace.setLineWidth(20);
		return trace;
	}

	private double value(CostResult result) {
		if (result == null || result.grossTotal == null)
			return 0;
		double val = result.grossTotal.heatGenerationCosts;
		return val * 1000;
	}

}
