package sophena.calc.biogas.eblocks;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import sophena.model.Stats;

/// The result of the block search: the blocks in which the plant runs over the
/// year. The blocks are ordered by their start hour, do not overlap, and each
/// of them has at least the minimum runtime of the plant.
@NullMarked
record Eblocks(List<Block> blocks) {

	Eblocks {
		blocks = List.copyOf(blocks);
	}

	/// The hours of the year in which the plant runs. The ramp hours (1/8 of
	/// the power before and after a block) are not included in the flags; they
	/// are added when the producer profile of the plant is created.
	boolean[] runFlags() {
		var flags = new boolean[Stats.HOURS];
		for (var block : blocks) {
			int from = Math.max(0, block.start().hour());
			int to = Math.min(Stats.HOURS, block.end().hour());
			for (int h = from; h < to; h++) {
				flags[h] = true;
			}
		}
		return flags;
	}
}
