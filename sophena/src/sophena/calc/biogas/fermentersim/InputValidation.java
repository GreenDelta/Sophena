package sophena.calc.biogas.fermentersim;

import org.openlca.commons.Res;

import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.RoofType;

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

		if (plant.fermenters.isEmpty()) {
			return Res.error("Es wurde kein Fermenter definiert.");
		}

		if (plant.substrateProfiles.isEmpty())
			return Res.error("Es wurde kein Substratinput definiert.");
		for (var p : plant.substrateProfiles) {
			if (p.substrate == null || invalid(p.hourlyValues))
				return Res.error("Es gibt unvollständige Substratprofile.");
		}

		return Res.ok();
	}

	/// Checks the rated power of the plant. It is only required when there is a
	/// fermenter with a double membrane roof, as it determines the support air
	/// flow of the roof.
	static Res<Void> ofRatedPower(BiogasPlant plant, double ratedPowerKW) {
		if (!Double.isFinite(ratedPowerKW) || ratedPowerKW < 0)
			return Res.error("Die Bemessungsleistung der Biogasanlage ist ungültig.");
		if (ratedPowerKW > 0)
			return Res.ok();
		for (var f : plant.fermenters) {
			if (f != null && f.roofType == RoofType.DOUBLE_MEMBRANE)
				return Res.error("Für ein Doppelmembrandach ist eine "
					+ "Bemessungsleistung der Biogasanlage erforderlich.");
		}
		return Res.ok();
	}

	private static boolean invalid(double[] values) {
		return values == null || values.length < Stats.HOURS;
	}

}
