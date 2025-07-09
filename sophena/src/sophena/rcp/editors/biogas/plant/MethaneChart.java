package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import sophena.model.ProductType;
import sophena.model.Stats;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

public class MethaneChart {

	private final List<CircularBufferDataProvider> buffers = new ArrayList<>();
	private final List<Trace> traces = new ArrayList<>();
	public final XYGraph graph;

	public MethaneChart(Composite parent, int height) {
		var canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		UI.gridData(canvas, true, true).minimumHeight = height;
		var lws = new LightweightSystem(canvas);
		graph = createGraph(lws);
	}

	public void setInput(List<SubstrateProfile> profiles) {
		if (profiles == null)
			return;

		// clean up old traces
		for (var trace : traces) {
			graph.removeTrace(trace);
		}
		traces.clear();
		buffers.clear();

		// add traces
		var colors = ColorConfig.get().groupOf(ProductType.BIOMASS_BOILER);
		double maxValue = 0;
		for (int i = 0; i < profiles.size(); i++) {
			var profile = profiles.get(i);


			double[] data = profile.getMethaneProfile();
			maxValue = Math.max(maxValue, Stats.max(data));

			var buffer = Charts.dataProvider();
			buffer.setCurrentYDataArray(data);
			buffers.add(buffer);

			var color = Colors.of(colors.variant(i));
			var trace = traceOf(profile, color, buffer);
			traces.add(trace);
			graph.addTrace(trace);
		}

		double max = Stats.nextStep(maxValue, 5);
		var y = graph.getPrimaryYAxis();
		y.setRange(0, max);
	}

	private Trace traceOf(
			SubstrateProfile profile, Color color, CircularBufferDataProvider buffer
	) {
		var trace = new Trace(
				getTraceName(profile),
				graph.getPrimaryXAxis(),
				graph.getPrimaryYAxis(),
				buffer);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(Trace.TraceType.SOLID_LINE);
		trace.setTraceColor(color);
		trace.setLineWidth(2);
		return trace;
	}

	private String getTraceName(SubstrateProfile profile) {
		if (profile == null || profile.substrate == null)
			return "Unknown";
		return profile.substrate.name;
	}

	private XYGraph createGraph(LightweightSystem lws) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(true);
		g.setBackgroundColor(Colors.getWhite());

		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setTitle("Hours");
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);

		var y = g.getPrimaryYAxis();
		y.setTitle("m³ CH₄");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");

		return g;
	}

}
