package sophena.rcp.editors.biogas.charts;

import java.util.Arrays;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.model.ProductType;
import sophena.model.Stats;
import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasRuntimeResult;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.Colors;

public class BiogasChart {

	private final XYGraph graph;
	private final CircularBufferDataProvider biogasData;
	private final CircularBufferDataProvider methaneData;

	public BiogasChart(Composite parent) {
		graph = Charts.initHoursGraph(parent, 250);
		graph.getPrimaryYAxis().setTitle("Biogas [m³]");
		biogasData = Charts.dataProvider();
		methaneData = Charts.dataProvider();

		var colors = ColorConfig.get().groupOf(ProductType.BIOMASS_BOILER);
		var biogasColor = Colors.of(colors.variant(0));
		var methaneColor = Colors.of(colors.variant(1));
		Charts.areaTraceOf(graph, "biogas", biogasColor, biogasData);
		Charts.areaTraceOf(graph, "methane", methaneColor, methaneData);
	}

	public void setInput(BiogasRuntimeResult r) {
		// the biogas production only depends on the substrates of the plant;
		// the profile of the result is empty when the plant cannot be
		// calculated for other reasons, so we compute it from the plant here
		var profile = BiogasProfile.of(r.plant());

		var vol = profile.volume();
		biogasData.setCurrentYDataArray(vol);
		var max = Stats.max(vol);

		var met = profile.methaneContent();
		var m = Arrays.copyOf(profile.volume(), Stats.HOURS);
		for (int h = 0; h < Stats.HOURS; h++) {
			m[h] *= met[h];
		}
		methaneData.setCurrentYDataArray(m);
		graph.getPrimaryYAxis().setRange(0, Stats.nextStep(max));
	}
}
