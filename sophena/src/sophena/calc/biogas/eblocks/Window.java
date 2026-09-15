package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;

/// The time window that is available for the next runtime block.
///
/// Starting from a state at `t_0`, the storage is filled until it is full at
/// `t_f`. When the plant then runs under full load from `t_f`, the storage is
/// empty at `t_e`. Thus, `n = t_e - t_f` is the maximum number of hours the
/// plant can run before the storage is empty:
///
/// ```
///   fill the storage     running the plant under full load
///  |-------------------|###################################|
///  t_0                     t_f                                       t_e
/// ```
///
/// A block must start between `t_0` and `t_f` because the gas that is produced
/// after `t_f` can no longer be stored. The maximum run time `n` is used to
/// select the price-optimal hours between `t_0` and `t_e`.
@NullMarked
record Window(State start, State full, State empty) {

	/// Computes the window that starts at the given state. The states in the
	/// window are a chain of immutable snapshots, so the window can be used to
	/// check possible block variants without changing its own state.
	static Window of(State start) {
		var full = start;
		while (full.canFillNext()) {
			full = full.fillNext();
		}
		var empty = full;
		while (empty.canRunNext()) {
			empty = empty.runNext();
		}
		return new Window(start, full, empty);
	}

	/// The hour `t_0` at which the window starts.
	int startHour() {
		return start.hour();
	}

	/// The hour `t_f` at which the storage is full. A block must start at or
	/// before this hour.
	int fillHour() {
		return full.hour();
	}

	/// The hour `t_e` at which the storage is empty when the plant runs under
	/// full load from `t_f`.
	int emptyHour() {
		return empty.hour();
	}

	/// The maximum number of hours the plant can run: `n = t_e - t_f`.
	int maxRunHours() {
		return emptyHour() - fillHour();
	}

	/// Returns `true` when the plant cannot run in this window. In this case
	/// it makes no sense to search for a block.
	boolean isEmpty() {
		return maxRunHours() <= 0;
	}
}
