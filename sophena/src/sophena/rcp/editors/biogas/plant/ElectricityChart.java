package sophena.rcp.editors.biogas.plant;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.BiogasAlgorithm;
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
	private final CircularBufferDataProvider otherData;

	ElectricityChart(BiogasPlantEditor editor, Composite parent, int height) {
		graph = Charts.initHoursGraph(parent, height);
		graph.getPrimaryYAxis().setTitle("Strompreis [ct/kWh]");
		defaultData = Charts.dataProvider();
		errorData = Charts.dataProvider();
		runData = Charts.dataProvider();
		warnData = Charts.dataProvider();
		pauseData = Charts.dataProvider();
		otherData = Charts.dataProvider();

		// default -> electricity price
		var defaultTrace = Charts.lineTraceOf(
			graph, "default", Colors.getChartBlue(), defaultData);
		defaultTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// error -> runs when it should not
		var errorTrace = Charts.lineTraceOf(
			graph, "error", Colors.getChartRed(), errorData);
		errorTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// run -> runs, and price is ok
		var runTrace = Charts.lineTraceOf(
			graph, "run", Colors.of("#4caf50"), runData);
		runTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// warn -> runs, but price is <= 0
		var warnTrace = Charts.lineTraceOf(
			graph, "warn", Colors.of("#ff9800"), warnData);
		warnTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// pause -> does not run, and it should not
		var pauseTrace = Charts.lineTraceOf(
			graph, "pause", Colors.of("#d3d3d3"), pauseData);
		pauseTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// the run hours of the other algorithm; both algorithms are calculated
		// so that they can be compared while the block search is validated
		var other = otherAlgorithm();
		var otherTrace = Charts.lineTraceOf(
			graph, "run-" + other.name().toLowerCase(),
			Colors.of("#7e57c2"), otherData);
		otherTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// draw a gray line at y = 0
		var zeros = Charts.dataProvider(new double[Stats.HOURS]);
		var zeroTrace = Charts.lineTraceOf(
			graph, "zeros", Colors.of("#d3d3d3"), zeros);
		zeroTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

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

			defaultVals[h] = !isRunning && !isBreak ? price : 0;
			errorVals[h] = isRunning && isBreak ? price : 0;
			runVals[h] = isRunning && !isBreak && price > 0 ? price : 0;
			warnVals[h] = isRunning && !isBreak && price <= 0 ? price : 0;
			pauseVals[h] = isBreak && !isRunning ? price : 0;
		}

		defaultData.setCurrentYDataArray(defaultVals);
		errorData.setCurrentYDataArray(errorVals);
		runData.setCurrentYDataArray(runVals);
		warnData.setCurrentYDataArray(warnVals);
		pauseData.setCurrentYDataArray(pauseVals);
		otherData.setCurrentYDataArray(otherValues(r, prices));

		graph.getPrimaryYAxis().setRange(min, max);
	}

	/// The prices of the hours in which the other algorithm runs the plant.
	/// Returns an empty series when that algorithm cannot calculate the plant.
	private double[] otherValues(BiogasPlantResult r, double[] prices) {
		var values = new double[Stats.HOURS];
		var res = BiogasPlantResult.calculate(r.plant(), otherAlgorithm());
		if (res.isError())
			return values;
		var flags = res.value().runFlags();
		for (int h = 0; h < Stats.HOURS; h++) {
			values[h] = flags[h] ? prices[h] : 0;
		}
		return values;
	}

	/// The algorithm that is not the default algorithm of the calculation.
	private static BiogasAlgorithm otherAlgorithm() {
		return BiogasAlgorithm.DEFAULT == BiogasAlgorithm.HOURS
			? BiogasAlgorithm.BLOCKS
			: BiogasAlgorithm.HOURS;
	}

	private double[] pricesOf(BiogasPlant plant) {
		if (plant == null
			|| plant.electricityPrices == null
			|| plant.electricityPrices.values == null)
			return new double[Stats.HOURS];
		return plant.electricityPrices.values;
	}
}
