package sophena.rcp.editors;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import sophena.model.Statistics;
import sophena.model.WeatherStation;

class ClimateDataChart extends Dialog {

	private WeatherStation station;

	public ClimateDataChart(Shell parentShell, WeatherStation station) {
		super(parentShell);
		this.station = station;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.MIN);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());
		Canvas canvas = new Canvas(container, SWT.NONE);
		createChart(canvas);
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(station.getName());
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 300);
	}

	private void createChart(Canvas canvas) {
		LightweightSystem lws = new LightweightSystem(canvas);
		XYGraph g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		CircularBufferDataProvider provider = new CircularBufferDataProvider(
				true);
		provider.setCurrentYDataArray(station.getData());
		Trace trace = new Trace("Data", g.primaryXAxis, g.primaryYAxis,
				provider);
		trace.setTraceType(TraceType.LINE_AREA);
		trace.setPointStyle(Trace.PointStyle.NONE);
		g.addTrace(trace);
		g.getXAxisList().get(0).setVisible(false);
		formatY(g);
	}

	private void formatY(XYGraph g) {
		double max = Statistics.max(station.getData());
		double min = Statistics.min(station.getData());
		Axis y = g.getYAxisList().get(0);
		y.setTitle("°C");
		y.setRange(min - 2, max + 2);
		y.setTitleFont(y.getFont());
	}

}
