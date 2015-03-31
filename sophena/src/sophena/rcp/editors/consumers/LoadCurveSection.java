package sophena.rcp.editors.consumers;

import java.io.File;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ConsumerLoadCurve;
import sophena.io.HoursProfile;
import sophena.model.Stats;
import sophena.rcp.Images;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Rcp;
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
		chartData.setBufferSize(Stats.HOURS);
		chartData.setConcatenate_data(false);
		render(body, tk);
		editor.onCalculated(this::update);
	}

	private void update(double[] data) {
		if (sorted) {
			Arrays.sort(data);
			for (int i = 0; i < data.length / 2; i++) {
				int j = data.length - i - 1;
				double v = data[i];
				data[i] = data[j];
				data[j] = v;
			}
		}
		chartData.setCurrentYDataArray(data);
		formatAxis(data);
	}

	private void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Jahresdauerlinie");
		GridData grid = UI.gridData(section, true, true);
		grid.minimumHeight = 250;
		grid.grabExcessVerticalSpace = true;
		Composite composite = UI.sectionClient(section, tk);
		composite.setLayout(new FillLayout());
		Canvas canvas = new Canvas(composite, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
		Actions.bind(section, new SortAction(), new ExportAction());
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
		g.primaryXAxis.setRange(0, Stats.HOURS);
		g.primaryXAxis.setTitle("");
		return g;
	}

	private void formatAxis(double[] data) {
		double max = Stats.nextStep(Stats.max(data), 5);
		Axis y = graph.primaryYAxis;
		y.setTitle("kW");
		y.setRange(0, max);
		y.setTitleFont(y.getFont());
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
			} else {
				sorted = true;
				setText("Unsortiert");
			}
			editor.calculate();
		}
	}

	private class ExportAction extends Action {

		public ExportAction() {
			setText("Als Datei speichern");
			setImageDescriptor(Images.FILE_16.des());
		}

		@Override
		public void run() {
			FileDialog dialog = new FileDialog(UI.shell(), SWT.SAVE);
			dialog.setFilterExtensions(new String[] { "*.csv" });
			dialog.setText("Jahresdauerlinie speichern");
			String path = dialog.open();
			if (path != null) {
				Rcp.run("Exportiere...", () -> doExport(path));
			}
		}

		private void doExport(String path) {
			try {
				double[] data = ConsumerLoadCurve.calculate(
						editor.getConsumer(),
						editor.getProject().getWeatherStation());
				File file = new File(path);
				HoursProfile.write(data, file);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to export load profile to " + path, e);
			}
		}
	}

}
