package sophena.calc.biogas.eblocks;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// Searches the blocks in which a biogas plant runs over one year.
///
/// The search walks from block to block. Every step starts with a `State`:
///
/// 1. `Window.of(state)` computes the window `[t_0, t_e)` of the state, see
///    `Window`. A block must start in `[t_0, t_f]` and the plant can run at
///    most `n = t_e - t_f` hours.
/// 2. `findBestMinBlock(window)` searches the block of the minimum runtime with
///    the highest price sum that starts in `[t_0, t_f]`.
/// 3. `PriceMarks.of(...)` marks the `n` hours of the window with the highest
///    price.
/// 4. `extend(...)` grows that block to the left and to the right as long as
///    the neighbouring hours are marked and the storage can deliver the gas.
/// 5. The block is added to the result and the search continues with the state
///    after the block.
///
/// Every step returns a `Res`, so the search stops with a message when it runs
/// into a state that it cannot handle instead of returning a wrong result.
/// `PreCheck` rejects the plants for which the search cannot work.
///
/// The search ends at the end of the year, also when the remaining hours are
/// not enough for a block of the minimum runtime. The gas that is produced in
/// these hours is not used.
@NullMarked
public final class EblockSearch {

	private final BiogasPlant plant;

	EblockSearch(BiogasPlant plant) {
		this.plant = plant;
	}

	/// Runs the block search and returns the hours of the year in which the
	/// plant runs: `flags[h]` is `true` when the plant runs in hour `h`.
	///
	/// The ramp hours (1/8 of the power before and after a block) are not
	/// included in the flags; they are added when the producer profile of the
	/// plant is created.
	///
	/// Returns an error when the plant cannot be calculated, e.g. when the gas
	/// storage is too small for the minimum runtime of the plant.
	public static Res<boolean[]> runFlags(BiogasPlant plant) {
		return run(plant).then(eblocks -> Res.ok(eblocks.runFlags()));
	}

	/// Runs the block search. This is the same as `runFlags` but it returns the
	/// blocks of the search, which is more useful for tests and charts.
	static Res<Eblocks> run(BiogasPlant plant) {
		var check = PreCheck.validate(plant);
		if (check.isError())
			return check.castError();
		return new EblockSearch(plant).search();
	}

	private Res<Eblocks> search() {
		var blocks = new ArrayList<Block>();
		var state = State.initial(plant);

		while (state.hour() < Stats.HOURS) {
			var window = Window.of(state);

			var found = findBestMinBlock(window);
			if (found.isError()) {
				// The year ends before a block of the minimum runtime can
				// start, also when the storage is full again. The plant does
				// not run any more and the gas of the remaining hours is not
				// used.
				if (window.fillHour() + plant.minimumRuntime > Stats.HOURS)
					break;
				return found.castError();
			}

			var marks = PriceMarks.of(plant, window, window.maxRunHours());
			var block = extend(window, marks, found.value());
			blocks.add(block);

			var next = block.end();
			if (next.hour() <= state.hour())
				return Res.error("the search does not advance at hour "
					+ state.hour() + "; a block must always move the state"
					+ " forward");
			state = next;
		}

		return Res.ok(new Eblocks(merge(blocks)));
	}

	/// Searches the block with the minimum runtime of the plant that starts in
	/// `[t_0, t_f]` of the window and has the highest price sum (see
	/// `Prices.sumOf`). When two blocks have the same price sum, the block that
	/// starts earlier is returned.
	///
	/// Returns an error when no block of the minimum runtime fits into the
	/// window. This cannot happen for a plant that passed the pre-check, but a
	/// search step must never continue with an undefined state.
	Res<Block> findBestMinBlock(Window window) {
		int hours = plant.minimumRuntime;
		if (window.maxRunHours() < hours)
			return Res.error("the storage allows a run of "
				+ window.maxRunHours() + " hours in the window from hour "
				+ window.startHour() + " to " + window.emptyHour()
				+ ", but the plant has a minimum runtime of " + hours
				+ " hours");

		Block best = null;
		double bestPrice = 0;
		var state = window.start();
		while (true) {
			var block = state.getBlock(hours);
			if (block != null) {
				double price = Prices.sumOf(plant, block);
				if (best == null || price > bestPrice) {
					best = block;
					bestPrice = price;
				}
			}
			if (state.hour() >= window.fillHour())
				break;
			state = state.fillNext();
		}

		if (best == null)
			return Res.error("no block of the minimum runtime of " + hours
				+ " hours fits into the storage in the window from hour "
				+ window.startHour() + " to " + window.emptyHour());
		return Res.ok(best);
	}

	/// Extends a block to the left and to the right as long as the neighbouring
	/// hour is marked as a price optimal hour (see `PriceMarks`) and the
	/// storage can deliver the gas for the longer block.
	///
	/// The block never grows beyond the window: `t_0` is the earliest and
	/// `t_e` is the hour after the latest hour in which the plant can run. The
	/// block is first extended to the left and then to the right, repeated
	/// until it cannot grow in either direction. When the prices are equal,
	/// this moves a block to the earliest hours from which it fits into the
	/// storage.
	Block extend(Window window, PriceMarks marks, Block block) {
		while (true) {
			var left = extendLeft(window, marks, block);
			if (left != null) {
				block = left;
				continue;
			}
			var right = extendRight(window, marks, block);
			if (right != null) {
				block = right;
				continue;
			}
			return block;
		}
	}

	/// Extends the block to the left when the hour before its first hour is
	/// marked, no hour before `t_0`, and the storage can deliver the gas for
	/// the longer block.
	@Nullable
	private Block extendLeft(Window window, PriceMarks marks, Block block) {
		var start = block.start();
		if (start.hour() <= window.startHour()
			|| !marks.contains(start.hour() - 1))
			return null;
		var previous = start.previous();
		return previous != null
			? previous.getBlock(block.length() + 1)
			: null;
	}

	/// Extends the block to the right when the hour after its last hour is
	/// marked and lies before `t_e`, and the storage can deliver the gas for
	/// the longer block.
	@Nullable
	private Block extendRight(Window window, PriceMarks marks, Block block) {
		int hour = block.end().hour();
		if (hour >= window.emptyHour() || !marks.contains(hour))
			return null;
		return block.start().getBlock(block.length() + 1);
	}

	/// Merges blocks that directly follow each other. Such blocks run without a
	/// ramp-down and a ramp-up in between, so they run as one block. The ramps
	/// that the search reserved for the two blocks are not needed then, so the
	/// merged block is always feasible.
	private static List<Block> merge(List<Block> blocks) {
		var merged = new ArrayList<Block>(blocks.size());
		for (var block : blocks) {
			if (merged.isEmpty()) {
				merged.add(block);
				continue;
			}
			var last = merged.getLast();
			if (last.end().hour() == block.start().hour()) {
				merged.set(merged.size() - 1, new Block(last.start(), block.end()));
			} else {
				merged.add(block);
			}
		}
		return merged;
	}
}
