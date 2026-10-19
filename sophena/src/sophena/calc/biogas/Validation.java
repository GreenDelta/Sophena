package sophena.calc.biogas;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.calc.biogas.runtime.BiogasProfile;
import sophena.calc.biogas.runtime.eblocks.EblockSearch;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Validates a biogas plant for the calculation. If the plant passes the
/// validation, it can be safely used in all calculations, so we need to run
/// this only once before a calculation is done.
@NullMarked
public class Validation {

	/// Checks if the biogas plant can be used for calculating the energy based
	/// results, excluding the costs.
	public static Res<Void> energetic(@Nullable BiogasPlant plant) {
		if (plant == null)
			return Res.error("Es wurde keine Biogasanlage übergeben.");
		if (plant.settings == null)
			return Res.error("Die Einstellungen zur Biogasanlage fehlen.");

		// check the boilers
		if (plant.boilers.isEmpty())
			return Res.error("Es wurden keine BHKW Aggregate definiert.");
		if (hasInvalidBoilers(plant))
			return Res.error(
				"Es wurden BHKW Blöcke mit ungültigen Leistungsdaten definiert.");

		if (plant.minimumRuntime < 1 || plant.minimumRuntime > 12)
			return Res.error("Die Mindestlaufzeit der Biogasanlage muss"
				+ " zwischen 1 und 12 Stunden liegen.");

		if (plant.electricityPrices == null
			|| plant.electricityPrices.values == null)
			return Res.error("Es wurden keine Spotmarktpreise definiert.");
		if (plant.electricityPrices.values.length < Stats.HOURS)
			return Res.error("Es wurden keine Spotmarktpreise für alle"
				+ " Stunden des Jahres definiert.");

		if (plant.substrateProfiles.isEmpty())
			return Res.error("Es wurden keine Substratprofile definiert.");

		double storageSize = BiogasPlants.effectiveGasStorageSizeOf(plant);
		if (storageSize <= 0)
			return Res.error("Es wurde keine gültige Gasspeichergröße definiert.");

		var profile = BiogasProfile.of(plant);
		double maxVolume = Stats.max(profile.volume());
		if (maxVolume <= 0)
			return Res.error(
				"Die Substrate der Biogasanlage erzeugen kein Biogas.");
		if (maxVolume > storageSize)
			return Res.error("Ein Gasspeicher von " + storageSize + " m3 kann"
				+ " das Gas nicht aufnehmen, das in einer Stunde erzeugt wird ("
				+ maxVolume + " m3).");

		if (!EblockSearch.canHoldMinimumBlock(plant))
			return Res.error("Ein Gasspeicher von " + storageSize + " m3 kann"
				+ " keinen Block der Mindestlaufzeit von "
				+ plant.minimumRuntime + " Stunden aufnehmen.");

		return Res.ok();
	}

	/// Checks if the biogas plant can be used in a full calculation, for the
	/// energetic and cost results.
	public static Res<Void> full(BiogasPlant plant) {
		var err = energetic(plant);
		if (err.isError())
			return err;

		if (!plant.settings.isFixedRemuneration
			&& plant.settings.marketValue == null)
			return Res.error("Die Referenzmarktwerte wurden nicht definiert.");

		return Res.ok();
	}

	private static boolean hasInvalidBoilers(BiogasPlant plant) {
		for (var entry : plant.boilers) {
			if (entry == null
				|| entry.boiler == null
				|| entry.boiler.maxPowerElectric <= 0
				|| entry.boiler.efficiencyRateElectric <= 0
				|| entry.boiler.maxPower <= 0
				|| entry.boiler.efficiencyRate <= 0)
				return true;
		}
		return false;
	}


}
