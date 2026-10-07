package sophena.rcp.editors.biogas.charts;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.linearscale.AbstractScale.LabelSide;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.BiogasRuntimeResult;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.Colors;

/// Shows the electricity prices of a biogas plant over the hours of a year.
/// The prices are shown as a single blue series with the left y-axis. The
/// producer profile of the plant is shown on a secondary y-axis on the right
/// side. The hours of the profile are colored like the states of the plant:
/// red when the plant runs in an hour in which the feed-in is not allowed,
/// green when it runs with a positive price, orange when it runs with a price
/// <= 0, and gray when it does not run.
public class ElectricityChart {

	private final XYGraph graph;
	private final Axis powerAxis;

	private final CircularBufferDataProvider priceData;
	private final CircularBufferDataProvider errorData;
	private final CircularBufferDataProvider runData;
	private final CircularBufferDataProvider warnData;
	private final CircularBufferDataProvider pauseData;

	public ElectricityChart(Composite parent) {
		graph = Charts.initHoursGraph(parent, 250);
		graph.getPrimaryYAxis().setTitle("Strompreis [ct/kWh]");

		// secondary axis on the right side for the power of the producer
		// profile
		powerAxis = new Axis("Leistung [kW]", true);
		powerAxis.setYAxis(true);
		powerAxis.setTickLabelSide(LabelSide.Secondary);
		powerAxis.setMinorTicksVisible(false);
		powerAxis.setFormatPattern("###,###,###,###");
		powerAxis.setRange(0, 500);
		graph.addAxis(powerAxis);



		// the producer profile on the right axis, colored by the state of the
		// hours
		errorData = Charts.dataProvider();
		runData = Charts.dataProvider();
		warnData = Charts.dataProvider();
		pauseData = Charts.dataProvider();

		// error -> runs although the feed-in is not allowed
		var errorTrace = Charts.lineTraceOf(
			graph, powerAxis, "error", Colors.getChartRed(), errorData);
		errorTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// run -> runs, and the price is ok
		var runTrace = Charts.lineTraceOf(
			graph, powerAxis, "run", Colors.of("#E1F4EE"), runData);
		runTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// warn -> runs, but the price is <= 0
		var warnTrace = Charts.lineTraceOf(
			graph, powerAxis, "warn", Colors.of("#ff9800"), warnData);
		warnTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// pause -> does not run, feed-in is not allowed
		var pauseTrace = Charts.lineTraceOf(
			graph, powerAxis, "pause", Colors.of("#d3d3d3"), pauseData);
		pauseTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

		// the electricity price as a single blue series with the left axis
		priceData = Charts.dataProvider();
		var priceTrace = Charts.lineTraceOf(
			graph, "Strompreis", Colors.getChartBlue(), priceData);
		priceTrace.setTraceType(Trace.TraceType.STEP_VERTICALLY);

	}

	public void setInput(BiogasRuntimeResult r) {
		var prices = pricesOf(r.plant());
		var max = Stats.max(prices);
		if (max == 0) {
			max = 50;
		}
		var min = Stats.min(prices);
		priceData.setCurrentYDataArray(prices);

		// the producer profile of the plant
		var profile = r.producerProfile();
		var power = profile != null && profile.maxPower != null
			? profile.maxPower
			: new double[Stats.HOURS];

		double[] errorVals = new double[Stats.HOURS];
		double[] runVals = new double[Stats.HOURS];
		double[] warnVals = new double[Stats.HOURS];
		double[] pauseVals = new double[Stats.HOURS];

		for (int h = 0; h < Stats.HOURS; h++) {
			double p = power[h];
			boolean isRunning = p > 0;
			boolean isBreak = !BiogasPlants.isFeedInAllowed(r.plant(), h);
			double price = prices[h];

			errorVals[h] = isRunning && isBreak ? p : 0;
			runVals[h] = isRunning && !isBreak && price > 0 ? p : 0;
			warnVals[h] = isRunning && !isBreak && price <= 0 ? p : 0;
			pauseVals[h] = isBreak && !isRunning ? p : 0;
		}

		errorData.setCurrentYDataArray(errorVals);
		runData.setCurrentYDataArray(runVals);
		warnData.setCurrentYDataArray(warnVals);
		pauseData.setCurrentYDataArray(pauseVals);

		graph.getPrimaryYAxis().setRange(min, max);

		double maxPower = Stats.max(power);
		powerAxis.setRange(0, maxPower > 0 ? Stats.nextStep(maxPower) : 100);
	}

	private double[] pricesOf(BiogasPlant plant) {
		if (plant == null
			|| plant.electricityPrices == null
			|| plant.electricityPrices.values == null)
			return new double[Stats.HOURS];
		return plant.electricityPrices.values;
	}
}
