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

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.model.Stats;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class BarChart {

	private Comparison comparison;
	private Data data;
	private XYGraph graph;

	private BarChart(Comparison comparison, Data data) {
		this.comparison = comparison;
		this.data = data;
	}

	static void create(Comparison comparison, Composite comp, Data data) {
		new BarChart(comparison, data).render(comp);
	}

	private void render(Composite composite) {
		Canvas canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 300;
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = new XYGraph();
		lws.setContents(graph);
		graph.setShowTitle(false);
		graph.setShowLegend(false);
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
		x.setMajorGridStep(999);
		x.setMinorTicksVisible(false);
		x.getScaleTickLabels().setVisible(false);
		x.setTitleFont(x.getFont());
		x.setTitle("");
		double max = 0;
		for (int i = 0; i < comparison.projects.length; i++) {
			ProjectResult pr = comparison.results[i];
			max = Math.max(max, data.value(pr.costResult));
			max = Math.max(max, data.value(pr.costResultFunding));
		}
		int step = (int) Math.pow(10, Math.floor(Math.log10(max)));
		y.setRange(0, Stats.nextStep(max, step));
		y.setTitleFont(y.getFont());
		y.setTitle(data.unit());
		y.setFormatPattern("#,###,###");
		y.setShowMajorGrid(true);
	}

	private void addNormalTrace(int i) {
		double x = i * 30 + 10;
		double y = data.value(comparison.results[i].costResult);
		String label = comparison.projects[i].name;
		Trace trace = createTrace(label, x, y);
		trace.setAreaAlpha(255);
		trace.setTraceColor(Colors.getForChart(i));
		graph.addTrace(trace);
	}

	private void addFundingTrace(int i) {
		double x = i * 30 + 20;
		double y = data.value(comparison.results[i].costResultFunding);
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
		trace.setLineWidth(40);
		return trace;
	}

	interface Data {

		String unit();

		double value(CostResult result);

	}

}