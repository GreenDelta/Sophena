package sophena.calc.biogas.fermentersim;


import sophena.model.Stats;
import sophena.model.WeatherStation;

/**
 * Generates a synthetic weather station dataset for 8760 hours.
 */
public final class SyntheticDataGenerator {

	private SyntheticDataGenerator() {
	}

	public static WeatherStation generateStation(FermenterParameters p) {
		var tAirC = new double[Stats.HOURS];
		var bHorWm2 = new double[Stats.HOURS];
		var dHorWm2 = new double[Stats.HOURS];

		for (int k = 0; k < Stats.HOURS; k++) {
			int doy = (k / 24) + 1;
			double hod = k % 24;

			tAirC[k] = computeSyntheticAirTemperature(doy, hod);

			double sinElevation = computeSunElevation(p, doy, hod);
			double solarFactor = Math.max(0.25, 0.65 + 0.35 * Math.sin(2.0 * Math.PI * (doy - 80.0) / 365.0));

			bHorWm2[k] = 750.0 * solarFactor * sinElevation;
			dHorWm2[k] = 120.0 * solarFactor * Math.sqrt(sinElevation);
		}

		var station = new WeatherStation();
		station.name = "Synthetic weather station";
		station.latitude = p.latitudeDeg();
		station.longitude = p.longitudeDeg();
		station.referenceLongitude = p.timeMeridianDeg();
		station.altitude = 0.0;
		station.data = tAirC;
		station.directRadiation = bHorWm2;
		station.diffuseRadiation = dHorWm2;
		return station;
	}

	private static double computeSyntheticAirTemperature(int doy, double hod) {
		return 9.0
			+ 10.0 * Math.sin(2.0 * Math.PI * (doy - 172.0) / 365.0)
			+ 3.0 * Math.sin(2.0 * Math.PI * (hod - 14.0) / 24.0);
	}

	private static double computeSunElevation(FermenterParameters p, int doy, double hod) {
		double dayAngleRad = Math.toRadians(360.0 / 365.0 * (doy - 81.0));
		double equationOfTimeMin = 9.87 * Math.sin(2.0 * dayAngleRad)
			- 7.53 * Math.cos(dayAngleRad)
			- 1.5 * Math.sin(dayAngleRad);

		double solarTimeH = hod + (4.0 * (p.longitudeDeg() - p.timeMeridianDeg()) + equationOfTimeMin) / 60.0;
		double hourAngleDeg = 15.0 * (solarTimeH - 12.0);
		double declinationDeg = 23.45 * Utils.sind(360.0 / 365.0 * (284.0 + doy));

		double sinElevation = Utils.sind(p.latitudeDeg()) * Utils.sind(declinationDeg)
			+ Utils.cosd(p.latitudeDeg()) * Utils.cosd(declinationDeg) * Utils.cosd(hourAngleDeg);

		return Math.clamp(sinElevation, 0.0, 1.0);
	}
}
