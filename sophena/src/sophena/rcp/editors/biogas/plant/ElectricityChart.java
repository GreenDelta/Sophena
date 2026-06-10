package sophena.rcp.editors.biogas.plant;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.BiogasPlantResult;
import sophena.calc.biogas.BiogasPlants;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.Colors;

class ElectricityChart {

	private final XYGraph graph;
	private final CircularBufferDataProvider defaultData;
	private final CircularBufferDataProvider errorData;
	private final CircularBufferDataProvider runData;
	private final CircularBufferDataProvider warnData;
	private final CircularBufferDataProvider pauseData;

	ElectricityChart(BiogasPlantEditor editor, Composite parent, int height) {
		graph = Charts.initHoursGraph(parent, height);
		graph.getPrimaryYAxis().setTitle("Strompreis [ct/kWh]");
		defaultData = Charts.dataProvider();
		errorData = Charts.dataProvider();
		runData = Charts.dataProvider();
		warnData = Charts.dataProvider();
		pauseData = Charts.dataProvider();

		// defaultTrace: chart-blue, hour i is the price value if it does not run and there is no break; otherwise 0
		var defaultTrace = Charts.lineTraceOf(
			graph, "default", Colors.getChartBlue(), defaultData);
		defaultTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// errorTrace: chart-red, hour i is the price if it runs and there is a break; otherwise 0
		var errorTrace = Charts.lineTraceOf(
			graph, "error", Colors.getChartRed(), errorData);
		errorTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// runTrace: chart-green (using hex/RGB for green), hour i is the price if it runs, there is no break and the price is > 0; otherwise 0
		var runTrace = Charts.lineTraceOf(
			graph, "run", Colors.of("#4caf50"), runData);
		runTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// warnTrace: chart-orange (using hex/RGB for orange), hour i is the price if it runs, there is no break and the price is <= 0; otherwise 0
		var warnTrace = Charts.lineTraceOf(
			graph, "warn", Colors.of("#ff9800"), warnData);
		warnTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// pauseTrace: light-gray (top-trace), hour i is there is a break and it does not run; otherwise 0
		var pauseTrace = Charts.lineTraceOf(
			graph, "pause", Colors.of("#d3d3d3"), pauseData);
		pauseTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		editor.onResult(this::setInput);
	}

	private void setInput(BiogasPlantResult r) {
		var prices = pricesOf(r.plant());
		var max = Stats.max(prices);
		if (max == 0) {
			max = 50;
		}
		var min = Stats.min(prices);

		double[] defaultVals = new double[Stats.HOURS];
		double[] errorVals = new double[Stats.HOURS];
		double[] runVals = new double[Stats.HOURS];
		double[] warnVals = new double[Stats.HOURS];
		double[] pauseVals = new double[Stats.HOURS];

		for (int h = 0; h < Stats.HOURS; h++) {
			boolean isRunning = r.runFlags()[h];
			boolean isBreak = !BiogasPlants.isFeedInAllowed(r.plant(), h);
			double price = prices[h];

			// defaultTrace: price if !isRunning && !isBreak; otherwise 0
			if (!isRunning && !isBreak) {
				defaultVals[h] = price;
			} else {
				defaultVals[h] = 0;
			}

			// errorTrace: price if isRunning && isBreak; otherwise 0
			if (isRunning && isBreak) {
				errorVals[h] = price;
			} else {
				errorVals[h] = 0;
			}

			// runTrace: price if isRunning && !isBreak && price > 0; otherwise 0
			if (isRunning && !isBreak && price > 0) {
				runVals[h] = price;
			} else {
				runVals[h] = 0;
			}

			// warnTrace: price if isRunning && !isBreak && price <= 0; otherwise 0
			if (isRunning && !isBreak && price <= 0) {
				warnVals[h] = price;
			} else {
				warnVals[h] = 0;
			}

			// pauseTrace: price if isBreak && !isRunning; otherwise 0
			if (isBreak && !isRunning) {
				pauseVals[h] = price;
			} else {
				pauseVals[h] = 0;
			}
		}

		defaultData.setCurrentYDataArray(defaultVals);
		errorData.setCurrentYDataArray(errorVals);
		runData.setCurrentYDataArray(runVals);
		warnData.setCurrentYDataArray(warnVals);
		pauseData.setCurrentYDataArray(pauseVals);

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
