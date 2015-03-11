package sophena.rcp.editors.consumers;

import java.util.Arrays;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.ConsumerLoadCurve;
import sophena.model.Statistics;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class LoadCurveSection {

	private boolean sorted = true;

	private ConsumerEditor editor;
	private CircularBufferDataProvider chartData;
	private XYGraph graph;

	public LoadCurveSection(ConsumerEditor editor, Composite body,
			FormToolkit tk) {
		this.editor = editor;
		chartData = new CircularBufferDataProvider(true);
		chartData.setBufferSize(8760);
		chartData.setConcatenate_data(false);
		render(body, tk);
		editor.setLoadCurveSection(this);
		update();
	}

	void update() {
		double[] data = ConsumerLoadCurve.calculate(editor.getConsumer(),
				editor.getProject().getWeatherStation(), App.getDb());
		if (!sorted) {
			chartData.setCurrentYDataArray(data);
			return;
		}
		Arrays.sort(data);
		for (int i = 0; i < data.length / 2; i++) {
			int j = data.length - i - 1;
			double v = data[i];
			data[i] = data[j];
			data[j] = v;
		}
		chartData.setCurrentYDataArray(data);
		formatAxis(data);
	}

	private void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "#Jahresdauerlinie");
		UI.gridData(section, true, true).minimumHeight = 200;
		Composite composite = UI.sectionClient(section, tk);
		composite.setLayout(new FillLayout());
		Canvas canvas = new Canvas(composite, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
		Actions.bind(section, new SortAction());
	}

	private XYGraph createGraph(LightweightSystem lws) {
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		Trace trace = new Trace("Data", g.primaryXAxis, g.primaryYAxis,
				chartData);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(TraceType.AREA);
		trace.setTraceColor(Colors.getLinkBlue());
		g.addTrace(trace);
		// g.getXAxisList().get(0).setVisible(false);
		g.primaryXAxis.setRange(0, 8760);
		g.primaryXAxis.setTitle("");
		return g;
	}

	private void formatAxis(double[] data) {
		double max = Statistics.nextStep(Statistics.max(data), 5);
		Axis y = graph.primaryYAxis;
		y.setTitle("kW");
		y.setRange(0, max);
		y.setTitleFont(y.getFont());
	}

	private class SortAction extends Action {

		public SortAction() {
			setText("#Unsortiert");
			setImageDescriptor(Images.SORTING_16.des());
		}

		@Override
		public void run() {
			if (sorted) {
				sorted = false;
				setText("#Sortiert");
			} else {
				sorted = true;
				setText("#Unsortiert");
			}
			update();
		}
	}

}
