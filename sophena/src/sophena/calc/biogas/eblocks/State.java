package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasStorage;
import sophena.calc.biogas.Demand;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Describes the state of the system **before** a given hour. Every state has
/// a previous state, except the initial state, and every state is an immutable
/// snapshot. The biogas profile is shared and never mutated, the storage state
/// is copied for every instance.
@NullMarked
record State(
	int hour,
	boolean isRunning,
	BiogasStorage storage,
	BiogasProfile profile,
	@Nullable State previous
) {

	static State initial(BiogasPlant plant) {
		return new State(
			0,
			false,
			BiogasStorage.of(plant),
			BiogasProfile.of(plant),
			null
		);
	}

	/// Returns `true` when the system can fill the storage for the next hour
	/// without running the power plant.
	boolean canFillNext() {
		return hour < Stats.HOURS && storage.canAdd(profile, hour);
	}

	State fillNext() {
		var nextStorage = storage.copy();
		nextStorage.add(profile, hour);
		return new State(
			hour + 1,
			false,
			nextStorage,
			profile,
			this
		);
	}

	boolean canRunNext() {
		if (hour >= Stats.HOURS)
			return false;
		var demand = previous != null && previous.isRunning()
			? Demand.FULL
			: Demand.FULL_RAMP;
		return storage.canRun(profile, hour, demand);
	}

	State runNext() {
		var nextStorage = storage.copy();
		var demand = previous != null && previous.isRunning()
			? Demand.FULL
			: Demand.FULL_RAMP;
		nextStorage.run(profile, hour, demand);
		return new State(
			hour + 1,
			false,
			nextStorage,
			profile,
			this
		);
	}

	/// Starting from the current state, tries to find the next runtime block for
	/// the given number of hours. Returns `null` if no such block could be found.
	@Nullable
	Block getNextBlock(int hours) {
		if (hours < 1)
			return null;
		var end = this;
		for (int h = 0; h < hours; h++) {
			if (!end.canRunNext())
				return null;
			end = end.runNext();
		}

		// ramp-down after the block
		if (!end.storage.canRunHours(Demand.RAMP.factor()))
			return null;
		end.storage.runHours(Demand.RAMP.factor());

		return new Block(this, end);
	}

}
