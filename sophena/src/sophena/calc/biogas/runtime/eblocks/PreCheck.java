package sophena.calc.biogas.runtime.eblocks;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.runtime.BiogasProfile;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Checks whether the block search can be applied to a biogas plant.
///
/// The search needs a storage that can hold the gas for a block of the minimum
/// runtime, otherwise it runs into a state in which no block can be found (see
/// `EblockSearch.findBestMinBlock`). This check verifies that with the actual
/// production profile of the plant: it fills the storage from the initial
/// (empty) state until it stops filling and checks how many hours the plant can
/// then run. The plant passes when this is at least its minimum runtime.
///
/// This is an early check with a clear message. The search repeats the same
/// check for the real state of every window and returns an error when a block
/// does not fit, so it remains the actual guard for the calculation.
@NullMarked
final class PreCheck {

	private PreCheck() {
	}

	/// Validates the plant. Returns an error with a message that describes the
	/// problem when the plant cannot be calculated.
	static Res<Void> validate(@Nullable BiogasPlant plant) {

		if (plant == null)
			return Res.error("there is no biogas plant");

		double storageSize = BiogasPlants.effectiveGasStorageSizeOf(plant);
		if (storageSize <= 0)
			return Res.error("the gas storage size must be greater than 0");

		if (plant.minimumRuntime < 2 || plant.minimumRuntime > 12)
			return Res.error("the minimum runtime of the plant must be between"
				+ " 2 and 12 hours, but is " + plant.minimumRuntime);

		double fuelPower = BiogasPlants.fullLoadFuelDemand(plant);
		if (fuelPower <= 0)
			return Res.error("the fuel power of the plant under full load is 0");

		if (plant.electricityPrices == null
			|| plant.electricityPrices.values == null
			|| plant.electricityPrices.values.length < Stats.HOURS)
			return Res.error("the plant has no electricity prices of"
				+ " " + Stats.HOURS + " hours");

		var profile = BiogasProfile.of(plant);
		double maxVolume = Stats.max(profile.volume());
		if (maxVolume <= 0)
			return Res.error("the substrates of the plant do not produce"
				+ " any biogas");
		if (maxVolume > storageSize)
			return Res.error("a gas storage of " + storageSize
				+ " m3 cannot hold the gas that is produced in a single hour ("
				+ maxVolume + " m3)");

		// check with the actual production profile: from the initial state,
		// the storage must be able to deliver a block of the minimum runtime
		var window = Window.of(State.initial(plant));
		if (window.maxRunHours() < plant.minimumRuntime)
			return Res.error("a gas storage of " + storageSize
				+ " m3 cannot hold a block of the minimum runtime of "
				+ plant.minimumRuntime + " hours; increase the gas storage"
				+ " or lower the minimum runtime");
		return Res.ok();
	}
}
