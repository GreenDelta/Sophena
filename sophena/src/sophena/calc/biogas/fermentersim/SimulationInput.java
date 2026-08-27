package sophena.calc.biogas.fermentersim;


import java.util.Arrays;

import sophena.model.Stats;
import sophena.model.WeatherStation;

/**
 * Simulation input for a full year of 8760 hours. The weather data are used
 * directly from the given weather station without copying; wind velocity and
 * the substrate feed rate are operational inputs that are not part of the
 * weather station data.
 */
public record SimulationInput(
	double windMps, double[] feedKgH
) {

	public SimulationInput {

		if (feedKgH == null || feedKgH.length != Stats.HOURS) {
			throw new IllegalArgumentException(
				"Feed rate data must have " + Stats.HOURS + " hourly values.");
		}
	}

	public int size() {
		return Stats.HOURS;
	}

	static SimulationInput constant(double windMps, double feedKgH) {
		var feed = new double[Stats.HOURS];
		Arrays.fill(feed, feedKgH);
		return new SimulationInput(windMps, feed);
	}
}
