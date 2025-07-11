package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.model.ProductType;
import sophena.model.Stats;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.Colors;

class MethaneChart {

	private final List<Trace> traces = new ArrayList<>();
	private final XYGraph graph;

	MethaneChart(Composite parent, int height) {
		graph = Charts.initHoursGraph(parent, height);
		graph.getPrimaryYAxis().setTitle("m³ CH₄");
	}

	void setInput(List<SubstrateProfile> profiles) {
		if (profiles == null)
			return;

		// clean up old traces
		for (var trace : traces) {
			graph.removeTrace(trace);
		}
		traces.clear();

		var ds = ProfileData.allOf(profiles);
		double[] top = ProfileData.sum(ds);
		double max = Stats.max(top);

		// add traces
		var colors = ColorConfig.get().groupOf(ProductType.BIOMASS_BOILER);

		for (var d : ds) {

			var buffer = Charts.dataProvider();
			buffer.setCurrentYDataArray(top);
			d.subtractFrom(top);

			var color = Colors.of(colors.variant(d.index));
			var trace = Charts.areaTraceOf(graph, d.name(), color, buffer);
			traces.add(trace);
		}

		var y = graph.getPrimaryYAxis();
		y.setRange(0, Stats.nextStep(max, 5));
	}

	private record ProfileData(
			int index, SubstrateProfile profile, double[] data) {

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
