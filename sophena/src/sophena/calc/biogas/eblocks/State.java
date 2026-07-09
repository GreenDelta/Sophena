package sophena.calc.biogas.eblocks;

import sophena.calc.biogas.BiogasProfile;
import sophena.calc.biogas.BiogasStorage;
import sophena.model.Stats;

/// Describes the state of the system **before** a given hour. Every state has
/// a previous state, except the initial state, and every state is an immutable
/// snapshot. The biogas profile is shared and never mutated, the storage state
/// is copied for every instance.
public record State(
	int hour,
	boolean isRunning,
	BiogasStorage storage,
	BiogasProfile profile,
	State previous
) {


	/// Returns `true`
	/// The storage of the system is full when the next hour of produced biogas
	/// cannot be added without running the power plant.
	boolean canFillNext() {
		return hour < Stats.HOURS && !storage.canAdd(profile, hour);
	}


	boolean isEmpty() {
		return false; // TODO
	}


}
