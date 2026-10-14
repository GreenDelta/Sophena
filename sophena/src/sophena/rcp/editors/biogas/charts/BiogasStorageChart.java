package sophena.rcp.editors.biogas.charts;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.calc.biogas.runtime.BiogasStorageProfile;
import sophena.model.ProductType;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.Colors;

public class BiogasStorageChart {

	private final XYGraph graph;
	private final CircularBufferDataProvider data;

	public BiogasStorageChart(Composite parent) {
		graph = Charts.initHoursGraph(parent, 250);
		graph.getPrimaryYAxis().setTitle("Gasspeicherfüllstand [m³]");
		data = Charts.dataProvider();
		var rgb = ColorConfig.get()
			.groupOf(ProductType.BIOMASS_BOILER)
			.variant(0);
		var color = Colors.of(rgb);
		Charts.lineTraceOf(graph, "biogas", color, data);
	}

	public void setInput(BiogasPlant plant, BiogasRuntimeResult r) {
		if (plant == null || r == null)
			return;
		var res = BiogasStorageProfile.of(plant, r.runFlags());
		if (res.isError())
			return;
		var profile = res.value();
		data.setCurrentYDataArray(profile);
		var top = Stats.nextStep(Stats.max(profile));
		graph.getPrimaryYAxis().setRange(0, top);
	}
}
