package sophena.calc.biogas.fermentersim;

import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;

/// Hourly input values for the fermenter simulation.
///
/// @param doy             Day of year in the annual simulation.
/// @param hod             Hour of day in the 24h cycle.
/// @param tAirC           Ambient air temperature in °C.
/// @param feedTemperature Temperature of the substrate feed in °C.
/// @param bHorWm2         Horizontal beam radiation in W/m2.
/// @param dHorWm2         Horizontal diffuse radiation in W/m2.
/// @param windMps         Wind speed in m/s.
/// @param feedKgH         Substrate feed mass in kg/h.
record StepInput(
	int doy,
	double hod,
	double tAirC,
	double feedTemperature,
	double bHorWm2,
	double dHorWm2,
	double windMps,
	double feedKgH
) {

	static StepInput of(
		BiogasPlant plant, WeatherStation station, int hour
	) {
		int doy = (hour / 24) + 1;
		double hod = hour % 24;
		double tAirC = station.data[hour];
		return new StepInput(
			doy,
			hod,
			tAirC,
			tAirC,
			station.directRadiation[hour],
			station.diffuseRadiation[hour],
			Const.windMps,
			feedMassAt(plant, hour)
		);
	}

	private static double feedMassAt(BiogasPlant plant, int hour) {
		double mass = 0;
		for (var p : plant.substrateProfiles) {
			mass += (p.hourlyValues[hour] * 1000);
		}
		return mass;
	}
}
