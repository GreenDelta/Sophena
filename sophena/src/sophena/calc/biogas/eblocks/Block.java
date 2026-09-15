package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;

/// A block of hours in which the biogas plant runs.
///
/// The plant runs in the hours `[start.hour(), end.hour())`, so the number of
/// hours is `end.hour() - start.hour()`. Because a block always contains at
/// least one hour, `end.hour()` is always greater than `start.hour()`.
///
/// The block contains the ramp-up in its first hour and the ramp-down after its
/// last hour (see `State.getBlock`), except when the run ends with the year.
@NullMarked
record Block(State start, State end) {

	/// The number of hours in which the plant runs.
	int length() {
		return end.hour() - start.hour();
	}
}
