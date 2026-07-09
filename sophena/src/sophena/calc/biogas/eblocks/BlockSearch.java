package sophena.calc.biogas.eblocks;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.apache.lucene.index.ReaderSlice;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.ElectricityPriceCurve;

@NullMarked
public class BlockSearch {

	private final BiogasPlant plant;
	private final ElectricityPriceCurve prices;

	private BlockSearch(BiogasPlant plant) {
		this.plant = plant;
		this.prices = plant.electricityPrices;
	}

	public static Res<Void> run(BiogasPlant plant) {
		var res = PreCheck.validate(plant);
		if (res.isError())
			return res;
		new BlockSearch(plant).run();
		return Res.ok();
	}

	private void run() {

		var state = State.initial(plant);
		var blocks = new ArrayDeque<BlockPrice>();

		while (state.hour() < Stats.HOURS) {
			var next = findNextBlock(state);
			if (next == null)
				break;
			var prev = !blocks.isEmpty()
				? blocks.peekLast()
				: null;

			if (prev == null) {
				blocks.add(next);
				state = next.block.end();
				continue;
			}


		}

	}

	@Nullable
	private BlockPrice findNextBlock(State start) {
		var end = start;
		while (end.canFillNext()) {
			end = end.fillNext();
		}
		if (end == start)
			return null;

		int lastPossibleHour = end.hour() < Stats.HOURS - 1
			? end.hour() + 1
			: end.hour();

		BlockPrice best = null;
		for (var it = start; it.hour() <= lastPossibleHour; it = it.fillNext()) {
			var block = it.getBlock(plant.minimumRuntime);
			if (block == null)
				continue;
			var next = priceOf(block);
			if (best == null || next.price > best.price) {
				best = next;
			}
		}

		return best;
	}

	private BlockPrice priceOf(Block block) {
		double total = 0;
		for (int h = block.start().hour(); h <= block.end().hour(); h++) {
			total += prices.values[h];
			if (!prices.feedInAllowed[h]) {
				total -= 1000;
			}
		}
		return new BlockPrice(block, total);
	}



	private record BlockPrice(Block block, double price) {
	}

}
