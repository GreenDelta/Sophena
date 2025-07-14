package sophena.rcp.editors.biogas.plant;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.BiogasPlantResult;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.Colors;

class ElectricityChart {

	private final XYGraph graph;
	private final CircularBufferDataProvider priceData;
	private final CircularBufferDataProvider runData;

	ElectricityChart(BiogasPlantEditor editor, Composite parent, int height) {
		graph = Charts.initHoursGraph(parent, height);
		graph.getPrimaryYAxis().setTitle("Strompreis [ct/kWh]");
		priceData = Charts.dataProvider();
		runData = Charts.dataProvider();
		Charts.lineTraceOf(graph, "price", Colors.getChartBlue(), priceData);
		Charts.areaTraceOf(graph, "run", Colors.getChartBlue(), runData).setAreaAlpha(100);
		editor.onResult(this::setInput);
	}

	private void setInput(BiogasPlantResult r) {
		var prices = pricesOf(r.plant());
		var max = Stats.max(prices);
		if (max == 0) {
			max = 50;
		}
		var min = Stats.min(prices);
		priceData.setCurrentYDataArray(prices);

		double[] runVals = new double[Stats.HOURS];
		for (int h = 0; h < Stats.HOURS; h++) {
			if (r.runFlags()[h]) {
				runVals[h] = max;
			}
		}
		runData.setCurrentYDataArray(runVals);

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
