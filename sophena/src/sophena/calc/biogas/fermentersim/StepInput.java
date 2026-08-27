package sophena.calc.biogas.fermentersim;

import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;

record StepInput(
	int doy,
	double hod,
	double tAirC,
	double bHorWm2,
	double dHorWm2,
	double windMps,
	double feedKgH
) {

	static StepInput of(
		SimulationConstants sim, BiogasPlant plant, WeatherStation station, int hour
	) {
		int doy = (hour / 24) + 1;
		double hod = hour % 24;
		return new StepInput(
			doy,
			hod,
			station.data[hour],
			station.directRadiation[hour],
			station.diffuseRadiation[hour],
			sim.windMps(),
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
