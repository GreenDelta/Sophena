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
	WeatherStation station,
	double windMps,
	double[] feedKgH
) {

	public SimulationInput {
		if (station == null) {
			throw new IllegalArgumentException("Weather station is required.");
		}
		if (station.data == null || station.data.length != Stats.HOURS) {
			throw new IllegalArgumentException(
				"Required weather data 'temperature' are missing or incomplete: "
					+ "expected " + Stats.HOURS + " hourly values.");
		}
		if (station.directRadiation == null || station.directRadiation.length != Stats.HOURS) {
			throw new IllegalArgumentException(
				"Required weather data 'direct radiation' are missing or incomplete: "
					+ "expected " + Stats.HOURS + " hourly values.");
		}
		if (station.diffuseRadiation == null || station.diffuseRadiation.length != Stats.HOURS) {
			throw new IllegalArgumentException(
				"Required weather data 'diffuse radiation' are missing or incomplete: "
					+ "expected " + Stats.HOURS + " hourly values.");
		}
		if (feedKgH == null || feedKgH.length != Stats.HOURS) {
			throw new IllegalArgumentException(
				"Feed rate data must have " + Stats.HOURS + " hourly values.");
		}
	}

	public int size() {
		return Stats.HOURS;
	}

	/**
	 * Creates an input with a constant wind velocity and feed rate over the
	 * whole year.
	 */
	public static SimulationInput constant(
		WeatherStation station, double windMps, double feedKgH
	) {
		var feed = new double[Stats.HOURS];
		Arrays.fill(feed, feedKgH);
		return new SimulationInput(station, windMps, feed);
	}
}
