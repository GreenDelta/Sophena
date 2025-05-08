package sophena.rcp.charts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.ProducerProfile;
import sophena.model.Stats;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public class ProducerProfileChart {

	public final XYGraph graph;
	public final XYGraph graphTemperature;
	private final CircularBufferDataProvider minData;
	private final CircularBufferDataProvider maxData;
	private final CircularBufferDataProvider temperatureData;

	public ProducerProfileChart(Composite parent, int height) {
		minData = Charts.dataProvider();
		maxData = Charts.dataProvider();
		temperatureData = Charts.dataProvider();
		var canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = height;
		LightweightSystem lws = new LightweightSystem(canvas);
		graph = createGraph(lws, false);
		
		var canvasTemp = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvasTemp, true, true).minimumHeight = height;
		LightweightSystem lwsTemp = new LightweightSystem(canvasTemp);
		graphTemperature = createGraph(lwsTemp, true);
	}

	public void setData(ProducerProfile profile) {
		if (profile == null)
			return;
		double[] max = profile.maxPower;
		if (max == null)
			max = new double[Stats.HOURS];
		double[] min = profile.minPower;
		if (min == null)
			min = new double[Stats.HOURS];
		double[] temp = profile.temperaturLevel;
		if (temp == null)
			temp = new double[Stats.HOURS];
		
		minData.setCurrentYDataArray(min);
		maxData.setCurrentYDataArray(max);
		temperatureData.setCurrentYDataArray(temp);

		// format the y-axis range
		double minMax = Stats.max(min);
		double maxMax = Stats.max(max);
		double top = Stats.nextStep(Math.max(maxMax, minMax));
		graph.getPrimaryYAxis().setRange(0, top);
		
		graphTemperature.getPrimaryYAxis().setRange(0, Stats.nextStep(Stats.max(temp)));
	}

	private XYGraph createGraph(LightweightSystem lws, Boolean isTemperatureGraph) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		g.setBackgroundColor(Colors.getWhite());
		if(isTemperatureGraph)
		{
			addMaxArea(g, true);
			addMaxLine(g, true);
		}
		else
		{
			addMaxArea(g, false);
			addMinArea(g);
			addMaxLine(g, false);
		}
		
		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setTitle("");
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);

		var y = g.getPrimaryYAxis();
		if(isTemperatureGraph)
			y.setTitle("°C");
		else
			y.setTitle("kW");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");
		return g;
	}

	private void addMaxArea(XYGraph g, Boolean isTemperatureGraph) {
		Trace t;
		if(isTemperatureGraph)
			t = new Trace("Temperatur", g.getPrimaryXAxis(),
					g.getPrimaryYAxis(), temperatureData);
		else
			t = new Trace("Max", g.getPrimaryXAxis(),
					g.getPrimaryYAxis(), maxData);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setTraceColor(color());
		t.setAreaAlpha(255);
		g.addTrace(t);
	}

	private void addMinArea(XYGraph g) {
		var t = new Trace("Min", g.getPrimaryXAxis(),
				g.getPrimaryYAxis(), minData);
		t.setPointStyle(Trace.PointStyle.NONE);
		t.setTraceType(Trace.TraceType.AREA);
		t.setTraceColor(Colors.getWhite());
		t.setAreaAlpha(255);
		g.addTrace(t);
	}

	private void addMaxLine(XYGraph g, Boolean isTemperatureGraph) {
		Trace t;
		if(isTemperatureGraph)
			t = new Trace("TemperatureLine", g.getPrimaryXAxis(),
					g.getPrimaryYAxis(), temperatureData);
		else
			t = new Trace("MaxLine", g.getPrimaryXAxis(),
					g.getPrimaryYAxis(), maxData);
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
