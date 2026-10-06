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

/// The runtime result of a biogas plant calculation: the gas that is produced
/// by the substrates of the plant and the hours of the year in which the plant
/// runs.
///
/// The run hours can be calculated with two algorithms, see `BiogasAlgorithm`.
/// The runtime result also contains the producer profile of the plant, which is
/// created with the default temperature (see `PRODUCER_TEMPERATURE`), and the
/// storage profile, which contains the gas that is in the biogas storage (in
/// m³) at the end of each hour.
///
/// The `gasStorageSize` of a result is the effective size that was used for
/// the calculation, see `BiogasPlants#effectiveGasStorageSizeOf(BiogasPlant)`.
///
/// The ramp hours (1/8 of the power before and after a block) are not included
/// in the run flags; they are added when the producer profile is created.
@NullMarked
public record BiogasRuntimeResult(
	BiogasPlant plant,
	BiogasProfile biogasProfile,
	double gasStorageSize,
	boolean[] runFlags,
	ProducerProfile producerProfile,
	double[] storageProfile
) {

	/// The temperature in °C that is used for the `producerProfile` of a
	/// result. The method `asProducerProfile` can be used to create a profile
	/// with another temperature.
	public static final double PRODUCER_TEMPERATURE = 95.0;

	/// Calculates the plant with the default algorithm
	/// (`BiogasAlgorithm.DEFAULT`) and returns an error when the plant cannot
	/// be calculated with it.
	public static Res<BiogasRuntimeResult> calculate(@Nullable BiogasPlant plant) {
		return calculate(plant, BiogasAlgorithm.DEFAULT);
	}

	/// Calculates the plant with the given algorithm and returns an error with
	/// a message that describes the problem when the plant cannot be calculated
	/// with it, e.g. when the gas storage is too small for the minimum runtime
	/// of the plant.
	public static Res<BiogasRuntimeResult> calculate(
		@Nullable BiogasPlant plant, @Nullable BiogasAlgorithm algorithm
	) {
		if (plant == null)
			return Res.error("No biogas plant provided");
		if (algorithm == null)
			return Res.error("no algorithm for the biogas plant given");
		var flags = switch (algorithm) {
			case HOURS -> EhourSearch.runFlags(plant);
			case BLOCKS -> EblockSearch.runFlags(plant);
		};
		return flags.then(runFlags -> {
			var profile = BiogasProfile.of(plant);
			return Res.ok(new BiogasRuntimeResult(
				plant,
				profile,
				BiogasPlants.effectiveGasStorageSizeOf(plant),
				runFlags,
				producerProfileOf(plant, runFlags, PRODUCER_TEMPERATURE),
				BiogasStorage.annualProfileOf(plant, profile, runFlags)));
		});
	}

	/// An empty result. It is used by callers that need a producer profile for
	/// a plant that cannot be calculated, e.g. when the plant is edited: an
	/// edit should always be possible, also when the plant is not complete.
	public static BiogasRuntimeResult emptyOf(BiogasPlant plant) {
		var runFlags = new boolean[Stats.HOURS];
		return new BiogasRuntimeResult(
			plant,
			BiogasProfile.empty(),
			0,
			runFlags,
			producerProfileOf(plant, runFlags, PRODUCER_TEMPERATURE),
			new double[Stats.HOURS]);
	}

	/// Creates the producer profile of this result with the given temperature
	/// in °C. The stored `producerProfile` was created with
	/// `PRODUCER_TEMPERATURE`.
	public ProducerProfile asProducerProfile(double temperature) {
		return producerProfileOf(plant, runFlags, temperature);
	}

	private static ProducerProfile producerProfileOf(
		BiogasPlant plant, boolean[] runFlags, double temperature
	) {
		var profile = new ProducerProfile();
		profile.id = UUID.randomUUID().toString();
		profile.minPower = new double[Stats.HOURS];
		profile.maxPower = new double[Stats.HOURS];
		profile.temperaturLevel = new double[Stats.HOURS];
		double power = BiogasPlants.totalThermalPower(plant);
		if (power <= 0)
			return profile;

		int n = runFlags.length;
		for (int h = 0; h < n; h++) {
			if (runFlags[h]) {
				profile.maxPower[h] = power;
				profile.temperaturLevel[h] = temperature;
			} else {
				// if we are before a block -> + 1/8 ramp-up
				if (h < (n - 1) && runFlags[h + 1]) {
					profile.maxPower[h] += power / 8;
					profile.temperaturLevel[h] = temperature;
				}

				// if we are after a block -> + 1/8 ramp-down
				if (h > 0 && runFlags[h - 1]) {
					profile.maxPower[h] += power / 8;
					profile.temperaturLevel[h] = temperature;
				}

				// note that we could be exactly between two
				// blocks and this is covered by the conditions
				// above, so do not join them in a single if-clause
			}
		}
		return profile;
	}
}
