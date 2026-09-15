package sophena.calc.biogas.eblocks;

import java.util.Arrays;

import org.jspecify.annotations.NullMarked;
import org.openlca.commons.Res;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasStorage;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Checks whether the block search can be applied to a biogas plant.
///
/// The search needs a storage that can hold the gas for a block of the minimum
/// runtime, otherwise it runs into a state in which no block can be found (see
/// `EblockSearch.findBestMinBlock`). Whether that can happen depends on the
/// hour in which a block starts, so this check uses the worst case that can
/// occur in the year:
///
/// - When the storage stops filling it contains at least the gas of one hour of
///   production. This is because the filling only stops when the production of
///   the next hour does not fit into the storage anymore. When the storage is
///   already full, it contains at least `size - max. production per hour`.
/// - The methane content of a storage is the weighted average of the methane
///   contents that were added, so it is at least the minimum methane content of
///   the production.
/// - The production during a block is at least the minimum production of an
///   hour of the year.
///
/// A plant that passes this check can always be calculated: every window of the
/// search contains at least one block of the minimum runtime. The check is
/// conservative, so a plant that fails it may still be feasible. The search
/// repeats the check with the real state of the window and returns an error
/// when it does not fit.
@NullMarked
final class PreCheck {

	private PreCheck() {
	}

	/// Validates the plant. Returns an error with a message that describes the
	/// problem when the plant cannot be calculated.
	static Res<Void> validate(BiogasPlant plant) {
		if (plant == null)
			return Res.error("there is no biogas plant");
		if (plant.gasStorageSize <= 0)
			return Res.error("the gas storage size must be greater than 0");
		if (plant.minimumRuntime < 2 || plant.minimumRuntime > 12)
			return Res.error("the minimum runtime of the plant must be between"
				+ " 2 and 12 hours, but is " + plant.minimumRuntime);
		if (!BiogasPlants.hasValidBoilers(plant))
			return Res.error("the plant has no boiler with an electric power"
				+ " and an electric efficiency of more than 0");
		double fuelPower = BiogasPlants.fullLoadFuelPower(plant);
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
		if (maxVolume > plant.gasStorageSize)
			return Res.error("a gas storage of " + plant.gasStorageSize
				+ " m3 cannot hold the gas that is produced in a single hour ("
				+ maxVolume + " m3)");

		// the gas that a storage contains at least when it stops filling, and
		// the minimum methane content of the produced gas
		double minVolume = Stats.min(profile.volume());
		double minMethane = minMethane(profile);
		double minGas = Math.max(minVolume, plant.gasStorageSize - maxVolume);

		var storage = new BiogasStorage(plant.gasStorageSize, fuelPower);
		storage.add(minGas, minMethane);
		var worstCase = new State(
			0, false, storage, worstCaseProfile(minVolume, minMethane), null);

		if (worstCase.getBlock(plant.minimumRuntime) == null)
			return Res.error("a gas storage of " + plant.gasStorageSize
				+ " m3 with a production of at least " + minVolume
				+ " m3 per hour and a methane content of " + (minMethane * 100)
				+ " % cannot hold a block of the minimum runtime of "
				+ plant.minimumRuntime + " hours; increase the gas storage"
				+ " or lower the minimum runtime");

		// TODO: this is the conservative test described above. It assumes that
		//  a block starts with the minimum gas content and that the plant
		//  produces the minimum amount of gas in every hour of the block. A
		//  plant that produces gas only in some hours of the day is rejected
		//  here although the search could find blocks in the producing hours.
		//  A tighter bound on the gas content of a storage that stops filling
		//  would make this check less conservative.
		return Res.ok();
	}

	/// The minimum methane content of the hours in which the plant produces
	/// gas. Returns 0 when there is no such hour.
	private static double minMethane(BiogasProfile profile) {
		double min = -1;
		for (int h = 0; h < Stats.HOURS; h++) {
			if (profile.volumeAt(h) <= 0)
				continue;
			double methane = profile.methaneContentAt(h);
			if (min < 0 || methane < min) {
				min = methane;
			}
		}
		return Math.max(0, min);
	}

	/// A profile in which every hour produces the minimum amount of gas.
	private static BiogasProfile worstCaseProfile(double volume, double methane) {
		var volumes = new double[Stats.HOURS];
		var methaneContents = new double[Stats.HOURS];
		Arrays.fill(volumes, volume);
		Arrays.fill(methaneContents, methane);
		return new BiogasProfile(volumes, methaneContents);
	}
}
