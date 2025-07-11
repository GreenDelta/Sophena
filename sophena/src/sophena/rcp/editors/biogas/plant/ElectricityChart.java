package sophena.rcp.editors.biogas.plant;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.Colors;

class ElectricityChart {

	private final XYGraph graph;
	private final CircularBufferDataProvider priceData;

	ElectricityChart(Composite parent, int height) {
		graph = Charts.initHoursGraph(parent, height);
		graph.getPrimaryYAxis().setTitle("Strompreis [ct/kWh]");
		priceData = Charts.dataProvider();
		Charts.lineTraceOf(graph, "price", Colors.getChartBlue(), priceData);
	}

	void setInput(BiogasPlant plant) {
		var prices = pricesOf(plant);
		var max = Stats.max(prices);
		if (max == 0) {
			max = 50;
		}
		var min = Stats.min(prices);
		priceData.setCurrentYDataArray(prices);
		graph.getPrimaryYAxis().setRange(min, max);
	}

	private double[] pricesOf(BiogasPlant plant) {
		if (plant == null
				|| plant.electricityPrices == null
				|| plant.electricityPrices.values == null)
			return new double[Stats.HOURS];
		return plant.electricityPrices.values;
	}
}
