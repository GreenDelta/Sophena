package sophena.calc.biogas;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.calc.biogas.eblocks.EblockSearch;
import sophena.calc.biogas.ehours.EhourSearch;
import sophena.model.ProducerProfile;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// The result of a biogas plant calculation: the gas that is produced by the
/// substrates of the plant and the hours of the year in which the plant runs.
///
/// The run hours can be calculated with two algorithms, see `BiogasAlgorithm`:
/// the hour based algorithm (`EhourSearch`) that was used before and the block
/// based algorithm (`EblockSearch`) that keeps the minimum runtime of the
/// plant.
///
/// A calculation with a different algorithm produces different run hours and
/// therefore different results for the whole project in which the plant is
/// used, because the run hours are used to create the producer profile of the
/// plant (see `BiogasPlants.syncProducerProfile`).
///
/// The ramp hours (1/8 of the power before and after a block) are not included
/// in the run flags; they are added when the producer profile is created.
@NullMarked
public record BiogasPlantResult(
	BiogasPlant plant,
	BiogasProfile biogasProfile,
	double gasStorageSize,
	boolean[] runFlags
) {

	/// Calculates the plant with the default algorithm
	/// (`BiogasAlgorithm.DEFAULT`) and returns an error when the plant cannot
	/// be calculated with it.
	public static Res<BiogasPlantResult> calculate(BiogasPlant plant) {
		return calculate(plant, BiogasAlgorithm.DEFAULT);
	}

	/// Calculates the plant with the given algorithm and returns an error with
	/// a message that describes the problem when the plant cannot be calculated
	/// with it, e.g. when the gas storage is too small for the minimum runtime
	/// of the plant.
	public static Res<BiogasPlantResult> calculate(
		BiogasPlant plant, @Nullable BiogasAlgorithm algorithm
	) {
		if (algorithm == null)
			return Res.error("no algorithm for the biogas plant given");
		var flags = switch (algorithm) {
			case HOURS -> EhourSearch.runFlags(plant);
			case BLOCKS -> EblockSearch.runFlags(plant);
		};
		return flags.then(runFlags -> Res.ok(new BiogasPlantResult(
			plant,
			BiogasProfile.of(plant),
			plant != null ? plant.gasStorageSize : 0,
			runFlags)));
	}

	/// An empty result. It is used by callers that need a producer profile for
	/// a plant that cannot be calculated, e.g. when the plant is edited: an
	/// edit should always be possible, also when the plant is not complete.
	public static BiogasPlantResult emptyOf(@Nullable BiogasPlant plant) {
		return new BiogasPlantResult(
			plant,
			BiogasProfile.empty(),
			0,
			new boolean[Stats.HOURS]);
	}

	public ProducerProfile asProducerProfile(double temperature) {
		var profile = new ProducerProfile();
		profile.id = UUID.randomUUID().toString();
		profile.minPower = new double[Stats.HOURS];
		profile.maxPower = new double[Stats.HOURS];
		profile.temperaturLevel = new double[Stats.HOURS];
		double power = BiogasPlants.totalThermalPower(plant);
		if (power <= 0 || runFlags == null)
			return profile;

		int n = runFlags.length;
		for (int h = 0; h < n; h++) {
			if (runFlags[h]) {
				profile.maxPower[h] = power;
				profile.temperaturLevel[h] = temperature;
			} else {
				// if we are before a block -> 1/8 ramp-up
				if (h < (n - 1) && runFlags[h + 1]) {
					profile.maxPower[h] += power / 8;
					profile.temperaturLevel[h] = temperature;
				}

				// if we are after a block -> 1/8 ramp-down
				if (h > 0 && runFlags[h - 1]) {
					profile.maxPower[h] += power / 8;
					profile.temperaturLevel[h] = temperature;
				}
			}
		}
		return profile;
	}
}
