package sophena.calc.biogas.fermentersim;


import sophena.model.Stats;
import sophena.model.WeatherStation;

/**
 * Generates a synthetic weather station dataset for 8760 hours.
 */
public final class TestWeatherStation {

	private static final double LATITUDE_DEG = 52.52;
	private static final double LONGITUDE_DEG = 13.405;
	private static final double REFERENCE_LONGITUDE_DEG = 15.0;

	private TestWeatherStation() {
	}

	public static WeatherStation get() {
		var temperature = new double[Stats.HOURS];
		var directRadiation = new double[Stats.HOURS];
		var diffuseRadiation = new double[Stats.HOURS];

		for (int k = 0; k < Stats.HOURS; k++) {
			int doy = (k / 24) + 1;
			double hod = k % 24;
			temperature[k] = computeTemperature(doy, hod);
			double sinElevation = computeSunElevation(doy, hod);
			double solarFactor = Math.max(
				0.25, 0.65 + 0.35 * Math.sin(2.0 * Math.PI * (doy - 80.0) / 365.0));

			directRadiation[k] = 750.0 * solarFactor * sinElevation;
			diffuseRadiation[k] = 120.0 * solarFactor * Math.sqrt(sinElevation);
		}

		var station = new WeatherStation();
		station.name = "Synthetic weather station";
		station.latitude = LATITUDE_DEG;
		station.longitude = LONGITUDE_DEG;
		station.referenceLongitude = REFERENCE_LONGITUDE_DEG;
		station.altitude = 0.0;
		station.data = temperature;
		station.directRadiation = directRadiation;
		station.diffuseRadiation = diffuseRadiation;
		return station;
	}

	private static double computeTemperature(int doy, double hod) {
		return 9.0
			+ 10.0 * Math.sin(2.0 * Math.PI * (doy - 172.0) / 365.0)
			+ 3.0 * Math.sin(2.0 * Math.PI * (hod - 14.0) / 24.0);
	}

	private static double computeSunElevation(int doy, double hod) {
		double dayAngleRad = Math.toRadians(360.0 / 365.0 * (doy - 81.0));
		double equationOfTimeMin = 9.87 * Math.sin(2.0 * dayAngleRad)
			- 7.53 * Math.cos(dayAngleRad)
			- 1.5 * Math.sin(dayAngleRad);

		double solarTimeH = hod
			+ (4.0 * (LONGITUDE_DEG - REFERENCE_LONGITUDE_DEG)
			+ equationOfTimeMin) / 60.0;
		double hourAngleDeg = 15.0 * (solarTimeH - 12.0);
		double declinationDeg = 23.45 * Utils.sind(360.0 / 365.0 * (284.0 + doy));

		double sinElevation = Utils.sind(LATITUDE_DEG) * Utils.sind(declinationDeg)
			+ Utils.cosd(LATITUDE_DEG)
			* Utils.cosd(declinationDeg)
			* Utils.cosd(hourAngleDeg);

		return Math.clamp(sinElevation, 0.0, 1.0);
	}
}
