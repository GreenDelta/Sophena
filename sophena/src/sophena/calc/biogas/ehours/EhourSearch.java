package sophena.calc.biogas.ehours;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasStorage;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// The hour based algorithm that selects the run hours of a biogas plant.
///
/// `ElectricityPriceSchedule` marks the hours of every day in which the storage
/// of the plant could be emptied: it fills a storage with the production of the
/// day, `hoursToEmpty` of that storage is the number of hours that are marked,
/// and the marked hours are the hours with the highest prices of that day.
///
/// The plant then runs through the year and runs in an hour when the storage
/// can deliver the gas for the minimum runtime of the plant, when the storage
/// is full, or when the plant is already running and did not reach the minimum
/// runtime yet. The plant stops when the storage is empty, so it also stops
/// when it did not run for the minimum runtime. Blocks that are shorter than
/// the minimum runtime are therefore normal in this algorithm (the test
/// `EhourSearchTest` describes this behavior in detail).
///
/// The block based algorithm that avoids these short blocks is
/// `sophena.calc.biogas.eblocks.EblockSearch`. This is the implementation of
/// `BiogasAlgorithm.HOURS`.
@NullMarked
public final class EhourSearch {

	private final BiogasPlant plant;
	private final int minRunTime;
	private final BiogasProfile profile;
	private final BiogasStorage storage;
	private final ElectricityPriceSchedule priceSchedule;

	private final boolean[] runFlags = new boolean[Stats.HOURS];
	private int runTime = 0;

	private EhourSearch(BiogasPlant plant) {
		this.plant = plant;
		minRunTime = Math.max(1, plant.minimumRuntime);
		profile = BiogasProfile.of(plant.substrateProfiles);
		storage = BiogasStorage.of(plant);
		priceSchedule = ElectricityPriceSchedule.calculate(plant, profile);
	}

	/// Runs the algorithm and returns the hours of the year in which the plant
	/// runs: `flags[h]` is `true` when the plant runs in hour `h`.
	///
	/// The ramp hours (1/8 of the power before and after a block) are not
	/// included in the flags; they are added when the producer profile of the
	/// plant is created.
	///
	/// Returns an error when the plant cannot be calculated, e.g. when it has
	/// no valid boiler.
	public static Res<boolean[]> runFlags(@Nullable BiogasPlant plant) {
		var check = check(plant);
		if (check.isError())
			return check.castError();
		return Res.ok(new EhourSearch(plant).run());
	}

	private static Res<Void> check(@Nullable BiogasPlant plant) {
		if (plant == null)
			return Res.error("there is no biogas plant");
		if (!BiogasPlants.hasValidBoilers(plant))
			return Res.error("the plant has no boiler with an electric power"
				+ " and an electric efficiency of more than 0");
		if (plant.gasStorageSize <= 0)
			return Res.error("the gas storage size must be greater than 0");
		if (plant.substrateProfiles.isEmpty())
			return Res.error("the plant has no substrate profiles");
		return Res.ok();
	}

	private boolean[] run() {
		for (int hour = 0; hour < Stats.HOURS; hour++) {

			storage.add(profile, hour);

			// the storage is empty
			if (!storage.canRunOneHour()) {
				stop();
				continue;
			}

			// the storage is full
			if (hour < (Stats.HOURS - 1) && !storage.canAdd(profile, hour + 1)) {
				runAt(hour);
				continue;
			}

			boolean priceOk = priceSchedule.shouldRunAt(hour);

			// if it is not running, start it only if the price is good and if
			// it can run for the minimum runtime
			if (runTime == 0) {
				if (priceOk && canStartAt(hour)) {
					runAt(hour);
				}
				continue;
			}

			// if it did not run for the minimum runtime or if the price is
			// good, keep it running
			if (priceOk || runTime < minRunTime) {
				runAt(hour);
				continue;
			}

			// otherwise stop it
			stop();
		}
		return runFlags;
	}

	private void runAt(int hour) {
		if (runTime == 0) {
			// ramp-up
			storage.runHours(0.125);
		}
		storage.runOneHour();
		runTime++;
		runFlags[hour] = true;
	}

	private void stop() {
		if (runTime > 0) {
			// ramp-down
			storage.runHours(0.125);
		}
		runTime = 0;
	}

	private boolean canStartAt(int startHour) {
		int endHour = startHour + minRunTime - 1;
		if (endHour >= Stats.HOURS)
			return false;
		var s = storage.copy();
		for (int h = startHour; h <= endHour; h++) {
			s.add(profile, h);
			double time = 1.0;
			// start and end could be the same when minRunTime = 1
			if (h == startHour) {
				time += 0.125;
			}
			if (h == endHour) {
				time += 0.125;
			}
			if (s.canRunHours(time)) {
				s.runHours(time);
			} else {
				return false;
			}
		}
		return true;
	}
}
