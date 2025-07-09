package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
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

		var ds = ProfileData.allOf(profiles);
		double[] top = ProfileData.sum(ds);
		double max = Stats.max(top);

		// add traces
		var colors = ColorConfig.get().groupOf(ProductType.BIOMASS_BOILER);

		for (var d : ds) {

			var buffer = Charts.dataProvider();
			buffer.setCurrentYDataArray(top);
			buffers.add(buffer);
			d.subtractFrom(top);

			var color = Colors.of(colors.variant(d.index));
			var trace = traceOf(d.name(), color, buffer);
			traces.add(trace);
			graph.addTrace(trace);
		}

		var y = graph.getPrimaryYAxis();
		y.setRange(0, Stats.nextStep(max, 5));
	}

	private Trace traceOf(
			String name, Color color, CircularBufferDataProvider buffer
	) {
		var trace = new Trace(
				name, graph.getPrimaryXAxis(), graph.getPrimaryYAxis(), buffer);
		trace.setPointStyle(Trace.PointStyle.NONE);
		trace.setTraceType(TraceType.AREA);
		trace.setTraceColor(color);
		return trace;
	}

	private XYGraph createGraph(LightweightSystem lws) {
		var g = new XYGraph();
		lws.setContents(g);
		g.setShowTitle(false);
		g.setShowLegend(false);
		g.setBackgroundColor(Colors.getWhite());

		var x = g.getPrimaryXAxis();
		x.setRange(0, Stats.HOURS);
		x.setMajorGridStep(500);
		x.setMinorTicksVisible(false);
		x.setTitle("");

		var y = g.getPrimaryYAxis();
		y.setTitle("m³ CH₄");
		y.setTitleFont(y.getFont());
		y.setRange(0, 50);
		y.setMinorTicksVisible(false);
		y.setFormatPattern("###,###,###,###");

		return g;
	}

	private record ProfileData(int index, SubstrateProfile profile,
														 double[] data) {

		static ProfileData of(int index, SubstrateProfile profile) {
			var data = profile.getMethaneProfile();
			return new ProfileData(index, profile, data);
		}

		static List<ProfileData> allOf(List<SubstrateProfile> profiles) {
			var data = new ArrayList<ProfileData>(profiles.size());
			for (int i = 0; i < profiles.size(); i++) {
				data.add(ProfileData.of(i, profiles.get(i)));
			}
			return data;
		}

		static double[] sum(List<ProfileData> data) {
			var sum = new double[Stats.HOURS];
			for (var d : data) {
				for (int h = 0; h < sum.length; h++) {
					sum[h] += d.data[h];
				}
			}
			return sum;
		}

		String name() {
			return profile.substrate != null
					? profile.substrate.name
					: "Unknown";
		}

		void subtractFrom(double[] ds) {
			for (int h = 0; h < data.length; h++) {
				ds[h] -= data[h];
			}
		}
	}

}
