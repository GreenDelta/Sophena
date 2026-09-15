package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasStorage;
import sophena.calc.biogas.Demand;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// An immutable snapshot of the biogas plant **before** a given hour.
///
/// - `hour`: the hour that is not decided yet. An hour is the interval
///   `[hour, hour + 1)`; the year has the hours `0` to `8759` and never wraps
///   around.
/// - `isRunning`: `true` when the plant ran in the hour before this state
///   (`hour - 1`). This decides how much fuel the plant needs in `hour`: the
///   first hour of a block needs the ramp-up (`1.125` hours of fuel under full
///   load), every following hour needs `1.0`.
/// - `storage`: the gas in the storage at the beginning of `hour`.
/// - `profile`: the biogas production of the plant over the year. It is shared
///   by all states and never modified.
/// - `previous`: the state before this state, `null` only for the initial
///   state. It allows walking back in the timeline, which is used when a block
///   is extended to the left.
///
/// The states form a chain: `fillNext` and `runNext` copy the storage, so an
/// existing state is never changed and every state can be used as the start of
/// its own scenario.
@NullMarked
record State(
	int hour,
	boolean isRunning,
	BiogasStorage storage,
	BiogasProfile profile,
	@Nullable State previous
) {

	/// The state at the beginning of the year: an empty storage and a stopped
	/// plant.
	static State initial(BiogasPlant plant) {
		return new State(
			0,
			false,
			BiogasStorage.of(plant),
			BiogasProfile.of(plant),
			null);
	}

	/// `true` when the plant can stay off in `hour` and the gas that is
	/// produced in that hour completely fits into the storage.
	boolean canFillNext() {
		return hour < Stats.HOURS && storage.canAdd(profile, hour);
	}

	/// The state after an hour in which the plant was off: the produced gas is
	/// added to the storage.
	State fillNext() {
		var nextStorage = storage.copy();
		nextStorage.add(profile, hour);
		return new State(hour + 1, false, nextStorage, profile, this);
	}

	/// `true` when the plant can run under full load in `hour`. The first hour
	/// of a block additionally needs the ramp-up, see `isRunning()`.
	boolean canRunNext() {
		return hour < Stats.HOURS && storage.canRun(profile, hour, demand());
	}

	/// The state after an hour in which the plant ran under full load.
	State runNext() {
		var nextStorage = storage.copy();
		nextStorage.run(profile, hour, demand());
		return new State(hour + 1, true, nextStorage, profile, this);
	}

	/// `true` when the storage still holds the fuel for the ramp-down
	/// (`0.125` hours under full load) that is needed when the plant stops
	/// after the run.
	boolean canStop() {
		return storage.canRunHours(Demand.RAMP.factor());
	}

	/// The state at the same hour after the plant was stopped: the fuel for the
	/// ramp-down is taken from the storage and the plant is off.
	///
	/// The hour does not change because the ramp-down happens directly after
	/// the last run hour. Its fuel is reserved from the gas that is available
	/// at the beginning of that hour, so it can also not be used when a new
	/// block starts in that hour.
	State stop() {
		var nextStorage = storage.copy();
		nextStorage.runHours(Demand.RAMP.factor());
		return new State(hour, false, nextStorage, profile, this);
	}

	/// Tries to run the plant in the `hours` hours that follow this state and
	/// returns the resulting block. Returns `null` when the storage cannot
	/// deliver the gas for that, that means when it would become empty during
	/// the run.
	///
	/// The block contains the ramp-up in its first hour (when the plant did not
	/// run in the hour before this state) and the ramp-down after its last hour
	/// (`stop()`). The ramp-down is not required when the run ends with the
	/// year.
	@Nullable
	Block getBlock(int hours) {
		if (hours < 1)
			return null;

		var end = this;
		for (int h = 0; h < hours; h++) {
			if (!end.canRunNext())
				return null;
			end = end.runNext();
		}

		// the year ends with the last hour of the run
		if (end.hour() >= Stats.HOURS)
			return new Block(this, end);

		if (!end.canStop())
			return null;
		return new Block(this, end.stop());
	}

	/// The fuel demand of the plant in `hour`: the ramp-up when the plant did
	/// not run in the hour before, the full load otherwise.
	private Demand demand() {
		return isRunning
			? Demand.FULL
			: Demand.FULL_RAMP;
	}
}
