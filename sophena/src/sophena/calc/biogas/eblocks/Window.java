package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;

import sophena.model.Stats;

/// The time window that is available for the next block, starting at a state
/// before the hour `t_0`.
///
/// From `t_0` the storage is filled until it is full at `t_f`. When the plant
/// then runs under full load without interruption, the storage is empty at
/// `t_e`:
///
/// ```
///   fill the storage     running the plant under full load
///  |-------------------|###################################|
///  t_0                     t_f                                       t_e
/// ```
///
/// - A block must start in `[t_0, t_f]`. The hour `t_f` is included because
///   the gas that is produced in the hours after `t_f` does not fit into the
///   storage anymore.
/// - The plant can run in the hours `[t_f, t_e)`, so `n = t_e - t_f` is the
///   number of hours that the plant can run at most. The fuel for the
///   ramp-down is reserved in `n`, so a block of `n` hours always fits into
///   the storage, including the ramp-down after its last hour.
///
/// A window is empty when the plant cannot run in it at all. This happens when
/// the storage never fills in the year (`t_f = t_e = 8760`) or when the storage
/// is too small for a run of a single hour.
@NullMarked
record Window(State start, State full, State empty) {

	/// Computes the window that starts at the given state. The returned window
	/// contains the chain of states from `start` to `empty`, so a block variant
	/// can be checked against it without changing the window.
	static Window of(State start) {
		var full = start;
		while (full.canFillNext()) {
			full = full.fillNext();
		}

		var empty = full;
		while (empty.canRunNext()) {
			var next = empty.runNext();
			// the plant must be able to pay the ramp-down after the last hour
			// of a block, except when the block ends with the year
			if (next.hour() < Stats.HOURS && !next.canStop())
				break;
			empty = next;
		}

		return new Window(start, full, empty);
	}

	/// The hour `t_0` at which the window starts. It is the first hour that is
	/// not decided yet.
	int startHour() {
		return start.hour();
	}

	/// The hour `t_f` at which the storage is full. A block must start in
	/// `[t_0, t_f]`.
	int fillHour() {
		return full.hour();
	}

	/// The hour `t_e` after the last hour in which the plant can run under full
	/// load, starting from `t_f`.
	int emptyHour() {
		return empty.hour();
	}

	/// The number of hours the plant can run at most: `n = t_e - t_f`.
	int maxRunHours() {
		return emptyHour() - fillHour();
	}

	/// `true` when the plant cannot run in this window, so no block can start
	/// here.
	boolean isEmpty() {
		return maxRunHours() <= 0;
	}
}
