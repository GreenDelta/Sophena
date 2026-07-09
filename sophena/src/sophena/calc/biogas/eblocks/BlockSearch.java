package sophena.calc.biogas.eblocks;

import org.jspecify.annotations.NullMarked;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

@NullMarked
public class BlockSearch {

	private final BiogasPlant plant;

	public BlockSearch(BiogasPlant plant) {
		this.plant = plant;
	}

	public void run() {

		var start = State.initial(plant);
		var end = start;
		while (end.canFillNext()) {
			end = end.fillNext();
		}

		if (end == start)
			return;

		int lastPossibleHour = end.hour() < Stats.HOURS - 1
			? end.hour() + 1
			: end.hour();

		Block minOpt = null;
		double price = Double.MIN_VALUE;
		for (var it = start; it.hour() <= lastPossibleHour; it = it.fillNext()) {
			var block = it.getBlock(plant.minimumRuntime);
			if (block == null)
				continue;
			var p = priceOf(block);
			if (minOpt == null || p > price) {
				minOpt = block;
				price = p;
			}
		}

		System.out.println(minOpt);

	}

	private double priceOf(Block block) {
		var p = plant.electricityPrices;
		if (p == null)
			return 0;
		double total = 0;
		for (int h = block.start().hour(); h <= block.end().hour(); h++) {
			total += p.values[h];
			if (!p.feedInAllowed[h]) {
				total -= 1000;
			}
		}
		return total;
	}


}
