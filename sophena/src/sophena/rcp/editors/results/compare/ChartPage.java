package sophena.rcp.editors.results.compare;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.Comparison;
import sophena.rcp.utils.UI;

class ChartPage extends FormPage {

	private Comparison comparison;

	ChartPage(ComparisonView view) {
		super(view, "ComparisonChartPage", "Ergebnisvergleich");
		this.comparison = view.comparison;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Energie");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);

		Section section = UI.section(body, tk, "Wärmegestehungskosten");
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		Canvas canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = 250;
		LightweightSystem lws = new LightweightSystem(canvas);
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);

		CircularBufferDataProvider data = new CircularBufferDataProvider(false);
		data.setBufferSize(4);
		data.setCurrentXDataArray(new double[] { 1, 3, 5, 7 });
		data.setCurrentYDataArray(new double[] { 10, 20, 40, 50 });

		Trace trace = new Trace(
				"Trace1-XY Plot",
				g.primaryXAxis,
				g.primaryYAxis,
				data);

		// set trace property
		trace.setTraceType(TraceType.BAR);
		trace.setLineWidth(15);
		trace.setAreaAlpha(200);
		trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));
		// add the trace to xyGraph
		g.addTrace(trace);

	}
}
