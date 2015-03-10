package sophena.rcp.editors.consumers;

import java.util.Arrays;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
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
import sophena.rcp.utils.UI;

class LoadCurveSection {

	private double[] data;

	private ConsumerEditor editor;

	public LoadCurveSection(ConsumerEditor editor) {
		this.editor = editor;
		data = ConsumerLoadCurve.calculate(editor.getConsumer(),
				editor.getProject().getWeatherStation(), App.getDb());
		Arrays.sort(data);
		for (int i = 0; i < data.length / 2; i++) {
			int j = data.length - i - 1;
			double v = data[i];
			data[i] = data[j];
			data[j] = v;
		}
	}

	void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "#Jahresdauerlinie");
		UI.gridData(section, true, true).minimumHeight = 150;
		Composite composite = UI.sectionClient(section, tk);
		composite.setLayout(new FillLayout());
		Canvas canvas = new Canvas(composite, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		CircularBufferDataProvider provider = new CircularBufferDataProvider(
				true);
		provider.setBufferSize(8760);
		provider.setCurrentYDataArray(data);
		provider.setConcatenate_data(false);
		Trace trace = new Trace("Data", g.primaryXAxis, g.primaryYAxis,
				provider);
		trace.setPointStyle(Trace.PointStyle.NONE);
		// trace.setTraceType(TraceType.BAR);
		g.addTrace(trace);
		// g.getXAxisList().get(0).setVisible(false);
		g.primaryXAxis.setRange(0, 8760);
		g.primaryXAxis.setTitle("");
		formatY(g);
	}

	private void formatY(XYGraph g) {
		double max = Statistics.max(data);
		Axis y = g.getYAxisList().get(0);
		y.setTitle("kW");
		y.setRange(0, max + 2);
		y.setTitleFont(y.getFont());
	}

}
