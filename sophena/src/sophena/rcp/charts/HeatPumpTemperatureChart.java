package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.Producer;
import sophena.model.Stats;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public class HeatPumpTemperatureChart {
	public final XYGraph graphTemperature;
	private final CircularBufferDataProvider temperatureData;

	public HeatPumpTemperatureChart(Composite parent, int height) {
		temperatureData = Charts.dataProvider();		
		var canvasTemp = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvasTemp, true, true).minimumHeight = height;
		LightweightSystem lwsTemp = new LightweightSystem(canvasTemp);
		graphTemperature = createGraph(lwsTemp);
	}

	public void setData(Producer producer) {
		if (producer == null)
			return;
		double[] temp = producer.sourceTemperatureHourly;
		if (temp == null)
			temp = new double[Stats.HOURS];
		temperatureData.setCurrentYDataArray(temp);		
		graphTemperature.getPrimaryYAxis().setRange(0, Stats.nextStep(Stats.max(temp)));
	}

	private XYGraph createGraph(LightweightSystem lws) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		g.setBackgroundColor(Colors.getWhite());
		addMaxArea(g);
		addMaxLine(g);
		
		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);

		var y = g.getPrimaryYAxis();
		y.setTitle("°C");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");
		return g;
	}

	private void addMaxArea(XYGraph g) {
		Trace t;
		t = new Trace("Temperatur", g.getPrimaryXAxis(), g.getPrimaryYAxis(), temperatureData);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setTraceColor(color());
		t.setAreaAlpha(255);
		g.addTrace(t);
	}

	private void addMaxLine(XYGraph g) {
		Trace t;
		t = new Trace("TemperatureLine", g.getPrimaryXAxis(), g.getPrimaryYAxis(), temperatureData);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.SOLID_LINE);
		t.setTraceColor(color());
		t.setAreaAlpha(255);
		g.addTrace(t);
	}

	private Color color() {
		// note that we need to create different color instances, otherwise
		// strange things happen
		return Colors.of(ColorConfig.get().get(ColorKey.PRODUCER_PROFILE));
	}
}
