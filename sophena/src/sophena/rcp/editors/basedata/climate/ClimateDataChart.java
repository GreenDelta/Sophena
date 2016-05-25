package sophena.rcp.editors.basedata.climate;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.rcp.M;
import sophena.rcp.utils.Colors;

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
		container.setBackground(Colors.getWhite());
		container.setLayout(new FillLayout());
		Canvas canvas = new Canvas(container, SWT.NONE);
		canvas.setBackground(Colors.getWhite());
		createChart(canvas);
		parent.getParent().setBackground(Colors.getWhite());
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(station.name);
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
		CircularBufferDataProvider data = new CircularBufferDataProvider(true);
		data.setBufferSize(Stats.HOURS);
		data.setCurrentYDataArray(station.data);
		data.setConcatenate_data(false);
		Trace trace = new Trace("Data", g.primaryXAxis, g.primaryYAxis, data);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceColor(Colors.getLinkBlue());
		g.addTrace(trace);
		Axis x = g.primaryXAxis;
		x.setRange(0, Stats.HOURS);
		x.setTitle(M.DwdSourceInfo);
		x.setMinorTicksVisible(false);
		x.setMajorGridStep(10000);
		x.setTitleFont(x.getFont());
		formatY(g);
	}

	private void formatY(XYGraph g) {
		double max = Stats.max(station.data);
		double min = Stats.min(station.data);
		Axis y = g.getYAxisList().get(0);
		y.setTitle("°C");
		y.setRange(min - 2, max + 2);
		y.setTitleFont(y.getFont());
	}

}
