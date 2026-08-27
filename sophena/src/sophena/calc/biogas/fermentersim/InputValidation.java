package sophena.calc.biogas.fermentersim;

import org.openlca.commons.Res;

import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;

class InputValidation {

	private InputValidation() {
	}

	static Res<Void> of(WeatherStation station) {
		if (station == null) {
			return Res.error("Es sind keine Wetterdaten vorhanden.");
		}

		if (station.latitude == 0 && station.longitude == 0) {
			return Res.error("Die Wetterstation hat keine gültigen Koordinaten.");
		}

		if (invalid(station.data)) {
			return Res.error(
				"Die Daten zur Außentemperatur fehlen in den Wetterdaten.");
		}

		if (invalid(station.directRadiation)) {
			return Res.error(
				"Die Daten zur direkten Sonneneinstrahlung fehlen in den Wetterdaten.");
		}

		if (invalid(station.diffuseRadiation)) {
			return Res.error(
				"Die Daten zur diffusen Sonneneinstrahlung fehlen in den Wetterdaten.");
		}

		return Res.ok();
	}

	static Res<Void> of(BiogasPlant plant) {
		if (plant == null) {
			return Res.error("Es wurde keine gültige Biogasanlage angegeben.");
		}

		if (plant.fermenter == null) {
			return Res.error("Die Angaben zum Fermenter fehlen.");
		}

		return Res.ok();
	}

	private static boolean invalid(double[] values) {
		return values == null || values.length < Stats.HOURS;
	}

}
