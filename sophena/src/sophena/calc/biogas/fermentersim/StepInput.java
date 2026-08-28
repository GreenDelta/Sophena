package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.BiogasPlant;

/// Hourly input values for the fermenter simulation.
///
/// @param hour            Current hour in the annual simulation.
/// Parameter of the original fermenter simulation model: `k`.
/// @param doy             Day of year in the annual simulation.
/// @param hod             Hour of day in the 24h cycle.
/// @param tAirC           Ambient air temperature in °C.
/// @param feedTemperature Temperature of the substrate feed in °C.
/// @param bHorWm2         Horizontal beam radiation in W/m2.
/// @param dHorWm2         Horizontal diffuse radiation in W/m2.
/// @param windMps         Wind speed in m/s.
/// @param feedKgH         Substrate feed mass in kg/h.
/// @param methaneContent  Methane content of the produced biogas as a fraction.
/// @param substrateHeatCapacity Heat capacity of the substrate feed in J/(kg K).
/// Parameter of the original fermenter simulation model: `sCpJkgK`.
record StepInput(
	int hour,
	int doy,
	double hod,
	double tAirC,
	double feedTemperature,
	double bHorWm2,
	double dHorWm2,
	double windMps,
	double feedKgH,
	double methaneContent,
	double substrateHeatCapacity
) {

	static StepInput of(
		BiogasPlant plant, WeatherScale scale, int hour
	) {
		int doy = (hour / 24) + 1;
		double hod = hour % 24;
		var station = scale.station();
		return new StepInput(
			hour,
			doy,
			hod,
			station.data[hour],
			feedTemperatureAt(plant, scale, hour),
			station.directRadiation[hour],
			station.diffuseRadiation[hour],
			Const.windMps,
			feedMassAt(plant, hour),
			methaneContentAt(plant, hour),
			substrateHeatCapacityAt(plant, hour)
		);
	}

	private static double feedMassAt(BiogasPlant plant, int hour) {
		double mass = 0;
		for (var p : plant.substrateProfiles) {
			mass += (p.hourlyValues[hour] * 1000);
		}
		return mass;
	}

	/// Calculates the temperature of the substrate feed at the given hour. The
	/// substrate temperature is assumed to range between the substrate's min. and
	/// max. temperatures, linearly interpolating based on the ambient air
	/// temperature.
	private static double feedTemperatureAt(
		BiogasPlant plant, WeatherScale scale, int hour
	) {
		double mass = 0;
		double substrateTemperature = 0;
		double airTemp = scale.station().data[hour];
		double minTemp = scale.minTemp();
		double maxTemp = scale.maxTemp();

		for (var p : plant.substrateProfiles) {
			double mi = p.hourlyValues[hour];
			mass += mi;

			double subMin = p.substrate.minTemperature;
			double subMax = p.substrate.maxTemperature;

			double subTemp = subMin
				+ ((airTemp - minTemp) / (maxTemp - minTemp))
				* (subMax - subMin);
			substrateTemperature += subTemp * mi;

		}
		return mass == 0 ? 0 : substrateTemperature / mass;
	}

	private static double methaneContentAt(BiogasPlant plant, int hour) {
		double mass = 0;
		double content = 0;
		for (var p : plant.substrateProfiles) {
			double mi = p.hourlyValues[hour];
			mass += mi;
			content += (p.substrate.methaneContent / 100) * mi;
		}
		return mass == 0 ? 0 : content / mass;
	}

	/// Calculates the heat capacity of the substrate input at the given hour in
	/// J/(kg K). We take this as the heat capacity of the fermenter content
	/// though this is just for the input. However, this seems to be still more
	/// reasonable instead taking an average value for the complete year when
	/// the substrates change over the year.
	private static double substrateHeatCapacityAt(BiogasPlant plant, int hour) {
		double mass = 0;
		double capa = 0;
		for (var p : plant.substrateProfiles) {
			double mi = p.hourlyValues[hour];
			mass += mi;

			double organicShare =
				(p.substrate.dryMatter / 100) * (p.substrate.organicDryMatter / 100);
			double waterShare = 1 - (p.substrate.dryMatter / 100);
			double inorganicShare = 1 - waterShare - organicShare;

			double capai = waterShare * 4180
				+ organicShare * 1800
				+ inorganicShare * 1000;
			capa += capai * mi;
		}
		return mass == 0 ? 0 : capa / mass;
	}

}
